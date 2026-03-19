package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.CrearDisponibilidadRequest;
import com.uco.tutorspace_api.domain.dto.DisponibilidadResponse;
import com.uco.tutorspace_api.domain.dto.TutorResponse;
import com.uco.tutorspace_api.service.DisponibilidadService;
import com.uco.tutorspace_api.service.TutorService;
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
@PreAuthorize( "hasRole('TUTOR')")
public class TutorController {
    private final DisponibilidadService disponibilidadService;
    private final TutorService tutorService;

    // Obtener el tutorId del token JWT para seguridad
    private Long getTutorId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @PostMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> crear(
            @Valid @RequestBody CrearDisponibilidadRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibilidadService.crearDisponibilidad(getTutorId(auth), request));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(disponibilidadService.listarPorTutor(getTutorId(auth)));
    }

    @DeleteMapping("/disponibilidad/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        disponibilidadService.eliminarDisponibilidad(id, getTutorId(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/perfil")
    public ResponseEntity<TutorResponse> verPerfil(Authentication auth) {
        Long tutorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        return ResponseEntity.ok(tutorService.getTutorById(tutorId));
    }
}
