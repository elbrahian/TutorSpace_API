package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.domain.dto.TutorBusquedaResponse;
import com.uco.tutorspace_api.service.BusquedaTutorService;
import com.uco.tutorspace_api.service.MateriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Estudiante", description = "Búsqueda de tutores y gestión de materias")
public class EstudianteController {
    private final BusquedaTutorService busquedaTutorService;
    private final MateriaService materiaService;

    private Long getEstudianteId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Buscar tutores", description = "Busca tutores disponibles por materia con paginación")
    @GetMapping("/tutores/buscar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<Page<TutorBusquedaResponse>> buscar(
            @RequestParam Long materiaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(busquedaTutorService.buscarPorMateria(materiaId, pageable));
    }

    @Operation(summary = "Listar materias", description = "Obtiene todas las materias disponibles para filtrar búsqueda")
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> listarMaterias() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }
}
