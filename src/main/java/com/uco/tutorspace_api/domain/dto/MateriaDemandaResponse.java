package com.uco.tutorspace_api.domain.dto;

public record MateriaDemandaResponse(
        String materia,
        int sesionesSolicitadas,
        int tutoresDisponibles,
        Double tasaCobertura
) {}
