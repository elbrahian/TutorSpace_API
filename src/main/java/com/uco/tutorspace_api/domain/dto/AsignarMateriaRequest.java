package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignarMateriaRequest(
        @NotNull(message = "El ID de la materia es requerido")
        @Positive(message = "El ID de la materia debe ser un valor positivo")
        Long materiaId
) {}