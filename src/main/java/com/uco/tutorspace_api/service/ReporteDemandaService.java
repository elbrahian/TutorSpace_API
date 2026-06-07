package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.dto.MateriaDemandaResponse;
import com.uco.tutorspace_api.domain.dto.ReporteDemandaResponse;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteDemandaService {

    private final SesionRepository sesionRepository;
    private final TutorRepository tutorRepository;

    public ReporteDemandaResponse getReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Long> sesionesPorMateria = sesionRepository
                .countSesionesByMateria(fechaInicio, fechaFin)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        Map<String, Long> tutoresPorMateria = tutorRepository
                .countTutoresActivosByMateria()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        Set<String> todasLasMaterias = new HashSet<>();
        todasLasMaterias.addAll(sesionesPorMateria.keySet());
        todasLasMaterias.addAll(tutoresPorMateria.keySet());

        List<MateriaDemandaResponse> materias = todasLasMaterias.stream()
                .map(nombre -> {
                    int sesiones = sesionesPorMateria.getOrDefault(nombre, 0L).intValue();
                    int tutores  = tutoresPorMateria.getOrDefault(nombre, 0L).intValue();
                    Double tasa  = tutores == 0 ? null : (double) sesiones / tutores;
                    return new MateriaDemandaResponse(nombre, sesiones, tutores, tasa);
                })
                .sorted(Comparator.comparing(MateriaDemandaResponse::materia))
                .collect(Collectors.toList());

        List<MateriaDemandaResponse> top5Mayor = materias.stream()
                .filter(m -> m.sesionesSolicitadas() > 0)
                .sorted(Comparator.comparingInt(MateriaDemandaResponse::sesionesSolicitadas).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<MateriaDemandaResponse> top5Menor = materias.stream()
                .sorted(Comparator.comparingInt(MateriaDemandaResponse::sesionesSolicitadas))
                .limit(5)
                .collect(Collectors.toList());

        return new ReporteDemandaResponse(materias, top5Mayor, top5Menor);
    }

    public void generarCsv(LocalDate fechaInicio, LocalDate fechaFin, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"demanda_tutorias.csv\"");

        ReporteDemandaResponse reporte = getReporte(fechaInicio, fechaFin);

        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF');
        writer.println("Materia,Sesiones Solicitadas,Tutores Disponibles,Tasa de Cobertura");

        for (MateriaDemandaResponse item : reporte.materias()) {
            String tasa = item.tasaCobertura() == null
                    ? "Sin tutores"
                    : String.format(Locale.US, "%.2f", item.tasaCobertura());
            writer.printf("\"%s\",%d,%d,%s%n",
                    item.materia().replace("\"", "\"\""),
                    item.sesionesSolicitadas(),
                    item.tutoresDisponibles(),
                    tasa);
        }
        writer.flush();
    }
}
