package com.uco.tutorspace_api.domain.dto;

public record AuthResponse(
        String token,
        String rol,
        String nombre,
        Long id
) {}
