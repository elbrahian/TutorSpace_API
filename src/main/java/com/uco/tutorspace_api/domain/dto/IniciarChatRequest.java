package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarChatRequest(
        @NotNull(message = "El ID del tutor es requerido")
        Long tutorId
) {
}
