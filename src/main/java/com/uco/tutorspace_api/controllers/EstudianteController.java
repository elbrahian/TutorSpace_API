package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.domain.dto.TutorBusquedaResponse;
import com.uco.tutorspace_api.service.BusquedaTutorService;
import com.uco.tutorspace_api.service.MateriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estudiante")
@RequiredArgsConstructor
@PreAuthorize( "hasRole('ESTUDIANTE')")
public class EstudianteController {
    private final BusquedaTutorService busquedaTutorService;
    private final MateriaService materiaService;

    private Long getEstudianteId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }
    // HU-05 — buscar tutores por materia (paginado, sin franjas exactas)
    @GetMapping("/tutores/buscar")
    public ResponseEntity<Page<TutorBusquedaResponse>> buscar(
            @RequestParam Long materiaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(busquedaTutorService.buscarPorMateria(materiaId, pageable));
    }

    // Listar materias disponibles para filtrar búsqueda
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> listarMaterias() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }
}
