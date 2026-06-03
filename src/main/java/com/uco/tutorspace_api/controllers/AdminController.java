package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.dto.*;
import com.uco.tutorspace_api.service.AuditoriaService;
import com.uco.tutorspace_api.service.MateriaService;
import com.uco.tutorspace_api.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración", description = "Gestión de tutores, materias y configuración del sistema")
public class AdminController {
    private final TutorService tutorService;
    private final MateriaService materiaService;

    //NUEVO
    private final AuditoriaService auditoriaService;


    // NUEVO
    @GetMapping("/auditoria/sesiones")
    public ResponseEntity<Page<AuditoriaSesionResponse>> auditoria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("fechaCambio").descending()
        );

        return ResponseEntity.ok(
                auditoriaService.obtenerAuditoria(pageable)
        );
    }

    @Operation(summary = "Registrar tutor", description = "Crea un nuevo tutor en el sistema")
    @PostMapping("/tutores")
    public ResponseEntity<TutorResponse> registrarTutor(
            @Valid @RequestBody CrearTutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorService.registrarTutor(request));
    }

    @Operation(summary = "Listar tutores", description = "Obtiene todos los tutores registrados")
    @GetMapping("/tutores")
    public ResponseEntity<List<TutorResponse>> listarTutores() {
        return ResponseEntity.ok(tutorService.listarTutores());
    }

    @Operation(summary = "Desactivar tutor", description = "Desactiva un tutor por su ID")
    @PatchMapping("/tutores/{id}/desactivar")
    public ResponseEntity<TutorResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.desactivarTutor(id));
    }

    @Operation(summary = "Activar tutor", description = "Activa un tutor por su ID")
    @PatchMapping("/tutores/{id}/activar")
    public ResponseEntity<TutorResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.activarTutor(id));
    }

    @Operation(summary = "Crear materia", description = "Crea una nueva materia en el sistema")
    @PostMapping("/materias")
    public ResponseEntity<MateriaResponse> crearMateria(
            @Valid @RequestBody CrearMateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaService.crearMateria(request));
    }

    @Operation(summary = "Listar materias", description = "Obtiene todas las materias registradas")
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> listarMaterias() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }

    @Operation(summary = "Asignar materia a tutor", description = "Asigna una materia a un tutor específico")
    @PostMapping("/tutores/{id}/materias")
    public ResponseEntity<TutorResponse> asignarMateria(
            @PathVariable Long id,
            @Valid @RequestBody AsignarMateriaRequest request) {
        return ResponseEntity.ok(tutorService.asignarMateria(id, request));
    }

    @Operation(summary = "Retirar materia de tutor", description = "Retira una materia asignada a un tutor")
    @DeleteMapping("/tutores/{tutorId}/materias/{materiaId}")
    public ResponseEntity<TutorResponse> retirarMateria(
            @PathVariable Long tutorId,
            @PathVariable Long materiaId) {
        return ResponseEntity.ok(tutorService.retirarMateria(tutorId, materiaId));
    }

    public record ActualizarJornadaRequest(@NotBlank String jornadaGeneral) {}

    @Operation(summary = "Actualizar jornada", description = "Actualiza la jornada laboral de un tutor")
    @PatchMapping("/tutores/{id}/jornada")
    public ResponseEntity<TutorResponse> actualizarJornada(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarJornadaRequest request) {
        return ResponseEntity.ok(tutorService.actualizarJornada(id, request.jornadaGeneral()));
    }
}
