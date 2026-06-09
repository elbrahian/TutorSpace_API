package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.dto.ComentarioCalificacionTutorResponse;
import com.uco.tutorspace_api.domain.dto.ReporteCalificacionTutorResponse;
import com.uco.tutorspace_api.repositories.CalificacionSesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte de calificación de tutores (MNT-10).
 *
 * Es un service de solo lectura que reutiliza las evaluaciones registradas por
 * MNT-12 ({@code CalificacionSesion}); no crea ni modifica calificaciones. Las
 * métricas se agregan en la base de datos mediante consultas dedicadas del
 * repositorio (RNF-02: respuesta &lt; 3s) y solo se ensamblan en memoria.
 *
 * Igual que MNT-08 y MNT-09, el filtro por fechas se aplica sobre la fecha de la
 * sesión ({@code c.sesion.fecha}). Cuando el cliente no envía rango se usan cotas
 * amplias para no pasar null a las consultas.
 */
@Service
@RequiredArgsConstructor
public class ReporteCalificacionTutorService {

    private final CalificacionSesionRepository calificacionSesionRepository;

    // Cotas amplias usadas cuando el cliente no envía fecha: así se incluyen todas
    // las evaluaciones y nunca se pasa null a las consultas (mismo criterio que MNT-11).
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1970, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    public List<ReporteCalificacionTutorResponse> obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate inicio = fechaInicio == null ? FECHA_MINIMA : fechaInicio;
        LocalDate fin = fechaFin == null ? FECHA_MAXIMA : fechaFin;

        List<Object[]> ranking = calificacionSesionRepository.reporteCalificacionesPorTutor(inicio, fin);
        if (ranking.isEmpty()) {
            return Collections.emptyList();
        }

        // Comentarios agrupados por tutor; el repositorio ya filtra los vacíos y
        // ordena por fecha de sesión descendente.
        Map<Long, List<ComentarioCalificacionTutorResponse>> comentariosPorTutor = agruparComentarios(
                calificacionSesionRepository.comentariosCalificacionesPorTutor(inicio, fin));

        List<ReporteCalificacionTutorResponse> reporte = new ArrayList<>(ranking.size());
        for (Object[] fila : ranking) {
            Long tutorId = ((Number) fila[0]).longValue();
            String nombreTutor = (String) fila[1];
            double promedio = redondear(((Number) fila[2]).doubleValue());
            long total = ((Number) fila[3]).longValue();

            reporte.add(new ReporteCalificacionTutorResponse(
                    tutorId,
                    nombreTutor,
                    promedio,
                    total,
                    ((Number) fila[4]).longValue(),
                    ((Number) fila[5]).longValue(),
                    ((Number) fila[6]).longValue(),
                    ((Number) fila[7]).longValue(),
                    ((Number) fila[8]).longValue(),
                    comentariosPorTutor.getOrDefault(tutorId, Collections.emptyList())
            ));
        }

        // El orden lo define el ranking (promedio desc, total desc, nombre asc); no se reordena.
        return reporte;
    }

    private Map<Long, List<ComentarioCalificacionTutorResponse>> agruparComentarios(List<Object[]> filas) {
        Map<Long, List<ComentarioCalificacionTutorResponse>> agrupados = new java.util.HashMap<>();
        for (Object[] fila : filas) {
            Long tutorId = ((Number) fila[0]).longValue();
            ComentarioCalificacionTutorResponse comentario = new ComentarioCalificacionTutorResponse(
                    ((Number) fila[1]).longValue(),
                    tutorId,
                    (String) fila[2],
                    ((Number) fila[3]).intValue(),
                    (String) fila[4],
                    (LocalDate) fila[5]
            );
            agrupados.computeIfAbsent(tutorId, k -> new ArrayList<>()).add(comentario);
        }
        return agrupados;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
