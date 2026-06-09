package com.uco.tutorspace_api.domain.dto;

/**
 * Métricas de uso de la plataforma para un rol específico (MNT-11).
 *
 * @param rol              Rol evaluado: ESTUDIANTE, TUTOR o ADMIN.
 * @param usuariosActivos  Usuarios distintos del rol con al menos una acción en el período.
 * @param sesiones         Sesiones en las que participó el rol dentro del período.
 * @param mensajesEnviados Mensajes enviados por usuarios del rol en el período.
 */
public record UsoPorRolResponse(
        String rol,
        long usuariosActivos,
        long sesiones,
        long mensajesEnviados
) {
}
