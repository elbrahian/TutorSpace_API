package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.*;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.service.EvaluacionEstudianteService;
import com.uco.tutorspace_api.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sesiones")
@RequiredArgsConstructor
@Tag(name = "Sesiones", description = "Gestión de sesiones de tutoría")
public class SesionController {
    private final SesionService sesionService;
    private final EvaluacionEstudianteService evaluacionEstudianteService;


    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Crear sesión", description = "El tutor crea una sesión. Requiere chat previo con el estudiante.")
    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SesionResponse> crear(
            @Valid @RequestBody CrearSesionRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sesionService.crearSesion(getUserId(auth), request));
    }

    @Operation(summary = "Cambiar estado de sesión", description = "El tutor cambia el estado: PENDIENTE → APROBADA o CANCELADA; APROBADA → COMPLETADA o CANCELADA")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SesionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoSesionRequest request,
            Authentication auth) {
        return ResponseEntity.ok(sesionService.cambiarEstado(id, getUserId(auth), request));
    }

    @GetMapping("/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<SesionResponse>> porTutor(Authentication auth) {
        return ResponseEntity.ok(sesionService.obtenerPorTutor(getUserId(auth)));
    }

    @GetMapping("/estudiante")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<List<SesionResponse>> porEstudiante(Authentication auth) {
        return ResponseEntity.ok(sesionService.obtenerPorEstudiante(getUserId(auth)));
    }

    @GetMapping("/estudiante/rango")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<List<SesionResponse>> porRango(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fin,
            Authentication auth) {
        return ResponseEntity.ok(
                sesionService.obtenerPorEstudianteYFecha(getUserId(auth), inicio, fin)
        );
    }

    @PostMapping("/{id}/evaluacion-estudiante")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Evaluar estudiante",
            description = "El tutor evalúa el desempeño del estudiante tras una sesión completada (RF-03, HU-M13)")
    public ResponseEntity<EvaluacionEstudianteResponse> evaluarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody EvaluacionEstudianteRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluacionEstudianteService.evaluarEstudiante(id, getUserId(auth), request));
    }

    @GetMapping("/{id}/evaluacion-estudiante")
    @Operation(summary = "Obtener evaluación de sesión",
            description = "Obtiene la evaluación registrada para una sesión (RF-06)")
    public ResponseEntity<EvaluacionEstudianteResponse> obtenerEvaluacion(
            @PathVariable Long id) {
        return ResponseEntity.ok(evaluacionEstudianteService.obtenerEvaluacionPorSesion(id));
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SesionResponse>> getMisSesiones(
            @RequestParam(required = false) EstadoSesion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sort,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<SesionResponse> sesiones = sesionService.getSesionesByTutorWithFilters(
                userId, estado, fechaInicio, fechaFin, pageable
        );
        return ResponseEntity.ok(sesiones);
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<SesionResponse> completarSesion(
            @PathVariable Long id,
            Authentication authentication) {
        Long tutorId = getUserId(authentication);
        return ResponseEntity.ok(sesionService.completarSesion(id, tutorId));
    }

}
