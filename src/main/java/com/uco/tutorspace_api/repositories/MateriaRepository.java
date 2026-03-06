package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    Optional<Materia> findByCodigo(String codigo);

}
