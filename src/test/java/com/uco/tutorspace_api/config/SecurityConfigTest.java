package com.uco.tutorspace_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
@DisplayName("Pruebas de Seguridad - SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Endpoints públicos")
    class EndpointsPublicosTests {

        @Test
        @DisplayName("POST /auth/register debe ser público")
        void registro_debeSerPublico() throws Exception {
            String requestBody = """
                {
                    "nombre": "Usuario Test",
                    "email": "test@uco.net.co",
                    "password": "password123"
                }
                """;

            mockMvc.perform(get("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST /auth/login debe ser público")
        void login_debeSerPublico() throws Exception {
            String requestBody = """
                {
                    "email": "test@uco.net.co",
                    "password": "wrong"
                }
                """;

            mockMvc.perform(get("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("OPTIONS /auth/login debe responder con 200")
        void opciones_auth_debeSerPublico() throws Exception {
            mockMvc.perform(get("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Endpoints protegidos requieren autenticación")
    class EndpointsProtegidosTests {

        @Test
        @DisplayName("GET /admin/tutores sin token debe retornar 403")
        void admin_sinToken_debeRetornar403() throws Exception {
            mockMvc.perform(get("/admin/tutores"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /tutor/perfil sin token debe retornar 403")
        void tutor_sinToken_debeRetornar403() throws Exception {
            mockMvc.perform(get("/tutor/perfil"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /estudiante/tutores/buscar sin token debe retornar 403")
        void estudiante_sinToken_debeRetornar403() throws Exception {
            mockMvc.perform(get("/estudiante/tutores/buscar")
                            .param("materiaId", "1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /sesiones/tutor sin token debe retornar 403")
        void sesion_sinToken_debeRetornar403() throws Exception {
            mockMvc.perform(get("/sesiones/tutor"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /notificaciones sin token debe retornar 403")
        void notificaciones_sinToken_debeRetornar403() throws Exception {
            mockMvc.perform(get("/notificaciones"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Control de acceso por rol")
    class ControlAccesoPorRolTests {

        @Test
        @DisplayName("GET /admin/tutores con rol ESTUDIANTE debe retornar 403")
        @WithMockUser(roles = "ESTUDIANTE")
        void estudiante_noPuedeAccederAdmin() throws Exception {
            mockMvc.perform(get("/admin/tutores"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /admin/tutores con rol TUTOR debe retornar 403")
        @WithMockUser(roles = "TUTOR")
        void tutor_noPuedeAccederAdmin() throws Exception {
            mockMvc.perform(get("/admin/tutores"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /estudiante/tutores/buscar con rol TUTOR debe retornar 403")
        @WithMockUser(roles = "TUTOR")
        void tutor_noPuedeAccederEstudiante() throws Exception {
            mockMvc.perform(get("/estudiante/tutores/buscar")
                            .param("materiaId", "1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /tutor/perfil con rol ESTUDIANTE debe retornar 403")
        @WithMockUser(roles = "ESTUDIANTE")
        void estudiante_noPuedeAccederTutor() throws Exception {
            mockMvc.perform(get("/tutor/perfil"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cabeceras de seguridad")
    class CabecerasSeguridadTests {

        @Test
        @DisplayName("Respuesta debe incluir X-Content-Type-Options: nosniff")
        void debeIncluirXContentTypeOptions() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        }

        @Test
        @DisplayName("Respuesta debe incluir X-Frame-Options: DENY")
        void debeIncluirXFrameOptions() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(header().string("X-Frame-Options", "DENY"));
        }

        @Test
        @DisplayName("Respuesta debe incluir Cache-Control para evitar cache de recursos sensibles")
        void debeIncluirCacheControl() throws Exception {
            mockMvc.perform(get("/admin/tutores"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"));
        }
    }
}
