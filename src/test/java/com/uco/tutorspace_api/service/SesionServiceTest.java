package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Disponibilidad;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.CambiarEstadoSesionRequest;
import com.uco.tutorspace_api.domain.dto.CrearSesionRequest;
import com.uco.tutorspace_api.domain.dto.SesionResponse;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.TipoNotificacion;
import com.uco.tutorspace_api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock private SesionRepository sesionRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private DisponibilidadRepository disponibilidadRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private DisponibilidadService disponibilidadService;
    @Mock private HistorialSesionService historialSesionService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private SesionService sesionService;

    private Tutor tutorMock;
    private Estudiante estudianteMock;
    private Disponibilidad disponibilidadMock;
    private Sesion sesionMock;
    private CrearSesionRequest crearSesionRequest;

    @BeforeEach
    void setUp() {
        tutorMock = new Tutor();
        tutorMock.setId(1L);
        tutorMock.setNombre("Tutor Test");

        estudianteMock = new Estudiante();
        estudianteMock.setId(2L);
        estudianteMock.setNombre("Estudiante Test");

        disponibilidadMock = new Disponibilidad();
        disponibilidadMock.setId(1L);
        disponibilidadMock.setTutor(tutorMock);
        disponibilidadMock.setEstado(EstadoDisponibilidad.DISPONIBLE);

        sesionMock = new Sesion();
        sesionMock.setId(1L);
        sesionMock.setTutor(tutorMock);
        sesionMock.setEstudiante(estudianteMock);
        sesionMock.setDisponibilidad(disponibilidadMock);
        sesionMock.setFecha(LocalDate.now().plusDays(1));
        sesionMock.setHoraInicio(LocalTime.of(8, 0));
        sesionMock.setHoraFin(LocalTime.of(10, 0));
        sesionMock.setEstado(EstadoSesion.PENDIENTE);

        crearSesionRequest = new CrearSesionRequest(
                2L, 1L, LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0)
        );
    }

    @Test
    @DisplayName("crearSesion con chat existente debe crear sesión")
    void crearSesion_withExistingChat_shouldCreateSession() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        SesionResponse response = sesionService.crearSesion(1L, crearSesionRequest);

        assertNotNull(response);
        assertEquals(EstadoSesion.PENDIENTE, response.estado());
        verify(sesionRepository).save(any(Sesion.class));
    }

    @Test
    @DisplayName("crearSesion debe establecer estado PENDIENTE")
    void crearSesion_shouldSetEstadoPendiente() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        sesionService.crearSesion(1L, crearSesionRequest);

        verify(sesionRepository).save(argThat(s -> s.getEstado() == EstadoSesion.PENDIENTE));
    }

    @Test
    @DisplayName("crearSesion debe bloquear disponibilidad")
    void crearSesion_shouldBloquearDisponibilidad() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        sesionService.crearSesion(1L, crearSesionRequest);

        verify(disponibilidadService).bloquearFranja(1L);
    }

    @Test
    @DisplayName("crearSesion debe crear entrada en historial")
    void crearSesion_shouldCreateHistorialEntry() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        sesionService.crearSesion(1L, crearSesionRequest);

        verify(historialSesionService).registrarCambio(any(), eq(EstadoSesion.PENDIENTE), eq(EstadoSesion.PENDIENTE));
    }

    @Test
    @DisplayName("crearSesion debe notificar al estudiante")
    void crearSesion_shouldNotifyEstudiante() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        sesionService.crearSesion(1L, crearSesionRequest);

        verify(notificacionService).enviarNotificacion(eq(estudianteMock), eq(TipoNotificacion.SESION_CREADA), anyString());
    }

    @Test
    @DisplayName("cambiarEstado a APROBADA debe actualizar estado")
    void cambiarEstado_toAprobada_shouldUpdateState() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.APROBADA);
        SesionResponse response = sesionService.cambiarEstado(1L, 1L, request);

        assertNotNull(response);
        verify(historialSesionService).registrarCambio(any(), eq(EstadoSesion.PENDIENTE), eq(EstadoSesion.APROBADA));
    }

    @Test
    @DisplayName("cambiarEstado a CANCELADA debe liberar disponibilidad")
    void cambiarEstado_toCancelada_shouldLiberarDisponibilidad() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.CANCELADA);
        sesionService.cambiarEstado(1L, 1L, request);

        verify(disponibilidadService).liberarFranja(1L);
    }

    @Test
    @DisplayName("cambiarEstado debe crear entrada en historial")
    void cambiarEstado_shouldCreateHistorialEntry() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.APROBADA);
        sesionService.cambiarEstado(1L, 1L, request);

        verify(historialSesionService).registrarCambio(any(), any(), eq(EstadoSesion.APROBADA));
    }

    @Test
    @DisplayName("cambiarEstado debe notificar al estudiante")
    void cambiarEstado_shouldNotifyEstudiante() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.APROBADA);
        sesionService.cambiarEstado(1L, 1L, request);

        verify(notificacionService).enviarNotificacion(eq(estudianteMock), eq(TipoNotificacion.CAMBIO_ESTADO), anyString());
    }

    @Test
    @DisplayName("cambiarEstado APROBADA → COMPLETADA debe actualizar estado (MNT-12)")
    void cambiarEstado_aprobadaToCompletada_shouldUpdateState() {
        sesionMock.setEstado(EstadoSesion.APROBADA);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));
        when(sesionRepository.save(any(Sesion.class))).thenReturn(sesionMock);

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.COMPLETADA);
        sesionService.cambiarEstado(1L, 1L, request);

        verify(historialSesionService).registrarCambio(any(), eq(EstadoSesion.APROBADA), eq(EstadoSesion.COMPLETADA));
    }

    @Test
    @DisplayName("cambiarEstado PENDIENTE → COMPLETADA debe lanzar excepción (MNT-12)")
    void cambiarEstado_pendienteToCompletada_shouldThrowException() {
        sesionMock.setEstado(EstadoSesion.PENDIENTE);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.COMPLETADA);
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                sesionService.cambiarEstado(1L, 1L, request)
        );

        assertEquals("Solo se puede completar una sesión que esté APROBADA", exception.getMessage());
        verify(sesionRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearSesion sin chat previo debe lanzar excepción")
    void crearSesion_withoutExistingChat_shouldThrowException() {
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            sesionService.crearSesion(1L, crearSesionRequest)
        );

        assertEquals("Debe existir una conversación previa para crear la sesión", exception.getMessage());
    }

    @Test
    @DisplayName("crearSesion con disponibilidad bloqueada debe lanzar excepción")
    void crearSesion_withBloqueadaDisponibilidad_shouldThrowException() {
        disponibilidadMock.setEstado(EstadoDisponibilidad.BLOQUEADA);
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            sesionService.crearSesion(1L, crearSesionRequest)
        );

        assertEquals("La franja horaria ya está ocupada", exception.getMessage());
    }

    @Test
    @DisplayName("cambiarEstado por tutor diferente debe lanzar excepción")
    void cambiarEstado_byDifferentTutor_shouldThrowException() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionMock));

        CambiarEstadoSesionRequest request = new CambiarEstadoSesionRequest(EstadoSesion.APROBADA);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            sesionService.cambiarEstado(1L, 999L, request)
        );

        assertEquals("No tienes permiso para modificar esta sesión", exception.getMessage());
    }
}
