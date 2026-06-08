package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[a-zA-Z ]{2,100}$", message = "El nombre solo puede contener letras y espacios")
        String nombre,
        
        @NotBlank(message = "El email es requerido")
        @Email(message = "El formato del email no es válido")
        String email,
        
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {}
