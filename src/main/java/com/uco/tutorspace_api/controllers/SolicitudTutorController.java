package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.CrearSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.RevisarSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.SolicitudTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import com.uco.tutorspace_api.service.SolicitudTutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/solicitudes-tutor")
@RequiredArgsConstructor
public class SolicitudTutorController {
    private final SolicitudTutorService solicitudTutorService;

    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<SolicitudTutorResponse> crearSolicitud(
            @Valid @RequestBody CrearSolicitudTutorRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudTutorService.crearSolicitud(getUserId(auth), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudTutorResponse>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitudTutor estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(solicitudTutorService.listarSolicitudes(estado, inicio, fin));
    }

    @GetMapping("/mia")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TUTOR')")
    public ResponseEntity<SolicitudTutorResponse> obtenerMiSolicitud(Authentication auth) {
        return ResponseEntity.of(
                solicitudTutorService.obtenerMiSolicitud(getUserId(auth))
        );
    }

    @PatchMapping("/{id}/revision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudTutorResponse> revisarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody RevisarSolicitudTutorRequest request) {
        return ResponseEntity.ok(solicitudTutorService.revisarSolicitud(id, request));
    }
}
