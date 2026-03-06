package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.dto.CrearMateriaRequest;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaService {
    private final MateriaRepository materiaRepository;

    public MateriaResponse crearMateria(CrearMateriaRequest request) {
        if (materiaRepository.findByCodigo(request.codigo()).isPresent()) {
            throw new RuntimeException("Ya existe una materia con ese código");
        }

        Materia materia = new Materia();
        materia.setNombre(request.nombre());
        materia.setCodigo(request.codigo().toUpperCase());

        Materia guardada = materiaRepository.save(materia);
        return toResponse(guardada);
    }

    public List<MateriaResponse> listarMaterias() {
        return materiaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MateriaResponse toResponse(Materia materia) {
        return new MateriaResponse(materia.getId(), materia.getNombre(), materia.getCodigo());
    }
}
