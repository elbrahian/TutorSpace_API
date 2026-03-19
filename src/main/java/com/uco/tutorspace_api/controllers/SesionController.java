package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.CambiarEstadoSesionRequest;
import com.uco.tutorspace_api.domain.dto.CrearSesionRequest;
import com.uco.tutorspace_api.domain.dto.SesionResponse;
import com.uco.tutorspace_api.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "Cambiar estado de sesión", description = "El tutor cambia el estado: PENDIENTE → APROBADA o CANCELADA")
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
}
