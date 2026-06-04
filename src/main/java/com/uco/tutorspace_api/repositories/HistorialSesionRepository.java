package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.HistorialSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// se agrega el JpaSpecificationExecutor para que funcione el Specification
public interface HistorialSesionRepository extends JpaRepository<HistorialSesion, Long>, JpaSpecificationExecutor<HistorialSesion> {
    List<HistorialSesion> findBySesionId(Long id);

    // Se crea el findAll para poder consultar todo el historial
    Page<HistorialSesion> findAll(Pageable pageable);
}
