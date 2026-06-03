package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.HistorialSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HistorialSesionRepository extends JpaRepository<HistorialSesion, Long> {
    List<HistorialSesion> findBySesionId(Long id);
    Page<HistorialSesion> findAll(Pageable pageable);
}
