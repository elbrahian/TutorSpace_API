package com.uco.tutorspace_api.auth.service;

import com.uco.tutorspace_api.auth.dto.AuthResponse;
import com.uco.tutorspace_api.auth.dto.LoginRequest;
import com.uco.tutorspace_api.auth.dto.RegisterEstudianteRequest;
import com.uco.tutorspace_api.auth.dto.RegisterTutorRequest;
import com.uco.tutorspace_api.auth.jwt.JwtUtil;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new DisabledException("La cuenta está inactiva");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    public AuthResponse registerEstudiante(RegisterEstudianteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.getNombre());
        estudiante.setEmail(request.getEmail());
        estudiante.setPassword(passwordEncoder.encode(request.getPassword()));
        estudiante.setRol(RolUsuario.ESTUDIANTE);

        usuarioRepository.save(estudiante);

        String token = jwtUtil.generateToken(estudiante.getEmail(), estudiante.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(estudiante.getEmail())
                .nombre(estudiante.getNombre())
                .rol(estudiante.getRol().name())
                .build();
    }

    public AuthResponse registerTutor(RegisterTutorRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Tutor tutor = new Tutor();
        tutor.setNombre(request.getNombre());
        tutor.setEmail(request.getEmail());
        tutor.setPassword(passwordEncoder.encode(request.getPassword()));
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setJornadaGeneral(request.getJornadaGeneral());

        usuarioRepository.save(tutor);

        String token = jwtUtil.generateToken(tutor.getEmail(), tutor.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(tutor.getEmail())
                .nombre(tutor.getNombre())
                .rol(tutor.getRol().name())
                .build();
    }
}
