package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    Page<Mensaje>  findByChatIdOrderByFechaAsc(Long chatId, Pageable pageable);

    /**
     * Proyección de actividad de mensajes para el reporte de uso por rol (MNT-11).
     * Devuelve filas [fecha, emisorId, rolEmisor] dentro del rango indicado.
     *
     * El rango siempre llega resuelto (sin null): el servicio sustituye los
     * límites ausentes por cotas amplias. Esto evita el patrón
     * ":param IS NULL OR ...", que en PostgreSQL falla con
     * "could not determine data type of parameter" cuando el bind es null.
     */
    @Query("""
        SELECT m.fecha, m.emisor.id, m.emisor.rol
        FROM Mensaje m
        WHERE m.fecha >= :inicio AND m.fecha <= :fin
        """)
    List<Object[]> findActividadMensajes(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
