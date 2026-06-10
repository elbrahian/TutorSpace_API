package com.uco.tutorspace_api.auth.config;

import com.uco.tutorspace_api.auth.jwt.JwtAuthFilter;
import com.uco.tutorspace_api.auth.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion central de Spring Security para TutorSpace.
 *
 * <p>Define una arquitectura de seguridad stateless (sin sesion HTTP) basada en JWT:
 * <ul>
 *   <li>CSRF deshabilitado — no es necesario en APIs REST stateless.</li>
 *   <li>Sesiones deshabilitadas — cada request se autentica via token JWT.</li>
 *   <li>Endpoints {@code /auth/**} y {@code /error} son publicos (sin autenticacion).</li>
 *   <li>Cualquier otro endpoint requiere token JWT valido.</li>
 *   <li>El filtro {@link JwtAuthFilter} se ejecuta antes del filtro estandar de Spring Security.</li>
 * </ul>
 *
 * <p>Algoritmo de hash de passwords: BCrypt con factor de trabajo predeterminado (10).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UsuarioDetailsService usuarioDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    /**
     * Define la cadena principal de filtros de seguridad HTTP.
     *
     * @param http builder de configuracion de seguridad
     * @return {@link SecurityFilterChain} configurado
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF: no aplica para APIs REST stateless
                .csrf(AbstractHttpConfigurer::disable)
                // Sin sesion HTTP: cada request se valida por token JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reglas de autorizacion por endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error").permitAll() // endpoints publicos
                        .anyRequest().authenticated())                      // todo lo demas requiere JWT
                // Respuesta personalizada para requests sin token valido (HTTP 401)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"No autenticado. Por favor, inicie sesión.\"}");
                        }))
                .authenticationProvider(authenticationProvider())
                // Insertar el filtro JWT antes del filtro de autenticacion estandar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proveedor de autenticacion basado en {@link UsuarioDetailsService} y BCrypt.
     *
     * @return {@link AuthenticationProvider} configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Encoder de passwords usando el algoritmo BCrypt.
     *
     * <p>BCrypt incorpora un salt aleatorio automaticamente, por lo que
     * dos hashes del mismo texto plano producen valores distintos.
     *
     * @return {@link PasswordEncoder} BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
