package com.uco.tutorspace_api.auth.service;

import com.uco.tutorspace_api.auth.dto.AuthResponse;
import com.uco.tutorspace_api.auth.dto.LoginRequest;
import com.uco.tutorspace_api.auth.dto.RegisterEstudianteRequest;
import com.uco.tutorspace_api.auth.dto.RegisterTutorRequest;
import com.uco.tutorspace_api.auth.jwt.JwtUtil;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Estudiante crearEstudianteActivo(String email) {
        Estudiante e = new Estudiante();
        e.setEmail(email);
        e.setPassword("encodedPass");
        e.setNombre("Test User");
        e.setRol(RolUsuario.ESTUDIANTE);
        return e;
    }

    // ---- LOGIN ----

    @Test
    void login_credencialesValidas_retornaAuthResponse() {
        Estudiante usuario = crearEstudianteActivo("user@test.com");
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("rawPass");

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("user@test.com", "ESTUDIANTE")).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getRol()).isEqualTo("ESTUDIANTE");
        assertThat(response.getNombre()).isEqualTo("Test User");
    }

    @Test
    void login_passwordIncorrecta_lanzaBadCredentials() {
        Estudiante usuario = crearEstudianteActivo("user@test.com");
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("wrongPass");

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void login_emailNoExiste_lanzaBadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("noexiste@test.com");
        req.setPassword("password");

        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void login_cuentaInactiva_lanzaDisabled() {
        Estudiante usuario = crearEstudianteActivo("user@test.com");
        usuario.setEstado(EstadoUsuario.INACTIVO);
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("rawPass");

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(DisabledException.class)
                .hasMessage("La cuenta está inactiva");
    }

    // ---- REGISTER ESTUDIANTE ----

    @Test
    void registerEstudiante_emailNuevo_guardaYRetornaToken() {
        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("Nuevo Estudiante");
        req.setEmail("nuevo@test.com");
        req.setPassword("password123");

        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("nuevo@test.com", "ESTUDIANTE")).thenReturn("jwt-token");

        AuthResponse response = authService.registerEstudiante(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("nuevo@test.com");
        assertThat(response.getRol()).isEqualTo("ESTUDIANTE");
        verify(usuarioRepository).save(any(Estudiante.class));
    }

    @Test
    void registerEstudiante_emailDuplicado_lanzaIllegalArgument() {
        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("Estudiante");
        req.setEmail("existente@test.com");
        req.setPassword("password123");

        when(usuarioRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerEstudiante(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado");

        verify(usuarioRepository, never()).save(any());
    }

    // ---- REGISTER TUTOR ----

    @Test
    void registerTutor_emailNuevo_guardaYRetornaToken() {
        RegisterTutorRequest req = new RegisterTutorRequest();
        req.setNombre("Nuevo Tutor");
        req.setEmail("tutor@test.com");
        req.setPassword("password123");
        req.setJornadaGeneral("MAÑANA");

        when(usuarioRepository.existsByEmail("tutor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("tutor@test.com", "TUTOR")).thenReturn("jwt-token");

        AuthResponse response = authService.registerTutor(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("tutor@test.com");
        assertThat(response.getRol()).isEqualTo("TUTOR");
        verify(usuarioRepository).save(any(Tutor.class));
    }

    @Test
    void registerTutor_emailDuplicado_lanzaIllegalArgument() {
        RegisterTutorRequest req = new RegisterTutorRequest();
        req.setNombre("Tutor");
        req.setEmail("existente@test.com");
        req.setPassword("password123");

        when(usuarioRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerTutor(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado");

        verify(usuarioRepository, never()).save(any());
    }
}
