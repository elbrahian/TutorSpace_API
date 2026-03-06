package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;

import java.time.LocalTime;

public record DisponibilidadResponse(
        Long id,
        String dia,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoDisponibilidad estado
) {
}
