package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSesion;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un registro de auditoría de una sesión.
 *
 * sesionId       Identificador único de la sesión auditada.
 * tutor          Nombre del tutor asociado a la sesión.
 * estudiante     Nombre del estudiante asociado a la sesión.
 * estadoAnterior Estado que tenía la sesión antes del cambio.
 * estadoNuevo    Estado al que pasó la sesión tras el cambio.
 * fechaCambio    Fecha y hora exacta en que se realizó el cambio de estado.
 */
public record AuditoriaSesionResponse(

        Long sesionId,

        String tutor,

        String estudiante,

        EstadoSesion estadoAnterior,

        EstadoSesion estadoNuevo,

        LocalDateTime fechaCambio

) {}