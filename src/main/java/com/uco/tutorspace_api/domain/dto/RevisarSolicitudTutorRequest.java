package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisarSolicitudTutorRequest(
        @NotNull(message = "El nuevo estado es requerido")
        EstadoSolicitudTutor nuevoEstado,

        @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
        String observaciones
) {
}
