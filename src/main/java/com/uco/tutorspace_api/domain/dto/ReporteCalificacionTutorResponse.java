package com.uco.tutorspace_api.domain.dto;

import java.util.List;

/**
 * MNT-10 — Fila del reporte de calificación de tutores.
 * Agrega el promedio, el total de evaluaciones, la distribución de estrellas
 * (1 a 5) y los comentarios de los estudiantes para un tutor, calculados sobre
 * las evaluaciones de MNT-12 dentro de un rango de fechas de sesión.
 */
public record ReporteCalificacionTutorResponse(
        Long tutorId,
        String nombreTutor,
        double promedioCalificacion,
        long totalEvaluaciones,
        long estrellas1,
        long estrellas2,
        long estrellas3,
        long estrellas4,
        long estrellas5,
        List<ComentarioCalificacionTutorResponse> comentarios
) {
}
