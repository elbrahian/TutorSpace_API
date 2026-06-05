package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionRequest;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.exceptions.CalificacionTutoriaException;
import com.uco.tutorspace_api.repositories.CalificacionSesionRepository;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalificacionSesionService {

    private final CalificacionSesionRepository calificacionSesionRepository;
    private final SesionRepository sesionRepository;
    private final EstudianteRepository estudianteRepository;

    public CalificacionSesionResponse calificarSesion(Long estudianteId, Long sesionId,
                                                      CalificacionSesionRequest request) {

        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new CalificacionTutoriaException(
                        "Sesión no encontrada", HttpStatus.NOT_FOUND));

        // RF-05 — solo el estudiante dueño de la sesión puede calificarla
        if (!sesion.getEstudiante().getId().equals(estudianteId)) {
            throw new AccessDeniedException("No puedes calificar una sesión que no es tuya");
        }

        // RF-07 / E1 — solo sesiones COMPLETADA
        if (sesion.getEstado() != EstadoSesion.COMPLETADA) {
            throw new CalificacionTutoriaException(
                    "Solo puedes calificar sesiones completadas", HttpStatus.CONFLICT);
        }

        // RF-06 / E2 — no se permite calificar dos veces => 409 Conflict
        if (calificacionSesionRepository.existsBySesionIdAndEstudianteId(sesionId, estudianteId)) {
            throw new CalificacionTutoriaException(
                    "Ya calificaste esta sesión", HttpStatus.CONFLICT);
        }

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new CalificacionTutoriaException(
                        "Estudiante no encontrado", HttpStatus.NOT_FOUND));

        CalificacionSesion calificacion = new CalificacionSesion();
        calificacion.setSesion(sesion);
        calificacion.setEstudiante(estudiante);
        calificacion.setCalificacion(request.calificacion());
        calificacion.setComentario(request.comentario());

        CalificacionSesion guardada = calificacionSesionRepository.save(calificacion);

        // RF-09 — confirmación con los datos registrados
        return new CalificacionSesionResponse(
                guardada.getId(),
                sesion.getId(),
                sesion.getTutor().getNombre(),
                guardada.getCalificacion(),
                guardada.getComentario(),
                sesion.getFecha(),
                sesion.getHoraInicio(),
                "Calificación registrada con éxito. ¡Muchas gracias!"
        );
    }

}
