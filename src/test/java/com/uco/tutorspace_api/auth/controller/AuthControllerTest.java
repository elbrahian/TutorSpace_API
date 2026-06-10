package com.uco.tutorspace_api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uco.tutorspace_api.auth.dto.AuthResponse;
import com.uco.tutorspace_api.auth.dto.LoginRequest;
import com.uco.tutorspace_api.auth.dto.RegisterEstudianteRequest;
import com.uco.tutorspace_api.auth.dto.RegisterTutorRequest;
import com.uco.tutorspace_api.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthResponse buildResponse(String email, String nombre, String rol) {
        return AuthResponse.builder()
                .token("test-token-123")
                .email(email)
                .nombre(nombre)
                .rol(rol)
                .build();
    }

    // ---- LOGIN ----

    @Test
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        when(authService.login(any())).thenReturn(buildResponse("user@test.com", "Usuario", "ESTUDIANTE"));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token-123"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));
    }

    @Test
    void login_credencialesInvalidas_retorna401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciales inválidas"));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void login_cuentaInactiva_retorna403() throws Exception {
        when(authService.login(any())).thenThrow(new DisabledException("La cuenta está inactiva"));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("La cuenta está inactiva"));
    }

    @Test
    void login_emailBlanco_retorna400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_emailFormatoInvalido_retorna400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("no-es-un-email");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---- REGISTER ESTUDIANTE ----

    @Test
    void registerEstudiante_datosValidos_retorna201() throws Exception {
        when(authService.registerEstudiante(any()))
                .thenReturn(buildResponse("est@test.com", "Estudiante Test", "ESTUDIANTE"));

        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("Estudiante Test");
        req.setEmail("est@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/register/estudiante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token-123"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));
    }

    @Test
    void registerEstudiante_emailDuplicado_retorna409() throws Exception {
        when(authService.registerEstudiante(any()))
                .thenThrow(new IllegalArgumentException("El email ya está registrado"));

        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("Estudiante");
        req.setEmail("dup@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/register/estudiante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }

    @Test
    void registerEstudiante_passwordCorta_retorna400() throws Exception {
        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("Estudiante");
        req.setEmail("test@test.com");
        req.setPassword("123");

        mockMvc.perform(post("/auth/register/estudiante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEstudiante_nombreBlanco_retorna400() throws Exception {
        RegisterEstudianteRequest req = new RegisterEstudianteRequest();
        req.setNombre("");
        req.setEmail("test@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/register/estudiante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---- REGISTER TUTOR ----

    @Test
    void registerTutor_datosValidos_retorna201() throws Exception {
        when(authService.registerTutor(any()))
                .thenReturn(buildResponse("tutor@test.com", "Tutor Test", "TUTOR"));

        RegisterTutorRequest req = new RegisterTutorRequest();
        req.setNombre("Tutor Test");
        req.setEmail("tutor@test.com");
        req.setPassword("password123");
        req.setJornadaGeneral("MAÑANA");

        mockMvc.perform(post("/auth/register/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token-123"))
                .andExpect(jsonPath("$.rol").value("TUTOR"));
    }

    @Test
    void registerTutor_emailDuplicado_retorna409() throws Exception {
        when(authService.registerTutor(any()))
                .thenThrow(new IllegalArgumentException("El email ya está registrado"));

        RegisterTutorRequest req = new RegisterTutorRequest();
        req.setNombre("Tutor");
        req.setEmail("dup@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/auth/register/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerTutor_passwordCorta_retorna400() throws Exception {
        RegisterTutorRequest req = new RegisterTutorRequest();
        req.setNombre("Tutor");
        req.setEmail("tutor@test.com");
        req.setPassword("123");

        mockMvc.perform(post("/auth/register/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
