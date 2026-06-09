package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearMateriaRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9 ]{2,100}$",
                message = "El nombre solo puede contener letras, numeros y espacios")
        String nombre,

        @NotBlank(message = "El codigo es requerido")
        @Size(min = 2, max = 20, message = "El codigo debe tener entre 2 y 20 caracteres")
        @Pattern(regexp = "^[A-Z][A-Z0-9]{1,19}$",
                message = "El codigo solo puede contener letras mayusculas y numeros")
        String codigo
) {}