package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrearSesionRequest(
        @NotNull(message = "El ID del estudiante es requerido")
        Long estudianteId,
        
        @NotNull(message = "El ID de la disponibilidad es requerido")
        Long disponibilidadId,
        
        @NotNull(message = "La fecha es requerida")
        LocalDate fecha,
        
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        
        @NotNull(message = "La hora de fin es requerida")
        LocalTime horaFin
) {
}
