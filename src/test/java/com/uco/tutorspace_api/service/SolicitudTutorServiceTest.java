package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.SolicitudTutor;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.dto.CrearSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.RevisarSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.SolicitudTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import com.uco.tutorspace_api.repositories.SolicitudTutorRepository;
import com.uco.tutorspace_api.repositories.TutorPromotionRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudTutorServiceTest {

    @Mock private SolicitudTutorRepository solicitudTutorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private MateriaRepository materiaRepository;
    @Mock private TutorPromotionRepository tutorPromotionRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private SolicitudTutorService solicitudTutorService;

    private Usuario estudiante;
    private Materia materia;
    private SolicitudTutor solicitud;

    @BeforeEach
    void setUp() {
        estudiante = new Usuario() {};
        estudiante.setId(1L);
        estudiante.setNombre("Estudiante Test");
        estudiante.setEmail("estudiante@uco.net.co");
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);

        materia = new Materia();
        materia.setId(10L);
        materia.setNombre("Matemáticas");
        materia.setCodigo("MAT");

        solicitud = new SolicitudTutor();
        solicitud.setId(100L);
        solicitud.setSolicitante(estudiante);
        solicitud.setJustificacion("Quiero apoyar a otros estudiantes");
        solicitud.setEstado(EstadoSolicitudTutor.PENDIENTE);
        solicitud.agregarMateria(materia);
    }

    @Test
    @DisplayName("crearSolicitud debe registrar solicitud pendiente")
    void crearSolicitud_shouldCreatePendingRequest() {
        CrearSolicitudTutorRequest request = new CrearSolicitudTutorRequest(
                List.of(10L),
                "Quiero apoyar a otros estudiantes"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(solicitudTutorRepository.existsBySolicitanteIdAndEstado(1L, EstadoSolicitudTutor.PENDIENTE))
                .thenReturn(false);
        when(materiaRepository.findAllById(any(Iterable.class))).thenReturn(List.of(materia));
        when(solicitudTutorRepository.save(any(SolicitudTutor.class))).thenReturn(solicitud);

        SolicitudTutorResponse response = solicitudTutorService.crearSolicitud(1L, request);

        assertNotNull(response);
        assertEquals(EstadoSolicitudTutor.PENDIENTE, response.estado());
        assertEquals(1L, response.solicitanteId());
        assertEquals(1, response.materias().size());
        verify(solicitudTutorRepository).save(any(SolicitudTutor.class));
        verify(emailService).enviarEstadoSolicitudTutor(
                estudiante,
                EstadoSolicitudTutor.PENDIENTE,
                "Tu solicitud fue recibida y está pendiente de revisión."
        );
    }

    @Test
    @DisplayName("crearSolicitud con pendiente existente debe lanzar excepción")
    void crearSolicitud_withPendingRequest_shouldThrowException() {
        CrearSolicitudTutorRequest request = new CrearSolicitudTutorRequest(
                List.of(10L),
                "Quiero ser tutor"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(solicitudTutorRepository.existsBySolicitanteIdAndEstado(1L, EstadoSolicitudTutor.PENDIENTE))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                solicitudTutorService.crearSolicitud(1L, request)
        );

        assertEquals("Ya existe una solicitud pendiente para este usuario", exception.getMessage());
        verify(solicitudTutorRepository, never()).save(any(SolicitudTutor.class));
    }

    @Test
    @DisplayName("crearSolicitud con usuario inactivo debe lanzar excepción")
    void crearSolicitud_withInactiveUser_shouldThrowException() {
        estudiante.setEstado(EstadoUsuario.INACTIVO);
        CrearSolicitudTutorRequest request = new CrearSolicitudTutorRequest(
                List.of(10L),
                "Quiero ser tutor"
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                solicitudTutorService.crearSolicitud(1L, request)
        );

        assertEquals("La cuenta debe estar activa para enviar la solicitud", exception.getMessage());
    }

    @Test
    @DisplayName("revisarSolicitud aprobada debe promover usuario a tutor")
    void revisarSolicitud_approved_shouldPromoteUser() {
        RevisarSolicitudTutorRequest request = new RevisarSolicitudTutorRequest(
                EstadoSolicitudTutor.APROBADA,
                "Aprobado"
        );

        when(solicitudTutorRepository.findByIdWithDetalle(100L)).thenReturn(Optional.of(solicitud));
        when(solicitudTutorRepository.save(any(SolicitudTutor.class))).thenReturn(solicitud);

        SolicitudTutorResponse response = solicitudTutorService.revisarSolicitud(100L, request);

        assertEquals(EstadoSolicitudTutor.APROBADA, response.estado());
        verify(tutorPromotionRepository).promoverUsuarioATutor(1L);
        verify(tutorPromotionRepository).asignarMateriasSolicitadas(1L, 100L);
        verify(emailService).enviarEstadoSolicitudTutor(estudiante, EstadoSolicitudTutor.APROBADA, "Aprobado");
    }

    @Test
    @DisplayName("revisarSolicitud rechazada no debe promover usuario")
    void revisarSolicitud_rejected_shouldNotPromoteUser() {
        RevisarSolicitudTutorRequest request = new RevisarSolicitudTutorRequest(
                EstadoSolicitudTutor.RECHAZADA,
                null
        );

        when(solicitudTutorRepository.findByIdWithDetalle(100L)).thenReturn(Optional.of(solicitud));
        when(solicitudTutorRepository.save(any(SolicitudTutor.class))).thenReturn(solicitud);

        SolicitudTutorResponse response = solicitudTutorService.revisarSolicitud(100L, request);

        assertEquals(EstadoSolicitudTutor.RECHAZADA, response.estado());
        verify(tutorPromotionRepository, never()).promoverUsuarioATutor(anyLong());
        verify(tutorPromotionRepository, never()).asignarMateriasSolicitadas(anyLong(), anyLong());
        verify(emailService).enviarEstadoSolicitudTutor(estudiante, EstadoSolicitudTutor.RECHAZADA, null);
    }

    @Test
    @DisplayName("revisarSolicitud no permite dejar estado pendiente")
    void revisarSolicitud_pendingState_shouldThrowException() {
        RevisarSolicitudTutorRequest request = new RevisarSolicitudTutorRequest(
                EstadoSolicitudTutor.PENDIENTE,
                null
        );

        when(solicitudTutorRepository.findByIdWithDetalle(100L)).thenReturn(Optional.of(solicitud));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                solicitudTutorService.revisarSolicitud(100L, request)
        );

        assertEquals("La revisión debe aprobar o rechazar la solicitud", exception.getMessage());
        verify(tutorPromotionRepository, never()).promoverUsuarioATutor(anyLong());
        verify(tutorPromotionRepository, never()).asignarMateriasSolicitadas(anyLong(), anyLong());
    }
}
