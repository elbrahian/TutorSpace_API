package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDate;

/**
 * MNT-10 — Comentario escrito por un estudiante al evaluar una sesión.
 * Es una proyección de solo lectura sobre {@code CalificacionSesion} (MNT-12);
 * no se persiste ni se modifica desde este reporte.
 */
public record ComentarioCalificacionTutorResponse(
        Long calificacionId,
        Long tutorId,
        String nombreEstudiante,
        int calificacion,
        String comentario,
        LocalDate fechaSesion
) {
}
