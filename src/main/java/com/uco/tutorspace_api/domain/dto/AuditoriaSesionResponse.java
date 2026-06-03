package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSesion;

import java.time.LocalDateTime;

public record AuditoriaSesionResponse(

        Long sesionId,

        String tutor,

        String estudiante,

        EstadoSesion estadoAnterior,

        EstadoSesion estadoNuevo,

        LocalDateTime fechaCambio

) {}