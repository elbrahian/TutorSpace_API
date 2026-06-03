package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalificacionSesionResponse(
        Long calificacionId,
        Long sesionId,
        String nombreTutor,
        int calificacion,
        String comentario,
        LocalDate fecha,
        LocalTime horaInicio,
        String mensaje
) {
}
