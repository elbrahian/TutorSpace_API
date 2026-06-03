package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalificacionSesionRepository extends JpaRepository<CalificacionSesion,Long> {
    boolean existsBySesionIdAndEstudianteId(Long sesionId, Long estudianteId);
}
