package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.Utils.EmailValidator;
import com.uco.tutorspace_api.Utils.JwtUtil;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.dto.AuthResponse;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.dto.RegisterRequest;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailValidator emailValidator;
    private final AuthenticationManager authenticationManager;

    // HU-01 — registro de estudiante con correo institucional
    public AuthResponse registrarEstudiante(RegisterRequest request) {

        // CA-01 — validar dominio @uco.net.co
        if (!emailValidator.esCorreoInstitucional(request.email())) {
            throw new RuntimeException(
                    "Solo se permiten correos institucionales con dominio @uco.net.co"
            );
        }

        // CA-05 — correo duplicado
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException(
                    "Ya existe una cuenta registrada con este correo"
            );
        }

        // Crear estudiante
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.nombre());
        estudiante.setEmail(request.email().toLowerCase());
        estudiante.setPassword(passwordEncoder.encode(request.password()));
        estudiante.setRol(RolUsuario.ESTUDIANTE);   // CA-03
        estudiante.setEstado(EstadoUsuario.ACTIVO);

        Estudiante guardado = estudianteRepository.save(estudiante);

        // Generar token y retornar
        String token = jwtUtil.generateToken(guardado);
        return new AuthResponse(token, guardado.getRol().name(),
                guardado.getNombre(), guardado.getId());
    }

    public AuthResponse login(LoginRequest request) {

        // Buscar usuario antes de autenticar para dar mensajes claros
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos"));

        // Verificar que la cuenta esté activa
        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new RuntimeException("Tu cuenta está desactivada, contacta al administrador");
        }

        // Delegar autenticación a Spring Security (valida password con BCrypt)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(usuario);
        return new AuthResponse(token, usuario.getRol().name(),
                usuario.getNombre(), usuario.getId());
    }
}
