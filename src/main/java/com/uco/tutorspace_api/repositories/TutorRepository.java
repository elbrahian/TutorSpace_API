package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    @Query("""
        SELECT DISTINCT t From Tutor t 
        JOIN t.materias m
        JOIN t.disponibilidades d 
        WHERE m.id = :materiaId
        AND t.estado = 'ACTIVO'
        AND d.estado = 'DISPONIBLE'
    """)
    Page<Tutor> findActivosByMateria(@Param("materiaId") Long materiaId, Pageable pageable);
}
