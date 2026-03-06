package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
}
