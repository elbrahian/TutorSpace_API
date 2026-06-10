package com.uco.tutorspace_api.domain.dto;

import java.util.List;

/**
 * Reporte de uso de la plataforma diferenciado por rol (MNT-11).
 *
 * @param estudiantesActivos   Total de estudiantes con al menos una acción en el período.
 * @param tutoresActivos       Total de tutores con al menos una acción en el período.
 * @param adminsActivos        Total de administradores con al menos una acción en el período.
 * @param totalSesionesCreadas Sesiones creadas en el período.
 * @param totalMensajesEnviados Mensajes enviados en el período.
 * @param metricasPorRol       Desglose de métricas por rol para la tabla del reporte.
 * @param actividadSemanal     Serie temporal semanal por rol para el gráfico de actividad.
 */
public record ReporteUsoResponse(
        long estudiantesActivos,
        long tutoresActivos,
        long adminsActivos,
        long totalSesionesCreadas,
        long totalMensajesEnviados,
        List<UsoPorRolResponse> metricasPorRol,
        List<ActividadSemanalResponse> actividadSemanal
) {
}
