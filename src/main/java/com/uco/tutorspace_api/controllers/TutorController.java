package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.CrearDisponibilidadRequest;
import com.uco.tutorspace_api.domain.dto.DisponibilidadResponse;
import com.uco.tutorspace_api.domain.dto.TutorResponse;
import com.uco.tutorspace_api.service.DisponibilidadService;
import com.uco.tutorspace_api.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TUTOR')")
@Tag(name = "Tutor", description = "Gestión de disponibilidad y perfil del tutor")
public class TutorController {
    private final DisponibilidadService disponibilidadService;
    private final TutorService tutorService;

    private Long getTutorId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Crear disponibilidad", description = "Crea una nueva franja de disponibilidad horaria")
    @PostMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> crear(
            @Valid @RequestBody CrearDisponibilidadRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibilidadService.crearDisponibilidad(getTutorId(auth), request));
    }

    @Operation(summary = "Listar disponibilidad", description = "Obtiene todas las franjas de disponibilidad del tutor")
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(disponibilidadService.listarPorTutor(getTutorId(auth)));
    }

    @Operation(summary = "Eliminar disponibilidad", description = "Elimina una franja de disponibilidad")
    @DeleteMapping("/disponibilidad/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        disponibilidadService.eliminarDisponibilidad(id, getTutorId(auth));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ver perfil", description = "Obtiene el perfil del tutor autenticado")
    @GetMapping("/perfil")
    public ResponseEntity<TutorResponse> verPerfil(Authentication auth) {
        Long tutorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        return ResponseEntity.ok(tutorService.getTutorById(tutorId));
    }
}
