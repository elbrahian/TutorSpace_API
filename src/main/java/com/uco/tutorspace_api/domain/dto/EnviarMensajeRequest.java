package com.uco.tutorspace_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensajeRequest(
        @JsonProperty("contenido")
        @NotBlank(message = "El contenido es requerido")
        @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
        String contenido
) {
}
