package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearSolicitudTutorRequest(
        @NotEmpty(message = "Debe seleccionar al menos una materia")
        List<Long> materiaIds,

        @NotBlank(message = "La justificación es requerida")
        @Size(max = 1000, message = "La justificación no puede exceder 1000 caracteres")
        String justificacion
) {
}
