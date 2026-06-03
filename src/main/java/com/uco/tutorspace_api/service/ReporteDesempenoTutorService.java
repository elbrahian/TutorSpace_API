package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.ReporteDesempenoTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteDesempenoTutorService {
    private final TutorRepository tutorRepository;
    private final SesionRepository sesionRepository;

    public List<ReporteDesempenoTutorResponse> obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Tutor> tutoresActivos = tutorRepository.findByEstadoOrderByNombreAsc(EstadoUsuario.ACTIVO);

        if (tutoresActivos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tutorIds = tutoresActivos.stream()
                .map(Tutor::getId)
                .toList();

        Map<Long, List<Sesion>> sesionesPorTutor = sesionRepository.findByTutorIdsAndRango(tutorIds, fechaInicio, fechaFin)
                .stream()
                .collect(Collectors.groupingBy(sesion -> sesion.getTutor().getId()));

        return tutoresActivos.stream()
                .map(tutor -> construirFila(tutor, sesionesPorTutor.getOrDefault(tutor.getId(), Collections.emptyList())))
                .toList();
    }

    private ReporteDesempenoTutorResponse construirFila(Tutor tutor, List<Sesion> sesiones) {
        long totalSesiones = sesiones.size();
        long sesionesCanceladas = sesiones.stream()
                .filter(sesion -> sesion.getEstado() == EstadoSesion.CANCELADA)
                .count();
        double porcentajeCancelacion = totalSesiones == 0
                ? 0
                : (sesionesCanceladas * 100.0) / totalSesiones;

        return new ReporteDesempenoTutorResponse(
                tutor.getId(),
                tutor.getNombre(),
                totalSesiones,
                null,
                sesionesCanceladas,
                redondear(porcentajeCancelacion),
                null
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
