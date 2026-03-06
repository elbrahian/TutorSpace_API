package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrearSesionRequest(
        @NotNull Long estudianteId,
        @NotNull Long disponibilidadId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {
}
