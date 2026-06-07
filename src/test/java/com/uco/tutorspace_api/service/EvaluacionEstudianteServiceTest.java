package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.*;
import com.uco.tutorspace_api.domain.dto.EvaluacionEstudianteRequest;
import com.uco.tutorspace_api.domain.dto.EvaluacionEstudianteResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.repositories.EvaluacionEstudianteRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - EvaluacionEstudianteService")
class EvaluacionEstudianteServiceTest {

    @Mock
    private EvaluacionEstudianteRepository evaluacionRepository;
    @Mock
    private SesionRepository sesionRepository;
    @Mock
    private TutorRepository tutorRepository;

    @InjectMocks
    private EvaluacionEstudianteService evaluacionService;

    private Tutor tutor;
    private Estudiante estudiante;
    private Sesion sesion;
    private EvaluacionEstudiante evaluacion;

    @BeforeEach
    void setUp() {
        // Setup Tutor
        tutor = new Tutor();
        tutor.setId(1L);
        tutor.setNombre("Tutor Test");
        tutor.setEmail("tutor@test.com");

        // Setup Estudiante
        estudiante = new Estudiante();
        estudiante.setId(2L);
        estudiante.setNombre("Estudiante Test");
        estudiante.setEmail("estudiante@test.com");

        // Setup Sesión COMPLETADA
        sesion = new Sesion();
        sesion.setId(1L);
        sesion.setTutor(tutor);
        sesion.setEstudiante(estudiante);
        sesion.setFecha(LocalDate.now().minusDays(1));
        sesion.setHoraInicio(LocalTime.of(9, 0));
        sesion.setHoraFin(LocalTime.of(10, 0));
        sesion.setEstado(EstadoSesion.COMPLETADA);

        // Setup Evaluación
        evaluacion = new EvaluacionEstudiante();
        evaluacion.setId(1L);
        evaluacion.setSesion(sesion);
        evaluacion.setTutor(tutor);
        evaluacion.setEstudiante(estudiante);
        evaluacion.setPuntuacion(5);
        evaluacion.setObservaciones("Excelente desempeño");
        evaluacion.setCreatedAt(LocalDateTime.now());
        evaluacion.setInmutable(true);
    }

    // ============ Tests para Crear Evaluación (RF-03, HU-M13) ============

