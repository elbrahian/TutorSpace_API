package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public record CrearDisponibilidadRequest(
        @NotBlank(message = "El día es requerido")
        @Pattern(regexp = "^(LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SABADO|DOMINGO)$",
                message = "El dia debe ser un dia de la semana valido en mayusculas")
        String dia,
        
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        
        @NotNull(message = "La hora de fin es requerida")
        LocalTime horaFin
) {}
