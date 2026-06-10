package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.ReporteDesempenoTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.repositories.CalificacionSesionRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteDesempenoTutorService {
    private final TutorRepository tutorRepository;
    private final SesionRepository sesionRepository;
    private final CalificacionSesionRepository calificacionSesionRepository;
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1970, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    public List<ReporteDesempenoTutorResponse> obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Tutor> tutoresActivos = tutorRepository.findByEstadoOrderByNombreAsc(EstadoUsuario.ACTIVO);

        if (tutoresActivos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tutorIds = tutoresActivos.stream()
                .map(Tutor::getId)
                .toList();

        LocalDate inicio = fechaInicio == null ? FECHA_MINIMA : fechaInicio;
        LocalDate fin = fechaFin == null ? FECHA_MAXIMA : fechaFin;
        Map<Long, TotalesSesiones> totalesPorTutor = obtenerTotalesPorTutor(tutorIds, inicio, fin);

        return tutoresActivos.stream()
                .map(tutor -> construirFila(tutor, totalesPorTutor.getOrDefault(tutor.getId(), TotalesSesiones.vacio()), inicio, fin))
                .toList();
    }

    private Map<Long, TotalesSesiones> obtenerTotalesPorTutor(List<Long> tutorIds, LocalDate inicio, LocalDate fin) {
        Map<Long, TotalesSesiones> totalesPorTutor = new HashMap<>();
        for (Object[] fila : sesionRepository.countSesionesPorTutorYEstado(tutorIds, inicio, fin)) {
            Long tutorId = ((Number) fila[0]).longValue();
            EstadoSesion estado = (EstadoSesion) fila[1];
            long cantidad = ((Number) fila[2]).longValue();
            totalesPorTutor.merge(tutorId, TotalesSesiones.desde(estado, cantidad), TotalesSesiones::sumar);
        }
        return totalesPorTutor;
    }

    private ReporteDesempenoTutorResponse construirFila(Tutor tutor, TotalesSesiones totales, LocalDate inicio, LocalDate fin) {
        double porcentajeCancelacion = totales.totalSesiones() == 0
                ? 0
                : (totales.sesionesCanceladas() * 100.0) / totales.totalSesiones();
        Double promedioCalificacion = calificacionSesionRepository.promedioCalificacionPorTutor(
                tutor.getId(),
                inicio,
                fin
        );

        return new ReporteDesempenoTutorResponse(
                tutor.getId(),
                tutor.getNombre(),
                totales.totalSesiones(),
                totales.sesionesCompletadas(),
                totales.sesionesCanceladas(),
                redondear(porcentajeCancelacion),
                redondear(promedioCalificacion)
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private Double redondear(Double valor) {
        return valor == null ? null : redondear(valor.doubleValue());
    }

    private record TotalesSesiones(long totalSesiones, long sesionesCompletadas, long sesionesCanceladas) {
        static TotalesSesiones vacio() {
            return new TotalesSesiones(0, 0, 0);
        }

        static TotalesSesiones desde(EstadoSesion estado, long cantidad) {
            return new TotalesSesiones(
                    cantidad,
                    estado == EstadoSesion.COMPLETADA ? cantidad : 0,
                    estado == EstadoSesion.CANCELADA ? cantidad : 0
            );
        }

        TotalesSesiones sumar(TotalesSesiones otro) {
            return new TotalesSesiones(
                    totalSesiones + otro.totalSesiones,
                    sesionesCompletadas + otro.sesionesCompletadas,
                    sesionesCanceladas + otro.sesionesCanceladas
            );
        }
    }
}
