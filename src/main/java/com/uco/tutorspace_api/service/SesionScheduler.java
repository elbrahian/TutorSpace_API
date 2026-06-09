package com.uco.tutorspace_api.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * MNT-12 — dispara periódicamente la auto-completación de sesiones vencidas.
 * Solo orquesta el disparo; la lógica vive en {@link SesionService} para poder
 * testearla sin el scheduler. Supuesto de despliegue: una sola instancia
 * (si se escala a varias réplicas, añadir un lock distribuido tipo ShedLock).
 */
@Component
@RequiredArgsConstructor
public class SesionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SesionScheduler.class);

    private final SesionService sesionService;

    // Cada minuto, en el segundo 0. Latencia máxima ~1 min respecto a la hora de fin.
    @Scheduled(cron = "0 * * * * *")
    public void completarSesionesVencidas() {
        int completadas = sesionService.completarSesionesVencidas();
        if (completadas > 0) {
            log.info("Auto-completadas {} sesiones cuya hora de fin ya pasó", completadas);
        }
    }
}
