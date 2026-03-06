package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoSesionRequest(
        @NotNull EstadoSesion nuevoEstado
) {
}
