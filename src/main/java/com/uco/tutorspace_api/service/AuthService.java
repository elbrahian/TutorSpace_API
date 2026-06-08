package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.utils.EmailValidator;
import com.uco.tutorspace_api.utils.JwtUtil;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.dto.AuthResponse;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.dto.RegisterRequest;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailValidator emailValidator;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registrarEstudiante(RegisterRequest request) {

        if (!emailValidator.esCorreoInstitucional(request.email())) {
            throw new IllegalArgumentException(
                    "Solo se permiten correos institucionales con dominio @uco.net.co"
            );
        }

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalStateException(
                    "Ya existe una cuenta registrada con este correo"
            );
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.nombre());
        estudiante.setEmail(request.email().toLowerCase());
        estudiante.setPassword(passwordEncoder.encode(request.password()));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);

        Estudiante guardado = estudianteRepository.save(estudiante);

        String token = jwtUtil.generateToken(guardado);
        return new AuthResponse(token, guardado.getRol().name(),
                guardado.getNombre(), guardado.getId());
    }

    public AuthResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Correo o contraseña incorrectos"));

        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new IllegalStateException("Tu cuenta está desactivada, contacta al administrador");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new UsernameNotFoundException("Correo o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(usuario);
        return new AuthResponse(token, usuario.getRol().name(),
                usuario.getNombre(), usuario.getId());
    }
}