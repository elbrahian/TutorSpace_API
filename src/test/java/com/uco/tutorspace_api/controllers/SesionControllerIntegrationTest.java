package com.uco.tutorspace_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uco.tutorspace_api.domain.Chat;
import com.uco.tutorspace_api.domain.Disponibilidad;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.CrearSesionRequest;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.ChatRepository;
import com.uco.tutorspace_api.repositories.DisponibilidadRepository;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
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
@DisplayName("Pruebas de Integración - SesionController")
class SesionControllerIntegrationTest {


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
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String tutorToken;
    private String estudianteToken;
    private Long tutorId;
    private Long estudianteId;
    private Long disponibilidadId;
    private Long chatId;
    private final ObjectMapper objectMapper;

    SesionControllerIntegrationTest() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void setUp() throws Exception {
        chatRepository.deleteAll();
        disponibilidadRepository.deleteAll();
        estudianteRepository.deleteAll();
        tutorRepository.deleteAll();

        Tutor tutor = new Tutor();
        tutor.setNombre("Tutor Test");
        tutor.setEmail("tutor@uco.net.co");
        tutor.setPassword(passwordEncoder.encode("password123"));
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setEstado(EstadoUsuario.ACTIVO);
        Tutor savedTutor = tutorRepository.save(tutor);
        tutorId = savedTutor.getId();

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Estudiante Test");
        estudiante.setEmail("estudiante@uco.net.co");
        estudiante.setPassword(passwordEncoder.encode("password123"));
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        Estudiante savedEstudiante = estudianteRepository.save(estudiante);
        estudianteId = savedEstudiante.getId();

        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setTutor(savedTutor);
        disponibilidad.setDia("LUNES");
        disponibilidad.setHoraInicio(LocalTime.of(9, 0));
        disponibilidad.setHoraFin(LocalTime.of(11, 0));
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        Disponibilidad savedDisponibilidad = disponibilidadRepository.save(disponibilidad);
        disponibilidadId = savedDisponibilidad.getId();

        Chat chat = new Chat();
        chat.setTutor(savedTutor);
        chat.setEstudiante(savedEstudiante);
        Chat savedChat = chatRepository.save(chat);
        chatId = savedChat.getId();

        tutorToken = "Bearer " + authService.login(new LoginRequest("tutor@uco.net.co", "password123")).token();
        estudianteToken = "Bearer " + authService.login(new LoginRequest("estudiante@uco.net.co", "password123")).token();
    }

    @Test
    @DisplayName("crear sesión con token TUTOR debe retornar 201")
    void crearSesion_conTokenTutor_debeRetornar201() throws Exception {
        CrearSesionRequest request = new CrearSesionRequest(
                estudianteId,
                disponibilidadId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        mockMvc.perform(post("/sesiones")
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("crear sesión con token ESTUDIANTE debe retornar 403")
    void crearSesion_conTokenEstudiante_debeRetornar403() throws Exception {
        CrearSesionRequest request = new CrearSesionRequest(
                estudianteId,
                disponibilidadId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        mockMvc.perform(post("/sesiones")
                        .header("Authorization", estudianteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("crear sesión sin token debe retornar 403")
    void crearSesion_sinToken_debeRetornar403() throws Exception {
        CrearSesionRequest request = new CrearSesionRequest(
                estudianteId,
                disponibilidadId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        mockMvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("obtener sesiones por TUTOR debe retornar 200")
    void obtenerSesionesPorTutor_debeRetornar200() throws Exception {
        mockMvc.perform(get("/sesiones/tutor")
                        .header("Authorization", tutorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obtener sesiones por ESTUDIANTE debe retornar 200")
    void obtenerSesionesPorEstudiante_debeRetornar200() throws Exception {
        mockMvc.perform(get("/sesiones/estudiante")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obtener sesiones por rango de fechas debe retornar 200")
    void obtenerSesionesPorRango_debeRetornar200() throws Exception {
        mockMvc.perform(get("/sesiones/estudiante/rango")
                        .header("Authorization", estudianteToken)
                        .param("inicio", LocalDate.now().toString())
                        .param("fin", LocalDate.now().plusMonths(1).toString()))
                .andExpect(status().isOk());
    }
}
