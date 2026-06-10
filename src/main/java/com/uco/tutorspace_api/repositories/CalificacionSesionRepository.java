package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalificacionSesionRepository extends JpaRepository<CalificacionSesion,Long> {
    boolean existsBySesionIdAndEstudianteId(Long sesionId, Long estudianteId);

    /**
     * MNT-08 — promedio de calificación de un tutor en un rango de fechas.
     * Navega la relación CalificacionSesion -> Sesion -> Tutor y filtra por la
     * fecha de la sesión. Devuelve null si el tutor no tiene evaluaciones en el rango.
     */
    @Query("""
            SELECT AVG(c.calificacion)
            FROM CalificacionSesion c
            WHERE c.sesion.tutor.id = :tutorId
              AND c.sesion.fecha BETWEEN :inicio AND :fin
            """)
    Double promedioCalificacionPorTutor(@Param("tutorId") Long tutorId,
                                        @Param("inicio") LocalDate inicio,
                                        @Param("fin") LocalDate fin);

    /**
     * MNT-10 — ranking agregado de tutores por calificación promedio en un rango
     * de fechas de sesión. Agrega en la base de datos (RNF-02) y devuelve por fila:
     * [0] tutorId, [1] nombreTutor, [2] promedio, [3] totalEvaluaciones,
     * [4..8] conteo de evaluaciones con 1, 2, 3, 4 y 5 estrellas.
     * Solo incluye tutores con al menos una evaluación en el rango.
     */
    @Query("""
            SELECT c.sesion.tutor.id,
                   c.sesion.tutor.nombre,
                   AVG(c.calificacion),
                   COUNT(c.id),
                   SUM(CASE WHEN c.calificacion = 1 THEN 1 ELSE 0 END),
                   SUM(CASE WHEN c.calificacion = 2 THEN 1 ELSE 0 END),
                   SUM(CASE WHEN c.calificacion = 3 THEN 1 ELSE 0 END),
                   SUM(CASE WHEN c.calificacion = 4 THEN 1 ELSE 0 END),
                   SUM(CASE WHEN c.calificacion = 5 THEN 1 ELSE 0 END)
            FROM CalificacionSesion c
            WHERE c.sesion.fecha BETWEEN :inicio AND :fin
            GROUP BY c.sesion.tutor.id, c.sesion.tutor.nombre
            ORDER BY AVG(c.calificacion) DESC, COUNT(c.id) DESC, c.sesion.tutor.nombre ASC
            """)
    List<Object[]> reporteCalificacionesPorTutor(@Param("inicio") LocalDate inicio,
                                                 @Param("fin") LocalDate fin);

    /**
     * MNT-10 — comentarios escritos por estudiantes en el rango de fechas de sesión.
     * Solo trae evaluaciones con comentario no vacío (RNF-02) y devuelve por fila:
     * [0] tutorId, [1] calificacionId, [2] nombreEstudiante, [3] calificacion,
     * [4] comentario, [5] fechaSesion.
     */
    @Query("""
            SELECT c.sesion.tutor.id,
                   c.id,
                   c.estudiante.nombre,
                   c.calificacion,
                   c.comentario,
                   c.sesion.fecha
            FROM CalificacionSesion c
            WHERE c.sesion.fecha BETWEEN :inicio AND :fin
              AND c.comentario IS NOT NULL
              AND TRIM(c.comentario) <> ''
            ORDER BY c.sesion.fecha DESC, c.id DESC
            """)
    List<Object[]> comentariosCalificacionesPorTutor(@Param("inicio") LocalDate inicio,
                                                     @Param("fin") LocalDate fin);
}
