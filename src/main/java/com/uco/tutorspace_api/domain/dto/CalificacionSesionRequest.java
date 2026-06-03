package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CalificacionSesionRequest(

        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        int calificacion,

        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String comentario
) {
}
