package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByTutorId(Long id);
    List<Sesion> findByEstudianteId(Long id);
    List<Sesion> findByEstudianteIdAndFechaBetween(Long estudianteId, LocalDate inicio, LocalDate fin);

    @Query("""
            SELECT s FROM Sesion s
            WHERE s.tutor.id IN :tutorIds
            AND (:fechaInicio IS NULL OR s.fecha >= :fechaInicio)
            AND (:fechaFin IS NULL OR s.fecha <= :fechaFin)
            """)
    List<Sesion> findByTutorIdsAndRango(
            @Param("tutorIds") List<Long> tutorIds,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("""
        SELECT m.nombre, COUNT(s.id)
        FROM Sesion s
        JOIN s.tutor t
        JOIN t.materias m
        WHERE (:fechaInicio IS NULL OR s.fecha >= :fechaInicio)
        AND (:fechaFin IS NULL OR s.fecha <= :fechaFin)
        GROUP BY m.id, m.nombre
        """)
    List<Object[]> countSesionesByMateria(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Proyección de actividad de sesiones para el reporte de uso por rol (MNT-11).
     * Devuelve filas [fecha, estudianteId, tutorId] dentro del rango indicado.
     *
     * El rango siempre llega resuelto (sin null): el servicio sustituye los
     * límites ausentes por cotas amplias. Esto evita el patrón
     * ":param IS NULL OR ...", que en PostgreSQL falla con
     * "could not determine data type of parameter" cuando el bind es null.
     */
    @Query("""
        SELECT s.fecha, s.estudiante.id, s.tutor.id
        FROM Sesion s
        WHERE s.fecha >= :fechaInicio AND s.fecha <= :fechaFin
        """)
    List<Object[]> findActividadSesiones(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
