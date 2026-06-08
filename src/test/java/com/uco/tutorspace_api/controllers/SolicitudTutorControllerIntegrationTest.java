package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.Administrador;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.SolicitudTutor;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import com.uco.tutorspace_api.repositories.SolicitudTutorRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import com.uco.tutorspace_api.service.AuthService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SolicitudTutorControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private AuthService authService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MateriaRepository materiaRepository;
    @Autowired private SolicitudTutorRepository solicitudTutorRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager entityManager;

    private MockMvc mockMvc;
    private String adminToken;
    private String estudianteToken;
    private Long estudianteId;
    private Long materiaId;
    private Long segundaMateriaId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Administrador admin = new Administrador();
        admin.setNombre("Admin Solicitudes");
        admin.setEmail("admin.solicitudes@uco.net.co");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(admin);

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Estudiante Solicitudes");
        estudiante.setEmail("estudiante.solicitudes@uco.net.co");
        estudiante.setPassword(passwordEncoder.encode("password123"));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        estudianteId = usuarioRepository.save(estudiante).getId();

        Materia materia = new Materia();
        materia.setNombre("Cálculo");
        materia.setCodigo("CAL-" + estudianteId);
        materiaId = materiaRepository.save(materia).getId();

        Materia segundaMateria = new Materia();
        segundaMateria.setNombre("Programación");
        segundaMateria.setCodigo("PROG-" + estudianteId);
        segundaMateriaId = materiaRepository.save(segundaMateria).getId();

        adminToken = "Bearer " + authService.login(
                new LoginRequest("admin.solicitudes@uco.net.co", "password123")
        ).token();
        estudianteToken = "Bearer " + authService.login(
                new LoginRequest("estudiante.solicitudes@uco.net.co", "password123")
        ).token();
    }

    @Test
    @DisplayName("crear solicitud con token ESTUDIANTE debe retornar 201")
    void crearSolicitud_withStudentToken_shouldReturn201() throws Exception {
        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSolicitudJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitanteId").value(estudianteId))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.materias.length()").value(2));
    }

    @Test
    @DisplayName("crear solicitud con token ADMIN debe retornar 403")
    void crearSolicitud_withAdminToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSolicitudJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("crear solicitud sin token debe retornar 403")
    void crearSolicitud_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/solicitudes-tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSolicitudJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("crear solicitud sin materias debe retornar 400")
    void crearSolicitud_withoutSubjects_shouldReturn400() throws Exception {
        String requestBody = """
                {
                  "materiaIds": [],
                  "justificacion": "Quiero ser tutor"
                }
                """;

        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.materiaIds").value("Debe seleccionar al menos una materia"));
    }

    @Test
    @DisplayName("crear solicitud sin justificación debe retornar 400")
    void crearSolicitud_withoutJustification_shouldReturn400() throws Exception {
        String requestBody = """
                {
                  "materiaIds": [%d],
                  "justificacion": " "
                }
                """.formatted(materiaId);

        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.justificacion").exists());
    }

    @Test
    @DisplayName("crear segunda solicitud pendiente debe retornar 400")
    void crearSolicitud_withExistingPendingRequest_shouldReturn400() throws Exception {
        crearSolicitud();

        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSolicitudJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ya existe una solicitud pendiente para este usuario"));
    }

    @Test
    @DisplayName("listar solicitudes con token ADMIN debe retornar 200")
    void listarSolicitudes_withAdminToken_shouldReturn200() throws Exception {
        crearSolicitud();

        mockMvc.perform(get("/solicitudes-tutor")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].materias.length()").value(2));
    }

    @Test
    @DisplayName("listar solicitudes con filtros debe retornar 200")
    void listarSolicitudes_withFilters_shouldReturn200() throws Exception {
        crearSolicitud();

        mockMvc.perform(get("/solicitudes-tutor")
                        .header("Authorization", adminToken)
                        .param("estado", "PENDIENTE")
                        .param("inicio", "2026-01-01")
                        .param("fin", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("listar solicitudes con token ESTUDIANTE debe retornar 403")
    void listarSolicitudes_withStudentToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/solicitudes-tutor")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("listar solicitudes con rango inválido debe retornar 400")
    void listarSolicitudes_withInvalidDateRange_shouldReturn400() throws Exception {
        mockMvc.perform(get("/solicitudes-tutor")
                        .header("Authorization", adminToken)
                        .param("inicio", "2026-12-31")
                        .param("fin", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("La fecha de inicio no puede ser posterior a la fecha fin"));
    }

    @Test
    @DisplayName("consultar mi solicitud con token ESTUDIANTE debe retornar estado actualizado")
    void obtenerMiSolicitud_withStudentToken_shouldReturnUpdatedStatus() throws Exception {
        Long solicitudId = crearSolicitud();
        solicitudTutorRepository.findById(solicitudId).ifPresent(solicitud -> {
            solicitud.setEstado(EstadoSolicitudTutor.RECHAZADA);
            solicitud.setObservaciones("Solicitud rechazada");
            solicitudTutorRepository.save(solicitud);
        });

        mockMvc.perform(get("/solicitudes-tutor/mia")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitudId))
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.observaciones").value("Solicitud rechazada"));
    }

    @Test
    @DisplayName("consultar mi solicitud sin solicitudes debe retornar 404")
    void obtenerMiSolicitud_withoutRequests_shouldReturn404() throws Exception {
        mockMvc.perform(get("/solicitudes-tutor/mia")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("consultar mi solicitud con token ADMIN debe retornar 403")
    void obtenerMiSolicitud_withAdminToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/solicitudes-tutor/mia")
                        .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("consultar mi solicitud después de aprobación debe retornar APROBADA")
    void obtenerMiSolicitud_afterApproval_shouldReturnApproved() throws Exception {
        Long solicitudId = crearSolicitud();

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nuevoEstado": "APROBADA",
                                  "observaciones": "Solicitud aprobada"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/solicitudes-tutor/mia")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.observaciones").value("Solicitud aprobada"));
    }

    @Test
    @DisplayName("aprobar solicitud debe promover tutor y asignar materias")
    void revisarSolicitud_approved_shouldPromoteTutorAndAssignSubjects() throws Exception {
        Long solicitudId = crearSolicitud();
        String requestBody = """
                {
                  "nuevoEstado": "APROBADA",
                  "observaciones": "Cumple los criterios"
                }
                """;

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.observaciones").value("Cumple los criterios"));

        Integer tutores = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tutores WHERE id = ?",
                Integer.class,
                estudianteId
        );
        Integer materiasAsignadas = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tutor_materia WHERE tutor_id = ?",
                Integer.class,
                estudianteId
        );
        String rol = jdbcTemplate.queryForObject(
                "SELECT rol FROM usuarios WHERE id = ?",
                String.class,
                estudianteId
        );

        assert tutores != null;
        assert materiasAsignadas != null;
        org.junit.jupiter.api.Assertions.assertEquals(1, tutores);
        org.junit.jupiter.api.Assertions.assertEquals(2, materiasAsignadas);
        org.junit.jupiter.api.Assertions.assertEquals("TUTOR", rol);
    }

    @Test
    @DisplayName("rechazar solicitud no debe promover usuario")
    void revisarSolicitud_rejected_shouldNotPromoteUser() throws Exception {
        Long solicitudId = crearSolicitud();
        String requestBody = """
                {
                  "nuevoEstado": "RECHAZADA",
                  "observaciones": null
                }
                """;

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        Integer tutores = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tutores WHERE id = ?",
                Integer.class,
                estudianteId
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, tutores);
    }

    @Test
    @DisplayName("revisar solicitud con token ESTUDIANTE debe retornar 403")
    void revisarSolicitud_withStudentToken_shouldReturn403() throws Exception {
        Long solicitudId = crearSolicitud();

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nuevoEstado": "APROBADA",
                                  "observaciones": null
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("revisar solicitud sin estado debe retornar 400")
    void revisarSolicitud_withoutState_shouldReturn400() throws Exception {
        Long solicitudId = crearSolicitud();

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "observaciones": "Sin estado"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nuevoEstado").value("El nuevo estado es requerido"));
    }

    @Test
    @DisplayName("revisar solicitud manteniendo PENDIENTE debe retornar 400")
    void revisarSolicitud_withPendingState_shouldReturn400() throws Exception {
        Long solicitudId = crearSolicitud();

        mockMvc.perform(patch("/solicitudes-tutor/" + solicitudId + "/revision")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nuevoEstado": "PENDIENTE",
                                  "observaciones": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("La revisión debe aprobar o rechazar la solicitud"));
    }

    private Long crearSolicitud() throws Exception {
        mockMvc.perform(post("/solicitudes-tutor")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearSolicitudJson()))
                .andExpect(status().isCreated());

        entityManager.flush();
        SolicitudTutor solicitud = solicitudTutorRepository.findAll().getFirst();
        return solicitud.getId();
    }

    private String crearSolicitudJson() {
        return """
                {
                  "materiaIds": [%d, %d],
                  "justificacion": "Tengo conocimientos para impartir estas materias"
                }
                """.formatted(materiaId, segundaMateriaId);
    }
}
