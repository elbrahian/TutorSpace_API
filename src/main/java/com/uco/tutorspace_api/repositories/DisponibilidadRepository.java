package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByTutorId(Long id);

    @Query("""
        SELECT d FROM Disponibilidad d
        WHERE d.tutor.id = :tutorId
        AND d.dia = :dia
        AND d.estado = 'DISPONIBLE'
        AND (d.horaInicio < :horaFin AND d.horaFin > :horaInicio)
    """)
    List<Disponibilidad> findSolapadas(
            @Param("tutorId") Long tutorId,
            @Param("dia") String dia,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
}
