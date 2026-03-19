package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.dto.*;
import com.uco.tutorspace_api.service.MateriaService;
import com.uco.tutorspace_api.service.TutorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize( "hasRole('ADMIN')")
public class AdminController {
    private final TutorService tutorService;
    private final MateriaService materiaService;

    // --- Tutores ---
    @PostMapping("/tutores")
    public ResponseEntity<TutorResponse> registrarTutor(
            @Valid @RequestBody CrearTutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorService.registrarTutor(request));
    }

    @GetMapping("/tutores")
    public ResponseEntity<List<TutorResponse>> listarTutores() {
        return ResponseEntity.ok(tutorService.listarTutores());
    }

    @PatchMapping("/tutores/{id}/desactivar")
    public ResponseEntity<TutorResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.desactivarTutor(id));
    }

    @PatchMapping("/tutores/{id}/activar")
    public ResponseEntity<TutorResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.activarTutor(id));
    }

    // --- Materias ---
    @PostMapping("/materias")
    public ResponseEntity<MateriaResponse> crearMateria(
            @Valid @RequestBody CrearMateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaService.crearMateria(request));
    }

    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> listarMaterias() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }

    // --- Asignación de materias a tutor ---
    @PostMapping("/tutores/{id}/materias")
    public ResponseEntity<TutorResponse> asignarMateria(
            @PathVariable Long id,
            @Valid @RequestBody AsignarMateriaRequest request) {
        return ResponseEntity.ok(tutorService.asignarMateria(id, request));
    }

    @DeleteMapping("/tutores/{tutorId}/materias/{materiaId}")
    public ResponseEntity<TutorResponse> retirarMateria(
            @PathVariable Long tutorId,
            @PathVariable Long materiaId) {
        return ResponseEntity.ok(tutorService.retirarMateria(tutorId, materiaId));
    }

    public record ActualizarJornadaRequest(@NotBlank String jornadaGeneral) {}

    @PatchMapping("/tutores/{id}/jornada")
    public ResponseEntity<TutorResponse> actualizarJornada(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarJornadaRequest request) {
        return ResponseEntity.ok(tutorService.actualizarJornada(id, request.jornadaGeneral()));
    }
}
