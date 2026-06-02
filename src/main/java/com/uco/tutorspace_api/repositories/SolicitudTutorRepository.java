package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.SolicitudTutor;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SolicitudTutorRepository extends JpaRepository<SolicitudTutor, Long>,
        JpaSpecificationExecutor<SolicitudTutor> {
    boolean existsBySolicitanteIdAndEstado(Long solicitanteId, EstadoSolicitudTutor estado);

    @Query("""
        SELECT DISTINCT solicitud FROM SolicitudTutor solicitud
        JOIN FETCH solicitud.solicitante solicitante
        LEFT JOIN FETCH solicitud.materiasSolicitadas solicitudMateria
        LEFT JOIN FETCH solicitudMateria.materia materia
        WHERE solicitud.id = :id
    """)
    Optional<SolicitudTutor> findByIdWithDetalle(@Param("id") Long id);
}