    @Test
    @DisplayName("RN-01: Evaluar sesión COMPLETADA debe exitoso")
    void evaluarEstudiante_withSesionCompletada_shouldSucceed() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(false);
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class))).thenReturn(evaluacion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Excelente desempeño");
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertNotNull(response);
        assertEquals(5, response.puntuacion());
        assertEquals("Excelente desempeño", response.observaciones());
        verify(evaluacionRepository).save(any(EvaluacionEstudiante.class));
    }

    @Test
    @DisplayName("RN-01: Evaluar sesión PENDIENTE debe lanzar excepción")
    void evaluarEstudiante_withSesionPendiente_shouldThrowException() {
        sesion.setEstado(EstadoSesion.PENDIENTE);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                evaluacionService.evaluarEstudiante(1L, 1L, request)
        );

        assertTrue(exception.getMessage().contains("Solo se pueden evaluar sesiones con estado COMPLETADA"));
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-01: Evaluar sesión CANCELADA debe lanzar excepción")
    void evaluarEstudiante_withSesionCancelada_shouldThrowException() {
        sesion.setEstado(EstadoSesion.CANCELADA);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(3, "No se completó");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                evaluacionService.evaluarEstudiante(1L, 1L, request)
        );

        assertTrue(exception.getMessage().contains("Solo se pueden evaluar sesiones con estado COMPLETADA"));
    }

    @Test
    @DisplayName("RN-03: Solo el tutor participante puede evaluar")
    void evaluarEstudiante_byDifferentTutor_shouldThrowException() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(4, "Observaciones");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                evaluacionService.evaluarEstudiante(1L, 999L, request)
        );

        assertEquals("Solo el tutor participante en la sesión puede evaluar al estudiante",
                exception.getMessage());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-02: Evaluar sesión ya evaluada debe lanzar excepción")
    void evaluarEstudiante_withExistingEvaluacion_shouldThrowException() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(true);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Excelente");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                evaluacionService.evaluarEstudiante(1L, 1L, request)
        );

        assertTrue(exception.getMessage().contains("El estudiante ya ha sido evaluado en esta sesión"));
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-04: Observaciones opcionales - evaluar sin observaciones")
    void evaluarEstudiante_withoutObservaciones_shouldSucceed() {
        evaluacion.setObservaciones(null);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class)))
                .thenAnswer(invocation -> {
                    EvaluacionEstudiante arg = invocation.getArgument(0);
                    // opcional: asignar id/createdAt para simular entidad persistida
                    arg.setId(1L);
                    arg.setCreatedAt(LocalDateTime.now());
                    return arg;
                });

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(4, null);
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertNotNull(response);
        assertEquals(4, response.puntuacion());
        assertNull(response.observaciones());
    }

    @Test
    @DisplayName("RN-04: Observaciones con máximo 500 caracteres")
    void evaluarEstudiante_withValidObservaciones_shouldSucceed() {
        String observacionesLargas = "a".repeat(500);
        evaluacion.setObservaciones(observacionesLargas);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(false);
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class))).thenReturn(evaluacion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(3, observacionesLargas);
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertNotNull(response);
        assertEquals(500, response.observaciones().length());
    }

    // ============ Tests para Puntuación (RF-02) ============

    @Test
    @DisplayName("RF-02: Puntuación mínima 1")
    void evaluarEstudiante_withMinPuntuacion_shouldSucceed() {
        evaluacion.setPuntuacion(1);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(false);
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class))).thenReturn(evaluacion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(1, "Necesita mejorar");
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertEquals(1, response.puntuacion());
    }

    @Test
    @DisplayName("RF-02: Puntuación máxima 5")
    void evaluarEstudiante_withMaxPuntuacion_shouldSucceed() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(false);
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class))).thenReturn(evaluacion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Excelente");
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertEquals(5, response.puntuacion());
    }

    // ============ Tests para Consulta de Evaluaciones (RF-06) ============

    @Test
    @DisplayName("RF-06: Obtener evaluaciones por estudiante")
    void obtenerEvaluacionesPorEstudiante_shouldReturnList() {
        List<EvaluacionEstudiante> evaluaciones = List.of(evaluacion);
        when(evaluacionRepository.findByEstudianteId(2L)).thenReturn(evaluaciones);

        List<EvaluacionEstudianteResponse> response = evaluacionService.obtenerEvaluacionesPorEstudiante(2L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(5, response.get(0).puntuacion());
    }

    @Test
    @DisplayName("RF-06: Obtener promedio de puntuaciones del estudiante")
    void obtenerPromedioPuntuacionesEstudiante_shouldReturnAverage() {
        when(evaluacionRepository.obtenerPromedioPuntuacionesEstudiante(2L)).thenReturn(4.5);

        Double promedio = evaluacionService.obtenerPromedioPuntuacionesEstudiante(2L);

        assertEquals(4.5, promedio);
    }

    @Test
    @DisplayName("RF-06: Contar evaluaciones de estudiante")
    void contarEvaluacionesEstudiante_shouldReturnCount() {
        when(evaluacionRepository.countEvaluacionesByEstudiante(2L)).thenReturn(3L);

        long count = evaluacionService.contarEvaluacionesEstudiante(2L);

        assertEquals(3L, count);
    }

    // ============ Tests para Consultas Adicionales ============

    @Test
    @DisplayName("Obtener evaluación por sesión")
    void obtenerEvaluacionPorSesion_shouldReturnEvaluacion() {
        when(evaluacionRepository.findBySesionId(1L)).thenReturn(Optional.of(evaluacion));

        EvaluacionEstudianteResponse response = evaluacionService.obtenerEvaluacionPorSesion(1L);

        assertNotNull(response);
        assertEquals(5, response.puntuacion());
    }

    @Test
    @DisplayName("Obtener evaluación por sesión inexistente debe lanzar excepción")
    void obtenerEvaluacionPorSesion_notFound_shouldThrowException() {
        when(evaluacionRepository.findBySesionId(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                evaluacionService.obtenerEvaluacionPorSesion(999L)
        );

        assertEquals("No existe evaluación para esta sesión", exception.getMessage());
    }

    @Test
    @DisplayName("Verificar existencia de evaluación")
    void existeEvaluacion_shouldReturnTrue() {
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(true);

        boolean existe = evaluacionService.existeEvaluacion(1L);

        assertTrue(existe);
    }

    @Test
    @DisplayName("Obtener evaluaciones por tutor")
    void obtenerEvaluacionesPorTutor_shouldReturnList() {
        List<EvaluacionEstudiante> evaluaciones = List.of(evaluacion);
        when(evaluacionRepository.findByTutorId(1L)).thenReturn(evaluaciones);

        List<EvaluacionEstudianteResponse> response = evaluacionService.obtenerEvaluacionesPorTutor(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("RNF-03: Evaluación debe ser inmutable")
    void evaluacion_shouldBeInmutable() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(false);
        when(evaluacionRepository.save(any(EvaluacionEstudiante.class))).thenReturn(evaluacion);

        EvaluacionEstudianteRequest request = new EvaluacionEstudianteRequest(5, "Bueno");
        EvaluacionEstudianteResponse response = evaluacionService.evaluarEstudiante(1L, 1L, request);

        assertNotNull(response);
        // Verificamos que la evaluación tenga timestamp de creación
        assertNotNull(response.createdAt());
    }
}
