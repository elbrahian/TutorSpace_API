package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearMateriaRequest(
        @NotBlank String nombre,
        @NotBlank @Size(max = 20) String codigo
) { }
