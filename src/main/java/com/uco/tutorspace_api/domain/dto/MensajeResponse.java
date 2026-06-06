package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long emisorId,
        String nombreEmisor,
        String contenido,
        LocalDateTime fecha,
        boolean esSistema
) {
}