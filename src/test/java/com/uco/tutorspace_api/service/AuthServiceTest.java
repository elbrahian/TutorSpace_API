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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registroValido;
    private LoginRequest loginValido;
    private Estudiante estudianteMock;

    @BeforeEach
    void setUp() {
        registroValido = new RegisterRequest("Juan Pérez", "juan.perez@uco.net.co", "password123");
        loginValido = new LoginRequest("juan.perez@uco.net.co", "password123");
        
        estudianteMock = new Estudiante();
        estudianteMock.setId(1L);
        estudianteMock.setNombre("Juan Pérez");
        estudianteMock.setEmail("juan.perez@uco.net.co");
        estudianteMock.setPassword("hashedPassword");
        estudianteMock.setRol(RolUsuario.ESTUDIANTE);
        estudianteMock.setEstado(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("registrarEstudiante con email UCO válido debe retornar token")
    void registrarEstudiante_withValidUCOEmail_shouldReturnToken() {
        when(emailValidator.esCorreoInstitucional(anyString())).thenReturn(true);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteMock);
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn("jwt-token-123");

        AuthResponse response = authService.registrarEstudiante(registroValido);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.token());
        assertEquals("ESTUDIANTE", response.rol());
        assertEquals("Juan Pérez", response.nombre());
        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("registrarEstudiante debe asignar rol ESTUDIANTE")
    void registrarEstudiante_shouldAssignEstudianteRole() {
        when(emailValidator.esCorreoInstitucional(anyString())).thenReturn(true);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocation -> {
            Estudiante e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn("token");

        AuthResponse response = authService.registrarEstudiante(registroValido);

        assertEquals("ESTUDIANTE", response.rol());
        verify(estudianteRepository).save(argThat(e -> e.getRol() == RolUsuario.ESTUDIANTE));
    }

    @Test
    @DisplayName("login con credenciales válidas debe retornar token")
    void login_withValidCredentials_shouldReturnToken() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(estudianteMock));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn("jwt-token-123");

        AuthResponse response = authService.login(loginValido);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.token());
    }

    @Test
    @DisplayName("login debe retornar rol correcto")
    void login_shouldReturnCorrectRole() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(estudianteMock));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn("token");

        AuthResponse response = authService.login(loginValido);

        assertEquals("ESTUDIANTE", response.rol());
    }

    @Test
    @DisplayName("registrarEstudiante con email no UCO debe lanzar excepción")
    void registrarEstudiante_withNonUCOEmail_shouldThrowException() {
        when(emailValidator.esCorreoInstitucional(anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.registrarEstudiante(registroValido)
        );

        assertEquals("Solo se permiten correos institucionales con dominio @uco.net.co", exception.getMessage());
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarEstudiante con email duplicado debe lanzar excepción")
    void registrarEstudiante_withDuplicateEmail_shouldThrowException() {
        when(emailValidator.esCorreoInstitucional(anyString())).thenReturn(true);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.registrarEstudiante(registroValido)
        );

        assertEquals("Ya existe una cuenta registrada con este correo", exception.getMessage());
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("login con contraseña incorrecta debe lanzar excepción")
    void login_withWrongPassword_shouldThrowException() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(estudianteMock));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.login(loginValido)
        );

        assertEquals("Correo o contraseña incorrectos", exception.getMessage());
    }

    @Test
    @DisplayName("login con usuario inactivo debe lanzar excepción")
    void login_withInactiveUser_shouldThrowException() {
        estudianteMock.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(estudianteMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.login(loginValido)
        );

        assertEquals("Tu cuenta está desactivada, contacta al administrador", exception.getMessage());
    }

    @Test
    @DisplayName("login con email inexistente debe lanzar excepción")
    void login_withNonexistentEmail_shouldThrowException() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            authService.login(loginValido)
        );

        assertEquals("Correo o contraseña incorrectos", exception.getMessage());
    }
}
