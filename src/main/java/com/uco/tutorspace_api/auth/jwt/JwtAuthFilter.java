package com.uco.tutorspace_api.auth.jwt;

import com.uco.tutorspace_api.auth.service.UsuarioDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro HTTP que intercepta cada request para extraer y validar el token JWT.
 *
 * <p>Se ejecuta UNA SOLA VEZ por request (extiende {@link OncePerRequestFilter}).
 * Se inserta en la cadena de filtros de Spring Security ANTES del filtro
 * {@code UsernamePasswordAuthenticationFilter}.
 *
 * <p>Flujo de procesamiento:
 * <ol>
 *   <li>Leer el header {@code Authorization}.</li>
 *   <li>Si no existe o no empieza con "Bearer ", dejar pasar sin autenticar.</li>
 *   <li>Extraer el token y validar firma/expiracion con {@link JwtUtil}.</li>
 *   <li>Si es valido y no hay autenticacion previa en el contexto, cargar el
 *       {@link UserDetails} del usuario y registrar la autenticacion.</li>
 *   <li>Continuar con el siguiente filtro de la cadena.</li>
 * </ol>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioDetailsService usuarioDetailsService) {
        this.jwtUtil = jwtUtil;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    /**
     * Logica principal del filtro JWT.
     *
     * @param request     request HTTP entrante
     * @param response    response HTTP saliente
     * @param filterChain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header Authorization o no es un token Bearer, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token removiendo el prefijo "Bearer "
        String token = authHeader.substring(7);

        // Si el token es invalido (firma incorrecta, expirado, malformado), continuar sin autenticar
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el email del subject del token
        String email = jwtUtil.extractEmail(token);

        // Solo autenticar si hay email y el contexto de seguridad aun no tiene autenticacion
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar detalles del usuario desde la base de datos
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);

            // Crear objeto de autenticacion con las authorities del usuario
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Registrar la autenticacion en el contexto de seguridad del request actual
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
