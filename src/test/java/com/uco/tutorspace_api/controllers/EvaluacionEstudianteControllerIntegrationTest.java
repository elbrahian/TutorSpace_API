package com.uco.tutorspace_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uco.tutorspace_api.domain.*;
import com.uco.tutorspace_api.domain.dto.EvaluacionEstudianteRequest;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.*;
import com.uco.tutorspace_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración - EvaluacionEstudianteController")
class EvaluacionEstudianteControllerIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private SesionRepository sesionRepository;
    @Autowired
    private EvaluacionEstudianteRepository evaluacionRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private String tutorToken;
    private String estudianteToken;
    private Long tutorId;
    private Long estudianteId;
    private Long sesionId;

    private final ObjectMapper objectMapper;

    EvaluacionEstudianteControllerIntegrationTest() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar datos
        evaluacionRepository.deleteAll();
        sesionRepository.deleteAll();
        chatRepository.deleteAll();
        disponibilidadRepository.deleteAll();
        estudianteRepository.deleteAll();
        tutorRepository.deleteAll();

        // Crear Tutor
        Tutor tutor = new Tutor();
        tutor.setNombre("Tutor Evaluador");
        tutor.setEmail("tutor@test.com");
        tutor.setPassword(passwordEncoder.encode("password123"));
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setEstado(EstadoUsuario.ACTIVO);
        Tutor savedTutor = tutorRepository.save(tutor);
        tutorId = savedTutor.getId();

        // Crear Estudiante
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Estudiante Evaluado");
        estudiante.setEmail("estudiante@test.com");
        estudiante.setPassword(passwordEncoder.encode("password123"));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        Estudiante savedEstudiante = estudianteRepository.save(estudiante);
        estudianteId = savedEstudiante.getId();

        // Crear Chat
        Chat chat = new Chat();
        chat.setTutor(savedTutor);
        chat.setEstudiante(savedEstudiante);
        chatRepository.save(chat);

        // Crear Disponibilidad
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setTutor(savedTutor);
        disponibilidad.setDia("LUNES");
        disponibilidad.setHoraInicio(LocalTime.of(9, 0));
        disponibilidad.setHoraFin(LocalTime.of(11, 0));
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        Disponibilidad savedDisponibilidad = disponibilidadRepository.save(disponibilidad);

        // Crear Sesión con estado COMPLETADA
        Sesion sesion = new Sesion();
        sesion.setTutor(savedTutor);
        sesion.setEstudiante(savedEstudiante);
        sesion.setDisponibilidad(savedDisponibilidad);
        sesion.setFecha(LocalDate.now().minusDays(1));
        sesion.setHoraInicio(LocalTime.of(9, 0));
        sesion.setHoraFin(LocalTime.of(10, 0));
        sesion.setEstado(EstadoSesion.COMPLETADA);
        Sesion savedSesion = sesionRepository.save(sesion);
        sesionId = savedSesion.getId();

        // Obtener tokens
        tutorToken = "Bearer " + authService.login(new LoginRequest("tutor@test.com", "password123")).token();
        estudianteToken = "Bearer " + authService.login(new LoginRequest("estudiante@test.com", "password123")).token();
    }

    // ============ Tests POST /sesiones/{id}/evaluacion-estudiante (RF-03) ============

    @Test
    @DisplayName("RF-03: Tutor evalúa sesión COMPLETADA debe retornar 201")
    void evaluarEstudiante_conTutorToken_debeRetornar201() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(
                5,
                "Excelente desempeño y participación"
        );

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion").value(5))
                .andExpect(jsonPath("$.observaciones").value("Excelente desempeño y participación"))
                .andExpect(jsonPath("$.estudianteNombre").value("Estudiante Evaluado"))
                .andExpect(jsonPath("$.tutorNombre").value("Tutor Evaluador"));
    }

    @Test
    @DisplayName("RF-02: Evaluar con puntuación 1 debe exitoso")
    void evaluarEstudiante_conPuntuacion1_debeExitoso() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(1, "Necesita mejorar");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion").value(1));
    }

    @Test
    @DisplayName("RF-02: Evaluar con puntuación 5 debe exitoso")
    void evaluarEstudiante_conPuntuacion5_debeExitoso() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Excelente");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion").value(5));
    }

    @Test
    @DisplayName("RN-04: Observaciones opcionales debe exitoso sin observaciones")
    void evaluarEstudiante_sinObservaciones_debeExitoso() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(4, null);

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion").value(4));
    }

    @Test
    @DisplayName("RNF-01: Estudiante intenta evaluar debe retornar 403 FORBIDDEN")
    void evaluarEstudiante_conEstudianteToken_debeRetornar403() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RNF-01: Sin token debe retornar 403 FORBIDDEN")
    void evaluarEstudiante_sinToken_debeRetornar403() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RN-05: Evaluar sesión PENDIENTE debe retornar 400 con mensaje de error")
    void evaluarEstudiante_sesionPendiente_debeRetornar500() throws Exception {
        // Cambiar sesión a PENDIENTE
        Sesion sesion = sesionRepository.findById(sesionId).orElseThrow();
        sesion.setEstado(EstadoSesion.PENDIENTE);
        sesionRepository.save(sesion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RN-02: Evaluar dos veces la misma sesión debe retornar error")
    void evaluarEstudiante_dobleEvaluacion_debeRetornarError() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Excelente");

        // Primera evaluación
        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda evaluación (debe fallar) -> ahora esperamos 400 por RuntimeException manejada
        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RN-03: Solo el tutor participante puede evaluar")
    void evaluarEstudiante_tutorDiferente_debeRetornarError() throws Exception {
        // Crear otro tutor
        Tutor otroTutor = new Tutor();
        otroTutor.setNombre("Otro Tutor");
        otroTutor.setEmail("otrotutor@test.com");
        otroTutor.setPassword(passwordEncoder.encode("password123"));
        otroTutor.setRol(RolUsuario.TUTOR);
        otroTutor.setEstado(EstadoUsuario.ACTIVO);
        Tutor savedOtroTutor = tutorRepository.save(otroTutor);

        // Token del otro tutor
        String otroTutorToken = "Bearer " + authService.login(
                new LoginRequest("otrotutor@test.com", "password123")).token();

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", otroTutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============ Tests GET /sesiones/{id}/evaluacion-estudiante (RF-06) ============

    @Test
    @DisplayName("RF-06: Obtener evaluación de sesión debe retornar 200")
    void obtenerEvaluacion_debeRetornar200() throws Exception {
        // Primero crear la evaluación
        EvaluacionEstudianteRequest requestEval = new EvaluacionEstudianteRequest(5, "Excelente");
        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEval)))
                .andExpect(status().isCreated());

        // Luego obtenerla -> incluir token porque el endpoint requiere autenticación
        mockMvc.perform(get("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntuacion").value(5))
                .andExpect(jsonPath("$.observaciones").value("Excelente"));
    }

    @Test
    @DisplayName("RF-06: Obtener evaluación inexistente debe retornar error")
    void obtenerEvaluacion_inexistente_debeRetornarError() throws Exception {
        // Añadimos token y esperamos 400 (RuntimeException -> GlobalExceptionHandler -> 400)
        mockMvc.perform(get("/sesiones/{id}/evaluacion-estudiante", 999L)
                        .header("Authorization", tutorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validación: Puntuación fuera de rango debe retornar 400")
    void evaluarEstudiante_puntuacionInvalida_debeRetornar400() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(6, "Inválido");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validación: Puntuación 0 debe retornar 400")
    void evaluarEstudiante_puntuacion0_debeRetornar400() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(0, "Inválido");

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validación: Observaciones > 500 caracteres debe retornar 400")
    void evaluarEstudiante_observacionesMuyLargas_debeRetornar400() throws Exception {
        String observacionesLargas = "a".repeat(501);
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, observacionesLargas);

        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", sesionId)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sesión inexistente debe retornar error")
    void evaluarEstudiante_sesionInexistente_debeRetornarError() throws Exception {
        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");

        // Esperar 400 (sesión no encontrada -> RuntimeException -> 400)
        mockMvc.perform(post("/sesiones/{id}/evaluacion-estudiante", 999L)
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
