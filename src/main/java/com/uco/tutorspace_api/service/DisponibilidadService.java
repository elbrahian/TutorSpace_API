package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.Utils.HorarioValidator;
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

@Service
@RequiredArgsConstructor
public class DisponibilidadService {
    private final DisponibilidadRepository disponibilidadRepository;
    private final TutorRepository tutorRepository;
    private final HorarioValidator horarioValidator;

    public DisponibilidadResponse crearDisponibilidad(Long tutorId,
                                                      CrearDisponibilidadRequest request) {
        // Validar rango horario
        if (!horarioValidator.esRangoValido(request.horaInicio(), request.horaFin())) {
            throw new RuntimeException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Validar solapamiento con franjas existentes del tutor
        List<Disponibilidad> solapadas = disponibilidadRepository.findSolapadas(
                tutorId,
                request.dia(),
                request.horaInicio(),
                request.horaFin()
        );

        if (!solapadas.isEmpty()) {
            throw new RuntimeException("La franja se solapa con una disponibilidad existente");
        }

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Disponibilidad no encontrada"));

        // Verificar que pertenece al tutor que hace la petición
        if (!d.getTutor().getId().equals(tutorId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta disponibilidad");
        }

        if (d.getEstado() == EstadoDisponibilidad.BLOQUEADA) {
            throw new RuntimeException("No se puede eliminar una franja bloqueada por una sesión");
        }

        disponibilidadRepository.delete(d);
    }

    // Llamado automáticamente desde SesionService (Sprint 3)
    public void bloquearFranja(Long disponibilidadId) {
        Disponibilidad d = disponibilidadRepository.findById(disponibilidadId)
                .orElseThrow(() -> new RuntimeException("Disponibilidad no encontrada"));
        d.setEstado(EstadoDisponibilidad.BLOQUEADA);
        disponibilidadRepository.save(d);
    }

    // Llamado automáticamente cuando se cancela sesión (Sprint 3)
    public void liberarFranja(Long disponibilidadId) {
        Disponibilidad d = disponibilidadRepository.findById(disponibilidadId)
                .orElseThrow(() -> new RuntimeException("Disponibilidad no encontrada"));
        d.setEstado(EstadoDisponibilidad.DISPONIBLE);
        disponibilidadRepository.save(d);
    }

    public DisponibilidadResponse toResponse(Disponibilidad d) {
        return new DisponibilidadResponse(
                d.getId(), d.getDia(), d.getHoraInicio(), d.getHoraFin(), d.getEstado()
        );
    }
}
