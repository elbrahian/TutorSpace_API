package com.uco.tutorspace_api.domain.dto;

import java.util.List;

public record TutorBusquedaResponse(
        Long id,
        String nombre,
        String jornadaGeneral,
        List<MateriaResponse> materias,
        List<String> diasDisponibles
) { }
