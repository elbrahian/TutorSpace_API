package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Sesion;
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
}
