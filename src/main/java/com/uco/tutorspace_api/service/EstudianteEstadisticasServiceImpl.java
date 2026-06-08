package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.dto.ActividadMensual;
import com.uco.tutorspace_api.domain.dto.EstudianteDashboard;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.repositories.SesionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EstudianteEstadisticasServiceImpl implements EstudianteEstadisticasService {

    private final SesionRepository sesionRepository;

    public EstudianteEstadisticasServiceImpl(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    public EstudianteDashboard obtenerEstadisticas(Long estudianteId) {
        EstudianteDashboard dto = new EstudianteDashboard();

        // RF-01: Total sesiones completadas
        long totalSesiones = sesionRepository.countSesionesCompletadasByEstudiante(estudianteId);
        dto.setTotalSesionesCompletadas(totalSesiones);

        // RF-02: horas = minutos acumulados / 60
        long totalMinutos = sesionRepository
                .findByEstudianteIdAndEstado(estudianteId, EstadoSesion.COMPLETADA)
                .stream()
                .mapToLong(s -> Duration.between(s.getHoraInicio(), s.getHoraFin()).toMinutes())
                .sum();
        dto.setTotalHorasTutoria(Math.round((totalMinutos / 60.0) * 10.0) / 10.0);

        // RF-03: Materia más consultada
        List<String> materias = sesionRepository.findMateriasMasConsultadas(
                estudianteId, PageRequest.of(0, 1));
        dto.setMateriaMasConsultada(materias.isEmpty() ? "N/A" : materias.get(0));

        // RF-04: Tutor frecuente
        List<String> tutores = sesionRepository.findTutoresFrecuentes(
                estudianteId, PageRequest.of(0, 1));
        dto.setTutorFrecuente(tutores.isEmpty() ? "N/A" : tutores.get(0));

        // RF-05: Actividad mensual
        int anioActual = LocalDate.now().getYear();
        List<Object[]> rawActividad = sesionRepository.findActividadMensual(estudianteId, anioActual);
        dto.setActividadMensual(mapearActividadMensual(rawActividad));

        return dto;
    }

    private List<ActividadMensual> mapearActividadMensual(List<Object[]> raw) {
        List<ActividadMensual> lista = new ArrayList<>();
        for (Object[] fila : raw) {
            int numMes = ((Number) fila[0]).intValue();
            long cantidad = ((Number) fila[1]).longValue();
            String nombreMes = Month.of(numMes)
                    .getDisplayName(TextStyle.FULL, new Locale("es", "CO"));
            lista.add(new ActividadMensual(numMes, nombreMes, cantidad));
        }
        return lista;
    }
}