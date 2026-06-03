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
@RequestMapping("/estudiante")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Calificaciones", description = "Calificación de sesiones por el estudiante")
public class CalificacionSesionController {

    private final CalificacionSesionService calificacionSesionService;

    private Long getEstudianteId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Calificar sesión",
            description = "El estudiante califica una sesión COMPLETADA (1-5) con comentario opcional")
    @PostMapping("/sesiones/{sesionId}/calificacion")
    public ResponseEntity<CalificacionSesionResponse> calificar(
            @PathVariable Long sesionId,
            @Valid @RequestBody CalificacionSesionRequest request,
            Authentication auth) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calificacionSesionService.calificarSesion(getEstudianteId(auth), sesionId, request));
    }
}
