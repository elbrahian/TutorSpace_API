package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CrearDisponibilidadRequest(
        @NotBlank(message = "El día es requerido")
        String dia,
        
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        
        @NotNull(message = "La hora de fin es requerida")
        LocalTime horaFin
) {}
