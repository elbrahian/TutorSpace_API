package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("""
    SELECT s FROM Sesion s
    WHERE s.tutor.id = :tutorId
      AND (:estado IS NULL OR s.estado = :estado)
      AND (:fechaInicio IS NULL OR s.fecha >= :fechaInicio)
      AND (:fechaFin IS NULL OR s.fecha <= :fechaFin)
    """)
    Page<Sesion> findByTutorIdWithFilters(
            @Param("tutorId") Long tutorId,
            @Param("estado") EstadoSesion estado,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable
    );
}
