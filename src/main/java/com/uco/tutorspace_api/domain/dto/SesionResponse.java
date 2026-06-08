package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSesion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SesionResponse(
        Long id,
        Long tutorId,
        String nombreTutor,
        Long estudianteId,
        String nombreEstudiante,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoSesion estado,
        LocalDateTime createdAt,
        boolean calificada
) {
}
