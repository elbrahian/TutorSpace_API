package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
