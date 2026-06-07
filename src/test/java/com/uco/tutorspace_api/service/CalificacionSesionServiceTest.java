package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionRequest;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.exceptions.CalificacionTutoriaException;
import com.uco.tutorspace_api.repositories.CalificacionSesionRepository;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionSesionServiceTest {

    @Mock private CalificacionSesionRepository calificacionSesionRepository;
    @Mock private SesionRepository sesionRepository;
    @Mock private EstudianteRepository estudianteRepository;

    @InjectMocks
    private CalificacionSesionService calificacionSesionService;

    private static final Long ESTUDIANTE_ID = 2L;
    private static final Long SESION_ID = 1L;

    private Tutor tutorMock;
    private Estudiante estudianteMock;
    private Sesion sesionMock;
    private CalificacionSesionRequest request;

    @BeforeEach
    void setUp() {
        tutorMock = new Tutor();
        tutorMock.setId(1L);
        tutorMock.setNombre("Tutor Test");

        estudianteMock = new Estudiante();
        estudianteMock.setId(ESTUDIANTE_ID);
        estudianteMock.setNombre("Estudiante Test");

        sesionMock = new Sesion();
        sesionMock.setId(SESION_ID);
        sesionMock.setTutor(tutorMock);
        sesionMock.setEstudiante(estudianteMock);
        sesionMock.setFecha(LocalDate.now().minusDays(1));
        sesionMock.setHoraInicio(LocalTime.of(8, 0));
        sesionMock.setHoraFin(LocalTime.of(10, 0));
        sesionMock.setEstado(EstadoSesion.COMPLETADA);

        request = new CalificacionSesionRequest(4, "Excelente tutor, mucha paciencia.");
    }

    @Test
    @DisplayName("calificarSesion sobre sesión COMPLETADA debe persistir y devolver confirmación")
    void calificarSesion_sesionCompletada_debePersistirYConfirmar() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));
        when(calificacionSesionRepository.existsBySesionIdAndEstudianteId(SESION_ID, ESTUDIANTE_ID))
                .thenReturn(false);
        when(estudianteRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudianteMock));
        when(calificacionSesionRepository.save(any(CalificacionSesion.class)))
                .thenAnswer(invocation -> {
                    CalificacionSesion c = invocation.getArgument(0);
                    c.setId(10L);
                    return c;
                });

        CalificacionSesionResponse response =
                calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, request);

        assertNotNull(response);
        assertEquals(10L, response.calificacionId());
        assertEquals(SESION_ID, response.sesionId());
        assertEquals("Tutor Test", response.nombreTutor());
        assertEquals(4, response.calificacion());
        assertEquals("Excelente tutor, mucha paciencia.", response.comentario());
        assertEquals(sesionMock.getFecha(), response.fecha());
        assertEquals(sesionMock.getHoraInicio(), response.horaInicio());
        assertNotNull(response.mensaje());
        verify(calificacionSesionRepository).save(any(CalificacionSesion.class));
    }

    @Test
    @DisplayName("calificarSesion debe guardar la calificación asociada a la sesión y al estudiante")
    void calificarSesion_debeAsociarSesionYEstudiante() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));
        when(calificacionSesionRepository.existsBySesionIdAndEstudianteId(SESION_ID, ESTUDIANTE_ID))
                .thenReturn(false);
        when(estudianteRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudianteMock));
        when(calificacionSesionRepository.save(any(CalificacionSesion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, request);

        verify(calificacionSesionRepository).save(argThat(c ->
                c.getSesion().equals(sesionMock)
                        && c.getEstudiante().equals(estudianteMock)
                        && c.getCalificacion() == 4
                        && "Excelente tutor, mucha paciencia.".equals(c.getComentario())
        ));
    }

    @Test
    @DisplayName("calificarSesion con comentario nulo (opcional) debe persistir igual")
    void calificarSesion_comentarioNulo_debePersistir() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));
        when(calificacionSesionRepository.existsBySesionIdAndEstudianteId(SESION_ID, ESTUDIANTE_ID))
                .thenReturn(false);
        when(estudianteRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudianteMock));
        when(calificacionSesionRepository.save(any(CalificacionSesion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CalificacionSesionRequest sinComentario = new CalificacionSesionRequest(5, null);

        CalificacionSesionResponse response =
                calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, sinComentario);

        assertNull(response.comentario());
        assertEquals(5, response.calificacion());
        verify(calificacionSesionRepository).save(any(CalificacionSesion.class));
    }

    @Test
    @DisplayName("calificarSesion sobre sesión inexistente debe lanzar 404")
    void calificarSesion_sesionInexistente_debeLanzarExcepcion() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.empty());

        CalificacionTutoriaException exception = assertThrows(CalificacionTutoriaException.class, () ->
                calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, request)
        );

        assertEquals("Sesión no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(calificacionSesionRepository, never()).save(any());
    }

    @Test
    @DisplayName("calificarSesion de una sesión ajena debe lanzar AccessDeniedException (RF-05)")
    void calificarSesion_sesionAjena_debeLanzarAccessDenied() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));

        Long otroEstudianteId = 999L;

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                calificacionSesionService.calificarSesion(otroEstudianteId, SESION_ID, request)
        );

        assertEquals("No puedes calificar una sesión que no es tuya", exception.getMessage());
        verify(calificacionSesionRepository, never()).save(any());
    }

    @Test
    @DisplayName("calificarSesion de una sesión no COMPLETADA debe lanzar 409 (RF-07)")
    void calificarSesion_sesionNoCompletada_debeLanzarExcepcion() {
        sesionMock.setEstado(EstadoSesion.APROBADA);
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));

        CalificacionTutoriaException exception = assertThrows(CalificacionTutoriaException.class, () ->
                calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, request)
        );

        assertEquals("Solo puedes calificar sesiones completadas", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(calificacionSesionRepository, never()).save(any());
    }

    @Test
    @DisplayName("calificarSesion ya calificada debe lanzar 409 Conflict (RF-06)")
    void calificarSesion_yaCalificada_debeLanzarDuplicada() {
        when(sesionRepository.findById(SESION_ID)).thenReturn(Optional.of(sesionMock));
        when(calificacionSesionRepository.existsBySesionIdAndEstudianteId(SESION_ID, ESTUDIANTE_ID))
                .thenReturn(true);

        CalificacionTutoriaException exception = assertThrows(CalificacionTutoriaException.class, () ->
                calificacionSesionService.calificarSesion(ESTUDIANTE_ID, SESION_ID, request)
        );

        assertEquals("Ya calificaste esta sesión", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(calificacionSesionRepository, never()).save(any());
    }
}
