package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDateTime;

public record MensajeWebSocketPayload(
        Long chatId,
        Long emisorId,
        String nombreEmisor,
        String contenido,
        LocalDateTime fecha
) {
}
