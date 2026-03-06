package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarMateriaRequest(
        @NotNull Long materiaId
) {
}
