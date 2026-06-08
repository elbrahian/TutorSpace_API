package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrearSesionRequest(
        @NotNull(message = "El ID del estudiante es requerido")
        @Positive(message = "El ID debe ser un valor positivo")
        Long estudianteId,
        
        @NotNull(message = "El ID de la disponibilidad es requerido")
        @Positive(message = "El ID debe ser un valor positivo")
        Long disponibilidadId,
        
        @NotNull(message = "La fecha es requerida")
        @FutureOrPresent(message = "La fecha de la sesion no puede ser en el pasado")
        LocalDate fecha,
        
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        
        @NotNull(message = "La hora de fin es requerida")
        LocalTime horaFin
) {
}
