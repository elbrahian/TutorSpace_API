package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Disponibilidad;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.domain.dto.TutorBusquedaResponse;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.repositories.DisponibilidadRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusquedaTutorService {
    private final TutorRepository tutorRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    // HU-05 — el estudiante busca tutores por materia
    // IMPORTANTE: nunca exponer franjas horarias exactas (CA-04)
    public Page<TutorBusquedaResponse> buscarPorMateria(Long materiaId, Pageable pageable) {
        return tutorRepository.findActivosByMateria(materiaId, pageable)
                .map(this::toRestrictedResponse);
    }

    private TutorBusquedaResponse toRestrictedResponse(Tutor tutor) {
        // Solo días únicos, nunca horas (CA-04 HU-05)
        List<String> diasDisponibles = disponibilidadRepository
                .findByTutorId(tutor.getId())
                .stream()
                .filter(d -> d.getEstado() == EstadoDisponibilidad.DISPONIBLE)
                .map(Disponibilidad::getDia)
                .distinct()
                .toList();

        List<MateriaResponse> materias = tutor.getMaterias().stream()
                .map(m -> new MateriaResponse(m.getId(), m.getNombre(), m.getCodigo()))
                .toList();

        return new TutorBusquedaResponse(
                tutor.getId(),
                tutor.getNombre(),
                tutor.getJornadaGeneral(),
                materias,
                diasDisponibles
        );
    }
}
