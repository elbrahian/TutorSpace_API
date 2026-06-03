package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
}
