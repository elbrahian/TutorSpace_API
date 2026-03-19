package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("register con datos válidos debe retornar 201")
    void register_withValidData_shouldReturn201() throws Exception {
        String requestBody = """
            {
                "nombre": "Juan Pérez",
                "email": "juan.perez@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("register con email no UCO debe retornar 400")
    void register_withNonUCOEmail_shouldReturn400() throws Exception {
        String requestBody = """
            {
                "nombre": "Juan Pérez",
                "email": "juan.perez@gmail.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solo se permiten correos institucionales con dominio @uco.net.co"));
    }

    @Test
    @DisplayName("register con email duplicado debe retornar 400")
    void register_withDuplicateEmail_shouldReturn400() throws Exception {
        Estudiante existente = new Estudiante();
        existente.setNombre("Usuario Existente");
        existente.setEmail("existente@uco.net.co");
        existente.setPassword(passwordEncoder.encode("password123"));
        existente.setRol(RolUsuario.ESTUDIANTE);
        existente.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(existente);

        String requestBody = """
            {
                "nombre": "Otro Usuario",
                "email": "existente@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe una cuenta registrada con este correo"));
    }

    @Test
    @DisplayName("login con credenciales válidas debe retornar 200 con token")
    void login_withValidCredentials_shouldReturn200WithToken() throws Exception {
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Usuario Test");
        estudiante.setEmail("test@uco.net.co");
        estudiante.setPassword(passwordEncoder.encode("password123"));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(estudiante);

        String requestBody = """
            {
                "email": "test@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));
    }

    @Test
    @DisplayName("login con credenciales incorrectas debe retornar 400")
    void login_withWrongCredentials_shouldReturn400() throws Exception {
        String requestBody = """
            {
                "email": "test@uco.net.co",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("endpoint register debe ser público")
    void register_endpoint_shouldBePublic() throws Exception {
        String requestBody = """
            {
                "nombre": "Test",
                "email": "test@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("endpoint login debe ser público")
    void login_endpoint_shouldBePublic() throws Exception {
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Usuario Test");
        estudiante.setEmail("public@uco.net.co");
        estudiante.setPassword(passwordEncoder.encode("password123"));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(estudiante);

        String requestBody = """
            {
                "email": "public@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
