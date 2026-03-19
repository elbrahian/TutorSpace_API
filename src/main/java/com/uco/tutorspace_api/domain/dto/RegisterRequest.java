package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,
        
        @NotBlank(message = "El email es requerido")
        @Email(message = "El formato del email no es válido")
        String email,
        
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        String password
) {}
