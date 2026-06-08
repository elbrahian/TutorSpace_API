package com.uco.tutorspace_api.domain.dto;

import java.time.LocalDate;

/**
 * Actividad agregada de una semana, diferenciada por rol (MNT-11).
 * Alimenta el gráfico de líneas de actividad semanal por rol.
 *
 * @param semana       Etiqueta legible de la semana (dd/MM del lunes que la inicia).
 * @param inicioSemana Fecha del lunes que inicia la semana (para ordenar en el cliente).
 * @param estudiante   Acciones (sesiones + mensajes) de estudiantes en la semana.
 * @param tutor        Acciones (sesiones + mensajes) de tutores en la semana.
 * @param admin        Acciones (mensajes) de administradores en la semana.
 */
public record ActividadSemanalResponse(
        String semana,
        LocalDate inicioSemana,
        long estudiante,
        long tutor,
        long admin
) {
}
