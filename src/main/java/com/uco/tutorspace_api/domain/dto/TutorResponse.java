package com.uco.tutorspace_api.domain.dto;

import com.uco.tutorspace_api.domain.enums.EstadoUsuario;

import java.util.List;

public record TutorResponse(
        Long id,
        String nombre,
        String email,
        String jornadaGeneral,
        EstadoUsuario estado,
        List<MateriaResponse> materias
) {
}
