package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.dto.ActividadSemanalResponse;
import com.uco.tutorspace_api.domain.dto.ReporteUsoResponse;
import com.uco.tutorspace_api.domain.dto.UsoPorRolResponse;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MensajeRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Genera el reporte de uso de la plataforma diferenciado por rol (MNT-11).
 *
 * Decisión de diseño (RF-05 / RNF-03): en lugar de introducir un interceptor
 * global de logging asíncrono — que tocaría el camino de ejecución de todos los
 * módulos y arrancaría sin histórico — la actividad se reconstruye de forma
 * solo-lectura a partir de las tablas de dominio existentes (sesiones y
 * mensajes). Así el reporte entrega datos reales desde el primer día sin
 * impactar el rendimiento del sistema principal.
 */
@Service
@RequiredArgsConstructor
public class ReporteUsoService {

    private final SesionRepository sesionRepository;
    private final MensajeRepository mensajeRepository;

    private static final DateTimeFormatter ETIQUETA_SEMANA = DateTimeFormatter.ofPattern("dd/MM");
    private static final int MAX_SEMANAS = 12;

    // Cotas amplias usadas cuando el cliente no envía fecha: así se listan
    // todos los registros y nunca se pasa null a las consultas (evita el error
    // de PostgreSQL "could not determine data type of parameter").
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1970, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    public ReporteUsoResponse obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate desde = fechaInicio == null ? FECHA_MINIMA : fechaInicio;
        LocalDate hasta = fechaFin == null ? FECHA_MAXIMA : fechaFin;

        List<Object[]> sesiones = sesionRepository.findActividadSesiones(desde, hasta);

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);
        List<Object[]> mensajes = mensajeRepository.findActividadMensajes(inicio, fin);

        Set<Long> estudiantesActivos = new HashSet<>();
        Set<Long> tutoresActivos = new HashSet<>();
        Set<Long> adminsActivos = new HashSet<>();

        long mensajesEstudiante = 0;
        long mensajesTutor = 0;
        long mensajesAdmin = 0;

        // [estudiante, tutor, admin] de acciones por semana (clave = lunes de la semana)
        Map<LocalDate, long[]> semanas = new TreeMap<>();

        for (Object[] fila : sesiones) {
            LocalDate fecha = (LocalDate) fila[0];
            Long estudianteId = (Long) fila[1];
            Long tutorId = (Long) fila[2];

            if (estudianteId != null) estudiantesActivos.add(estudianteId);
            if (tutorId != null) tutoresActivos.add(tutorId);

            long[] acciones = semanas.computeIfAbsent(inicioSemana(fecha), k -> new long[3]);
            acciones[0]++; // sesión solicitada por el estudiante
            acciones[1]++; // sesión atendida por el tutor
        }

        for (Object[] fila : mensajes) {
            LocalDateTime fecha = (LocalDateTime) fila[0];
            Long emisorId = (Long) fila[1];
            RolUsuario rol = (RolUsuario) fila[2];

            long[] acciones = semanas.computeIfAbsent(inicioSemana(fecha.toLocalDate()), k -> new long[3]);
            switch (rol) {
                case ESTUDIANTE -> {
                    estudiantesActivos.add(emisorId);
                    mensajesEstudiante++;
                    acciones[0]++;
                }
                case TUTOR -> {
                    tutoresActivos.add(emisorId);
                    mensajesTutor++;
                    acciones[1]++;
                }
                case ADMIN -> {
                    adminsActivos.add(emisorId);
                    mensajesAdmin++;
                    acciones[2]++;
                }
            }
        }

        long totalSesiones = sesiones.size();

        List<UsoPorRolResponse> metricasPorRol = List.of(
                new UsoPorRolResponse("ESTUDIANTE", estudiantesActivos.size(), totalSesiones, mensajesEstudiante),
                new UsoPorRolResponse("TUTOR", tutoresActivos.size(), totalSesiones, mensajesTutor),
                new UsoPorRolResponse("ADMIN", adminsActivos.size(), 0, mensajesAdmin)
        );

        List<ActividadSemanalResponse> actividadSemanal = new ArrayList<>();
        for (Map.Entry<LocalDate, long[]> entrada : semanas.entrySet()) {
            long[] acciones = entrada.getValue();
            actividadSemanal.add(new ActividadSemanalResponse(
                    entrada.getKey().format(ETIQUETA_SEMANA),
                    entrada.getKey(),
                    acciones[0],
                    acciones[1],
                    acciones[2]
            ));
        }
        if (actividadSemanal.size() > MAX_SEMANAS) {
            actividadSemanal = new ArrayList<>(
                    actividadSemanal.subList(actividadSemanal.size() - MAX_SEMANAS, actividadSemanal.size()));
        }

        return new ReporteUsoResponse(
                estudiantesActivos.size(),
                tutoresActivos.size(),
                adminsActivos.size(),
                totalSesiones,
                mensajesEstudiante + mensajesTutor + mensajesAdmin,
                metricasPorRol,
                actividadSemanal
        );
    }

    private LocalDate inicioSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
