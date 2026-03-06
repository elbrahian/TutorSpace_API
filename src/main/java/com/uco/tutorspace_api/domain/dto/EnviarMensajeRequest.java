package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensajeRequest(
        @NotBlank @Size(max = 500) String contenido
) {
}
