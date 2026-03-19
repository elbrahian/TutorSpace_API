package com.uco.tutorspace_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensajeRequest(
        @JsonProperty("contenido")
        @NotBlank @Size(max = 500) String contenido
) {
}
