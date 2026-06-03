package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionRequest;
import com.uco.tutorspace_api.domain.dto.CalificacionSesionResponse;
import com.uco.tutorspace_api.service.CalificacionSesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sesiones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Evaluación de tutorías", description = "Evaluación de sesiones por el estudiante (MNT-12)")
public class CalificacionSesionController {

    private final CalificacionSesionService calificacionSesionService;

    private Long getEstudianteId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Evaluar sesión",
            description = "El estudiante participante evalúa una sesión COMPLETADA: " +
                    "calificación entera 1-5 y comentario opcional (máx. 500). " +
                    "Solo se permite una evaluación por sesión/estudiante (409 si ya existe).")
    @PostMapping("/{id}/evaluacion")
    public ResponseEntity<CalificacionSesionResponse> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody CalificacionSesionRequest request,
            Authentication auth) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calificacionSesionService.calificarSesion(getEstudianteId(auth), id, request));
    }
}
