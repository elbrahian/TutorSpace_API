package com.uco.tutorspace_api.auth.service;

import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacion de {@link UserDetailsService} que carga los datos de un usuario
 * desde la base de datos usando su email como identificador.
 *
 * <p>Spring Security utiliza esta clase para recuperar el {@link UserDetails}
 * durante la validacion del token JWT en {@link com.uco.tutorspace_api.auth.jwt.JwtAuthFilter}.
 *
 * <p>Las authorities se asignan con el prefijo {@code ROLE_} automaticamente
 * mediante el metodo {@code roles()} del builder de Spring Security
 * (ej: rol "ESTUDIANTE" se convierte en authority "ROLE_ESTUDIANTE").
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca un usuario en la base de datos por su email y lo convierte
     * en un objeto {@link UserDetails} de Spring Security.
     *
     * @param email identificador del usuario (campo "username" en el contexto de Spring Security)
     * @return {@link UserDetails} con email, password encriptada y rol del usuario
     * @throws UsernameNotFoundException si no existe ningun usuario con ese email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Construir UserDetails; roles() agrega el prefijo ROLE_ a cada rol automaticamente
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
