package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record AsignarMateriaRequest(
        @NotNull
        @Positive(message = "El ID debe ser un valor positivo")
        Long materiaId
) {
}
