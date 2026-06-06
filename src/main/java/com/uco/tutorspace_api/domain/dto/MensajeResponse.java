package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para mensajes del chat.
 * emisorId es null y esSistema es true cuando el mensaje es automático (MNT-05).
 */
public record MensajeResponse(
        Long id,
        Long emisorId,       // null si es mensaje del sistema
        String nombreEmisor, // "Sistema" si esSistema = true
        String contenido,
        LocalDateTime fecha,
        boolean esSistema
) {
}