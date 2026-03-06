package com.uco.tutorspace_api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String nombre,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password
) {}
