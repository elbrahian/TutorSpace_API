package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.utils.HorarioValidator;
import com.uco.tutorspace_api.domain.Disponibilidad;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.CrearDisponibilidadRequest;
import com.uco.tutorspace_api.domain.dto.DisponibilidadResponse;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.repositories.DisponibilidadRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private static final String DISPONIBILIDAD_NO_ENCONTRADA = "Disponibilidad no encontrada";

    private final DisponibilidadRepository disponibilidadRepository;
    private final TutorRepository tutorRepository;
    private final HorarioValidator horarioValidator;

    public DisponibilidadResponse crearDisponibilidad(Long tutorId,
                                                      CrearDisponibilidadRequest request) {
        if (!horarioValidator.esRangoValido(request.horaInicio(), request.horaFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        List<Disponibilidad> solapadas = disponibilidadRepository.findSolapadas(
                tutorId,
                request.dia(),
                request.horaInicio(),
                request.horaFin()
        );

        if (!solapadas.isEmpty()) {
            throw new IllegalStateException("La franja se solapa con una disponibilidad existente");
        }

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setTutor(tutor);
        disponibilidad.setDia(request.dia().toUpperCase());
        disponibilidad.setHoraInicio(request.horaInicio());
        disponibilidad.setHoraFin(request.horaFin());
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);

        return toResponse(disponibilidadRepository.save(disponibilidad));
    }

    public List<DisponibilidadResponse> listarPorTutor(Long tutorId) {
        return disponibilidadRepository.findByTutorId(tutorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void eliminarDisponibilidad(Long disponibilidadId, Long tutorId) {
        Disponibilidad d = disponibilidadRepository.findById(disponibilidadId)
                .orElseThrow(() -> new NoSuchElementException(DISPONIBILIDAD_NO_ENCONTRADA));

        if (!d.getTutor().getId().equals(tutorId)) {
            throw new IllegalStateException("No tienes permiso para eliminar esta disponibilidad");
        }

        if (d.getEstado() == EstadoDisponibilidad.BLOQUEADA) {
            throw new IllegalStateException("No se puede eliminar una franja bloqueada por una sesión");
        }

        disponibilidadRepository.delete(d);
    }

    public void bloquearFranja(Long disponibilidadId) {
        Disponibilidad d = disponibilidadRepository.findById(disponibilidadId)
                .orElseThrow(() -> new NoSuchElementException(DISPONIBILIDAD_NO_ENCONTRADA));
        d.setEstado(EstadoDisponibilidad.BLOQUEADA);
        disponibilidadRepository.save(d);
    }

    public void liberarFranja(Long disponibilidadId) {
        Disponibilidad d = disponibilidadRepository.findById(disponibilidadId)
                .orElseThrow(() -> new NoSuchElementException(DISPONIBILIDAD_NO_ENCONTRADA));
        d.setEstado(EstadoDisponibilidad.DISPONIBLE);
        disponibilidadRepository.save(d);
    }

    public DisponibilidadResponse toResponse(Disponibilidad d) {
        return new DisponibilidadResponse(
                d.getId(), d.getDia(), d.getHoraInicio(), d.getHoraFin(), d.getEstado()
        );
    }
}