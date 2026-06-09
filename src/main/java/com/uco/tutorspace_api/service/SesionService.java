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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SesionService {
    private final SesionRepository sesionRepository;
    private final TutorRepository tutorRepository;
    private final EstudianteRepository estudianteRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final ChatRepository chatRepository;
    private final DisponibilidadService disponibilidadService;
    private final HistorialSesionService historialSesionService;
    private final NotificacionService notificacionService;
    private final ChatService chatService; // MNT-05

    public SesionResponse crearSesion(Long tutorId, CrearSesionRequest request) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new NoSuchElementException("Estudiante no encontrado"));

        boolean existeChat = chatRepository
                .existsByTutorIdAndEstudianteId(tutorId, request.estudianteId());

        if (!existeChat) {
            throw new IllegalStateException(
                    "Debe existir una conversación previa para crear la sesión"
            );
        }

        Disponibilidad disponibilidad = disponibilidadRepository
                .findById(request.disponibilidadId())
                .orElseThrow(() -> new NoSuchElementException("Disponibilidad no encontrada"));

        if (disponibilidad.getEstado() == EstadoDisponibilidad.BLOQUEADA) {
            throw new IllegalStateException("La franja horaria ya está ocupada");
        }

        Sesion sesion = new Sesion();
        sesion.setTutor(tutor);
        sesion.setEstudiante(estudiante);
        sesion.setDisponibilidad(disponibilidad);
        sesion.setFecha(request.fecha());
        sesion.setHoraInicio(request.horaInicio());
        sesion.setHoraFin(request.horaFin());
        sesion.setEstado(EstadoSesion.PENDIENTE);

        Sesion guardada = sesionRepository.save(sesion);

        disponibilidadService.bloquearFranja(disponibilidad.getId());

        historialSesionService.registrarCambio(guardada, EstadoSesion.PENDIENTE, EstadoSesion.PENDIENTE);

        notificacionService.enviarNotificacion(
                estudiante,
                TipoNotificacion.SESION_CREADA,
                "El tutor " + tutor.getNombre() + " agendó una sesión contigo el " + request.fecha()
        );

        // MNT-05 — mensaje automático al crear sesión
        chatService.enviarMensajeSistema(
                tutor.getId(),
                estudiante.getId(),
                "Se agendó una sesión para el " + request.fecha()
                        + " de " + request.horaInicio()
                        + " a " + request.horaFin()
        );

        return toResponse(guardada);
    }

    public SesionResponse cambiarEstado(Long sesionId, Long tutorId,
                                        CambiarEstadoSesionRequest request) {
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));

        if (!sesion.getTutor().getId().equals(tutorId)) {
            throw new IllegalStateException("No tienes permiso para modificar esta sesión");
        }

        EstadoSesion estadoAnterior = sesion.getEstado();
        EstadoSesion estadoNuevo = request.nuevoEstado();

        if (estadoNuevo == EstadoSesion.COMPLETADA
                && estadoAnterior != EstadoSesion.APROBADA) {
            throw new IllegalStateException("Solo se puede completar una sesión que esté APROBADA");
        }

        sesion.setEstado(estadoNuevo);
        Sesion actualizada = sesionRepository.save(sesion);

        historialSesionService.registrarCambio(actualizada, estadoAnterior, estadoNuevo);

        if (estadoNuevo == EstadoSesion.CANCELADA) {
            disponibilidadService.liberarFranja(sesion.getDisponibilidad().getId());
        }

        notificacionService.enviarNotificacion(
                sesion.getEstudiante(),
                TipoNotificacion.CAMBIO_ESTADO,
                "Tu sesión del " + sesion.getFecha() + " cambió a estado: " + estadoNuevo
        );

        // MNT-05 — mensaje automático al cambiar estado
        String textoMensaje = switch (estadoNuevo) {
            case APROBADA  -> "La sesión del " + sesion.getFecha() + " fue aprobada por el tutor";
            case CANCELADA -> "La sesión del " + sesion.getFecha() + " fue cancelada";
            default        -> "La sesión del " + sesion.getFecha() + " cambió a estado: " + estadoNuevo;
        };

        chatService.enviarMensajeSistema(
                sesion.getTutor().getId(),
                sesion.getEstudiante().getId(),
                textoMensaje
        );

        return toResponse(actualizada);
    }

    // MNT-12 — zona horaria del negocio (Colombia). Se fija explícitamente para que
    // la auto-completación no dependa de la zona del servidor (los contenedores suelen
    // correr en UTC, lo que adelantaría el cierre de las sesiones).
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    /**
     * MNT-12 — marca como COMPLETADA las sesiones APROBADA cuya hora de fin ya pasó.
     * Registra el cambio en el historial y notifica al estudiante (igual que el flujo
     * manual). Devuelve cuántas sesiones se completaron. Lo invoca el scheduler.
     */
    @Transactional
    public int completarSesionesVencidas() {
        LocalDate hoy = LocalDate.now(ZONA);
        LocalTime ahora = LocalTime.now(ZONA);

        List<Sesion> vencidas = sesionRepository.findAprobadasVencidas(hoy, ahora);

        for (Sesion sesion : vencidas) {
            sesion.setEstado(EstadoSesion.COMPLETADA);
            sesionRepository.save(sesion);

            historialSesionService.registrarCambio(
                    sesion, EstadoSesion.APROBADA, EstadoSesion.COMPLETADA);

            notificacionService.enviarNotificacion(
                    sesion.getEstudiante(),
                    TipoNotificacion.CAMBIO_ESTADO,
                    "Tu sesión del " + sesion.getFecha() + " finalizó. Ya puedes evaluarla."
            );
        }

        return vencidas.size();
    }

    public List<SesionResponse> obtenerPorEstudiante(Long estudianteId) {
        return sesionRepository.findByEstudianteId(estudianteId)
                .stream().map(this::toResponse).toList();
    }

    public List<SesionResponse> obtenerPorTutor(Long tutorId) {
        return sesionRepository.findByTutorId(tutorId)
                .stream().map(this::toResponse).toList();
    }

    public List<SesionResponse> obtenerPorEstudianteYFecha(Long estudianteId,
                                                           LocalDate inicio,
                                                           LocalDate fin) {
        return sesionRepository
                .findByEstudianteIdAndFechaBetween(estudianteId, inicio, fin)
                .stream().map(this::toResponse).toList();
    }

    public SesionResponse toResponse(Sesion s) {
        // MNT-12 — indica si el estudiante dueño ya evaluó la sesión, para que el
        // front muestre el botón "Evaluar sesión" solo cuando aún no se ha calificado.
        boolean calificada = calificacionSesionRepository
                .existsBySesionIdAndEstudianteId(s.getId(), s.getEstudiante().getId());

        return new SesionResponse(
                s.getId(),
                s.getTutor().getId(),
                s.getTutor().getNombre(),
                s.getEstudiante().getId(),
                s.getEstudiante().getNombre(),
                s.getFecha(),
                s.getHoraInicio(),
                s.getHoraFin(),
                s.getEstado(),
                s.getCreatedAt(),
                calificada
        );
    }
}