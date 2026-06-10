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

/**
 * Servicio principal de autenticacion para TutorSpace.
 *
 * <p>Gestiona tres operaciones criticas del ciclo de vida del usuario:
 * <ul>
 *   <li>Login con verificacion de credenciales y estado de cuenta</li>
 *   <li>Registro de estudiantes</li>
 *   <li>Registro de tutores</li>
 * </ul>
 *
 * <p>En todos los casos exitosos se genera un token JWT que el cliente
 * debe enviar en el header {@code Authorization: Bearer <token>} para
 * acceder a los endpoints protegidos.
 */
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

    /**
     * Autentica a un usuario existente y retorna un token JWT.
     *
     * <p>El flujo es:
     * <ol>
     *   <li>Buscar el usuario por email — si no existe lanza {@link BadCredentialsException}.</li>
     *   <li>Comparar la password ingresada con el hash BCrypt almacenado.</li>
     *   <li>Verificar que la cuenta este en estado {@code ACTIVO}.</li>
     *   <li>Generar y retornar un token JWT con el email y rol del usuario.</li>
     * </ol>
     *
     * @param request DTO con email y password del usuario
     * @return {@link AuthResponse} con token JWT, email, nombre y rol
     * @throws BadCredentialsException si el email no existe o la password es incorrecta
     * @throws DisabledException       si la cuenta esta en estado INACTIVO
     */
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email; si no existe se lanza credenciales invalidas
        // (no se distingue entre "no existe" y "password incorrecta" por seguridad)
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar password contra el hash BCrypt almacenado en la BD
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Rechazar cuentas desactivadas por un administrador
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new DisabledException("La cuenta está inactiva");
        }

        // Generar token JWT firmado con email (subject) y rol (claim)
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    /**
     * Registra un nuevo estudiante en el sistema.
     *
     * <p>La cuenta se crea con estado {@code ACTIVO} por defecto y rol {@code ESTUDIANTE}.
     * La password es codificada con BCrypt antes de persistirla.
     * Se retorna un token JWT de inmediato para que el estudiante quede autenticado
     * tras el registro sin necesidad de un login adicional.
     *
     * @param request DTO con nombre, email y password del estudiante
     * @return {@link AuthResponse} con token JWT listo para usar
     * @throws IllegalArgumentException si el email ya esta registrado en el sistema
     */
    public AuthResponse registerEstudiante(RegisterEstudianteRequest request) {
        // Verificar unicidad del email antes de persistir
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Construir entidad con rol y password cifrada
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.getNombre());
        estudiante.setEmail(request.getEmail());
        estudiante.setPassword(passwordEncoder.encode(request.getPassword()));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        // estado = ACTIVO por defecto (definido en la clase Usuario)

        usuarioRepository.save(estudiante);

        String token = jwtUtil.generateToken(estudiante.getEmail(), estudiante.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(estudiante.getEmail())
                .nombre(estudiante.getNombre())
                .rol(estudiante.getRol().name())
                .build();
    }

    /**
     * Registra un nuevo tutor en el sistema.
     *
     * <p>Adicionalmente al flujo de registro base, persiste el campo
     * {@code jornadaGeneral} (ej. "MAÑANA", "TARDE", "NOCHE") que define
     * la disponibilidad horaria general del tutor.
     *
     * @param request DTO con nombre, email, password y jornadaGeneral del tutor
     * @return {@link AuthResponse} con token JWT listo para usar
     * @throws IllegalArgumentException si el email ya esta registrado en el sistema
     */
    public AuthResponse registerTutor(RegisterTutorRequest request) {
        // Verificar unicidad del email antes de persistir
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Construir entidad Tutor con datos especificos del rol
        Tutor tutor = new Tutor();
        tutor.setNombre(request.getNombre());
        tutor.setEmail(request.getEmail());
        tutor.setPassword(passwordEncoder.encode(request.getPassword()));
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setJornadaGeneral(request.getJornadaGeneral());
        // estado = ACTIVO por defecto (definido en la clase Usuario)

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
