package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Notificacion;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.TipoNotificacion;
import com.uco.tutorspace_api.repositories.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket

    public void enviarNotificacion(Usuario usuario, TipoNotificacion tipo, String mensaje) {
        // 1. Guardar en BD
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        notificacion.setFecha(LocalDateTime.now());

        Notificacion guardada = notificacionRepository.save(notificacion);

        // 2. Push en tiempo real por WebSocket al canal del usuario
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/" + usuario.getId(),
                guardada
        );
    }

    public List<Notificacion> obtenerNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public List<Notificacion> obtenerTodas(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public void marcarLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso sobre esta notificación");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
}
