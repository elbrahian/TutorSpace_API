package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.Notificacion;
import com.uco.tutorspace_api.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {
    private final NotificacionService notificacionService;

    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> todas(Authentication auth) {
        return ResponseEntity.ok(notificacionService.obtenerTodas(getUserId(auth)));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> noLeidas(Authentication auth) {
        return ResponseEntity.ok(notificacionService.obtenerNoLeidas(getUserId(auth)));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Authentication auth) {
        notificacionService.marcarLeida(id, getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}
