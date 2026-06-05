package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    @Query("""
    SELECT DISTINCT t FROM Tutor t 
    JOIN FETCH t.materias m
    LEFT JOIN t.disponibilidades d
    WHERE m.id = :materiaId
    AND t.estado = 'ACTIVO'
    AND (d IS NULL OR d.estado = 'DISPONIBLE')
""")
    Page<Tutor> findActivosByMateria(@Param("materiaId") Long materiaId, Pageable pageable);

    @Query("SELECT t FROM Tutor t LEFT JOIN FETCH t.materias WHERE t.id = :id")
    Optional<Tutor> findByIdWithMaterias(@Param("id") Long id);

    List<Tutor> findByEstadoOrderByNombreAsc(EstadoUsuario estado);
}
