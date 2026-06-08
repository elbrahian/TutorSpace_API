package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.Administrador;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class   AdminControllerIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private MateriaRepository materiaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


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

    private String adminToken;
    private String tutorToken;
    private String estudianteToken;
    private Long tutorId;
    private Long materiaId;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        
        Administrador admin = new Administrador();
        admin.setNombre("Admin Test");
        admin.setEmail("admin@uco.net.co");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(admin);

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
        usuarioRepository.save(estudiante);

        Materia materia = new Materia();
        materia.setNombre("Matemáticas");
        materia.setCodigo("MAT101");
        Materia savedMateria = materiaRepository.save(materia);
        materiaId = savedMateria.getId();

        adminToken = "Bearer " + authService.login(
                new com.uco.tutorspace_api.domain.dto.LoginRequest("admin@uco.net.co", "password123")).token();
        tutorToken = "Bearer " + authService.login(
                new com.uco.tutorspace_api.domain.dto.LoginRequest("tutor@uco.net.co", "password123")).token();
        estudianteToken = "Bearer " + authService.login(
                new com.uco.tutorspace_api.domain.dto.LoginRequest("estudiante@uco.net.co", "password123")).token();
    }

    @Test
    @DisplayName("getTutores con token ADMIN debe retornar 200")
    void getTutores_withAdminToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/admin/tutores")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getTutores con token TUTOR debe retornar 403")
    void getTutores_withTutorToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/tutores")
                        .header("Authorization", tutorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getTutores con token ESTUDIANTE debe retornar 403")
    void getTutores_withEstudianteToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/tutores")
                        .header("Authorization", estudianteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getTutores sin token debe retornar 403")
    void getTutores_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/tutores"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createTutor con token ADMIN debe retornar 201")
    void createTutor_withAdminToken_shouldReturn201() throws Exception {
        String requestBody = """
            {
                "nombre": "Nuevo Tutor",
                "email": "nuevo.tutor@uco.net.co",
                "password": "password123",
                "jornadaGeneral": "MANANA"
            }
            """;

        mockMvc.perform(post("/admin/tutores")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Nuevo Tutor"));
    }

    @Test
    @DisplayName("createTutor con token TUTOR debe retornar 403")
    void createTutor_withTutorToken_shouldReturn403() throws Exception {
        String requestBody = """
            {
                "nombre": "Nuevo Tutor",
                "email": "nuevo.tutor@uco.net.co",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/admin/tutores")
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("asignarMateria con token ADMIN debe retornar 200")
    void asignarMateria_withAdminToken_shouldReturn200() throws Exception {
        String requestBody = """
            {
                "materiaId": """ + materiaId + """
            }
            """;

        mockMvc.perform(post("/admin/tutores/" + tutorId + "/materias")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("desactivarTutor con token ADMIN debe retornar 200")
    void desactivarTutor_withAdminToken_shouldReturn200() throws Exception {
        mockMvc.perform(patch("/admin/tutores/" + tutorId + "/desactivar")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }
}
