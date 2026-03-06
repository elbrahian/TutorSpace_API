package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDateTime;

public record ChatResponse(
        Long id,
        Long tutorId,
        String nombreTutor,
        Long estudianteId,
        String nombreEstudiante,
        LocalDateTime fechaCreacion
) {
}
