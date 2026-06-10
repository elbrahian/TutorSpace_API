package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudTutorResponse(
        Long id,
        Long solicitanteId,
        String nombreSolicitante,
        String emailSolicitante,
        String justificacion,
        String observaciones,
        EstadoSolicitudTutor estado,
        LocalDateTime fechaEnvio,
        LocalDateTime fechaRevision,
        List<MateriaResponse> materias
) {
}
