package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.Notificacion;
import com.uco.tutorspace_api.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del usuario")
public class NotificacionController {
    private final NotificacionService notificacionService;

    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "Obtener todas", description = "Obtiene todas las notificaciones del usuario")
    @GetMapping
    public ResponseEntity<List<Notificacion>> todas(Authentication auth) {
        return ResponseEntity.ok(notificacionService.obtenerTodas(getUserId(auth)));
    }

    @Operation(summary = "Obtener no leídas", description = "Obtiene las notificaciones no leídas del usuario")
    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> noLeidas(Authentication auth) {
        return ResponseEntity.ok(notificacionService.obtenerNoLeidas(getUserId(auth)));
    }

    @Operation(summary = "Marcar como leída", description = "Marca una notificación como leída")
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Authentication auth) {
        notificacionService.marcarLeida(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}
