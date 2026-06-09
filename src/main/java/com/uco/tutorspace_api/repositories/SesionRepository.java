package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByTutorId(Long id);
    List<Sesion> findByEstudianteId(Long id);
    List<Sesion> findByEstudianteIdAndFechaBetween(Long estudianteId, LocalDate inicio, LocalDate fin);
    List<Sesion> findByEstudianteIdAndEstado(Long estudianteId, EstadoSesion estado);

    /**
     * MNT-12 — sesiones APROBADA cuya hora de fin ya pasó, candidatas a marcarse
     * automáticamente como COMPLETADA. Incluye las de días anteriores (s.fecha < hoy)
     * y las de hoy cuya horaFin ya transcurrió.
     */
    @Query("""
            SELECT s FROM Sesion s
            WHERE s.estado = com.uco.tutorspace_api.domain.enums.EstadoSesion.APROBADA
              AND (s.fecha < :hoy OR (s.fecha = :hoy AND s.horaFin <= :ahora))
            """)
    List<Sesion> findAprobadasVencidas(@Param("hoy") LocalDate hoy,
                                       @Param("ahora") LocalTime ahora);

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
    // Total sesiones COMPLETADAS
    @Query("SELECT COUNT(s) FROM Sesion s WHERE s.estudiante.id = :estudianteId AND s.estado = com.uco.tutorspace_api.domain.enums.EstadoSesion.COMPLETADA")
    long countSesionesCompletadasByEstudiante(@Param("estudianteId") Long estudianteId);

    // Materia más consultada — se obtiene de las materias asignadas al tutor de cada sesión
    @Query("""
            SELECT m.nombre FROM Sesion s
            JOIN s.tutor t
            JOIN t.materias m
            WHERE s.estudiante.id = :estudianteId
            AND s.estado = com.uco.tutorspace_api.domain.enums.EstadoSesion.COMPLETADA
            GROUP BY m.id, m.nombre
            ORDER BY COUNT(s) DESC
            """)
    List<String> findMateriasMasConsultadas(@Param("estudianteId") Long estudianteId, Pageable pageable);

    // Tutor frecuente
    @Query("SELECT s.tutor.nombre FROM Sesion s WHERE s.estudiante.id = :estudianteId AND s.estado = com.uco.tutorspace_api.domain.enums.EstadoSesion.COMPLETADA GROUP BY s.tutor.id ORDER BY COUNT(s) DESC")
    List<String> findTutoresFrecuentes(@Param("estudianteId") Long estudianteId, Pageable pageable);

    // Actividad mensual
    @Query("SELECT MONTH(s.fecha), COUNT(s) FROM Sesion s WHERE s.estudiante.id = :estudianteId AND s.estado = com.uco.tutorspace_api.domain.enums.EstadoSesion.COMPLETADA AND YEAR(s.fecha) = :anio GROUP BY MONTH(s.fecha) ORDER BY MONTH(s.fecha)")
    List<Object[]> findActividadMensual(@Param("estudianteId") Long estudianteId, @Param("anio") int anio);

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
