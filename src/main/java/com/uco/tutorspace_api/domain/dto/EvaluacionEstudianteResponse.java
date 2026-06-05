package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDateTime;

public record EvaluacionEstudianteResponse(
        Long id,
        Long sesionId,
        Long tutorId,
        String tutorNombre,
        Long estudianteId,
        String estudianteNombre,
        Integer puntuacion,
        String observaciones,
        LocalDateTime createdAt
) {}
