package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CrearDisponibilidadRequest(
        @NotBlank String dia,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}
