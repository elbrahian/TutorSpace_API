package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email es requerido")
        @Email(message = "El formato del email no es válido")
        String email,
        
        @NotBlank(message = "La contraseña es requerida")
        String password
){}
