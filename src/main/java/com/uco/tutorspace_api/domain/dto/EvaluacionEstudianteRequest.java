package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record EvaluacionEstudianteRequest(
        @Min(value = 1, message = "La puntuación debe ser entre 1 y 5")
        @Max(value = 5, message = "La puntuación debe ser entre 1 y 5")
        Integer puntuacion,

        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
        String observaciones
) {}
