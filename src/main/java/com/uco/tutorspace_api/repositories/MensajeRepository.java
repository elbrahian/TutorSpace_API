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
     */
    @Query("""
        SELECT m.fecha, m.emisor.id, m.emisor.rol
        FROM Mensaje m
        WHERE (:inicio IS NULL OR m.fecha >= :inicio)
        AND (:fin IS NULL OR m.fecha <= :fin)
        """)
    List<Object[]> findActividadMensajes(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
