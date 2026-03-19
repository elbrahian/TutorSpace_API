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

import java.time.LocalDate;
import java.util.List;

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

    // HU-09 — el tutor crea la sesión (debe existir chat previo)
    public SesionResponse crearSesion(Long tutorId, CrearSesionRequest request) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        // HU-09 CA-01 — verificar que existe chat previo entre tutor y estudiante
        boolean existeChat = chatRepository
                .existsByTutorIdAndEstudianteId(tutorId, request.estudianteId());

        if (!existeChat) {
            throw new RuntimeException(
                    "Debe existir una conversación previa para crear la sesión"
            );
        }

        // Obtener y validar disponibilidad
        Disponibilidad disponibilidad = disponibilidadRepository
                .findById(request.disponibilidadId())
                .orElseThrow(() -> new RuntimeException("Disponibilidad no encontrada"));

        if (disponibilidad.getEstado() == EstadoDisponibilidad.BLOQUEADA) {
            throw new RuntimeException("La franja horaria ya está ocupada");
        }

        // Crear sesión
        Sesion sesion = new Sesion();
        sesion.setTutor(tutor);
        sesion.setEstudiante(estudiante);
        sesion.setDisponibilidad(disponibilidad);
        sesion.setFecha(request.fecha());
        sesion.setHoraInicio(request.horaInicio());
        sesion.setHoraFin(request.horaFin());
        sesion.setEstado(EstadoSesion.PENDIENTE);  // HU-09 CA-03

        Sesion guardada = sesionRepository.save(sesion);

        // HU-10 — bloquear franja automáticamente
        disponibilidadService.bloquearFranja(disponibilidad.getId());

        // Registrar en historial
        historialSesionService.registrarCambio(guardada, EstadoSesion.PENDIENTE, EstadoSesion.PENDIENTE);

        // Notificar al estudiante
        notificacionService.enviarNotificacion(
                estudiante,
                TipoNotificacion.SESION_CREADA,
                "El tutor " + tutor.getNombre() + " agendó una sesión contigo el " + request.fecha()
        );

        return toResponse(guardada);
    }

    // HU-10 — solo el tutor puede cambiar el estado
    public SesionResponse cambiarEstado(Long sesionId, Long tutorId,
                                        CambiarEstadoSesionRequest request) {
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        // Verificar que el tutor es dueño de la sesión
        if (!sesion.getTutor().getId().equals(tutorId)) {
            throw new RuntimeException("No tienes permiso para modificar esta sesión");
        }

        EstadoSesion estadoAnterior = sesion.getEstado();
        EstadoSesion estadoNuevo = request.nuevoEstado();

        sesion.setEstado(estadoNuevo);
        Sesion actualizada = sesionRepository.save(sesion);

        // Registrar cambio en historial automáticamente
        historialSesionService.registrarCambio(actualizada, estadoAnterior, estadoNuevo);

        // Si se cancela, liberar la franja (HU-10 CA-03)
        if (estadoNuevo == EstadoSesion.CANCELADA) {
            disponibilidadService.liberarFranja(sesion.getDisponibilidad().getId());
        }

        // Notificar al estudiante del cambio (HU-10 CA-02)
        notificacionService.enviarNotificacion(
                sesion.getEstudiante(),
                TipoNotificacion.CAMBIO_ESTADO,
                "Tu sesión del " + sesion.getFecha() + " cambió a estado: " + estadoNuevo
        );

        return toResponse(actualizada);
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
                s.getCreatedAt()
        );
    }
}
