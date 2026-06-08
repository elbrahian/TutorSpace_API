package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IniciarChatRequest(
        @NotNull(message = "El ID del tutor es requerido")
        @Positive(message = "El ID debe ser un valor positivo")
        Long tutorId
) {
}
