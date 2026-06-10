package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.dto.*;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.service.AuditoriaService;
import com.uco.tutorspace_api.service.MateriaService;
import com.uco.tutorspace_api.service.ReporteCalificacionTutorService;
import com.uco.tutorspace_api.service.ReporteDemandaService;
import com.uco.tutorspace_api.service.ReporteDesempenoTutorService;
import com.uco.tutorspace_api.service.ReporteUsoService;
import com.uco.tutorspace_api.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración", description = "Gestión de tutores, materias y configuración del sistema")
public class AdminController {
    private final TutorService tutorService;
    private final MateriaService materiaService;
    private final ReporteDesempenoTutorService reporteDesempenoTutorService;

    // Se inyecta el service de auditoria
    private final AuditoriaService auditoriaService;
    private final ReporteDemandaService reporteDemandaService;
    private final ReporteUsoService reporteUsoService;
    private final ReporteCalificacionTutorService reporteCalificacionTutorService;

    @GetMapping("/auditoria/sesiones")
    public ResponseEntity<Page<AuditoriaSesionResponse>> auditoria(

            // Filtro: estado nuevo de la sesión
            @RequestParam(required = false)
            EstadoSesion estadoNuevo,

            // Filtro: estado anterior de la sesión
            @RequestParam(required = false)
            EstadoSesion estadoAnterior,

            // Filtro: nombre o ID del tutor
            @RequestParam(required = false)
            String tutor,

            // Filtro: nombre o ID del estudiante
            @RequestParam(required = false)
            String estudiante,

            // Parámetros de paginación inyectados automáticamente por Spring
            Pageable pageable
    ) {
        // Delega la lógica de consulta al servicio de auditoría y retorna HTTP 200 con el resultado
        return ResponseEntity.ok(
                auditoriaService.obtenerAuditoria(
                        estadoNuevo,
                        estadoAnterior,
                        tutor,
                        estudiante,
                        pageable
                )
        );
    }

    @Operation(summary = "Registrar tutor", description = "Crea un nuevo tutor en el sistema")
    @PostMapping("/tutores")
    public ResponseEntity<TutorResponse> registrarTutor(
            @Valid @RequestBody CrearTutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorService.registrarTutor(request));
    }

    @Operation(summary = "Listar tutores", description = "Obtiene todos los tutores registrados")
    @GetMapping("/tutores")
    public ResponseEntity<List<TutorResponse>> listarTutores() {
        return ResponseEntity.ok(tutorService.listarTutores());
    }

    @Operation(summary = "Reporte de desempeño de tutores", description = "Obtiene métricas agregadas de sesiones por tutor")
    @GetMapping("/reportes/tutores")
    public ResponseEntity<List<ReporteDesempenoTutorResponse>> reporteDesempenoTutores(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteDesempenoTutorService.obtenerReporte(fechaInicio, fechaFin));
    }

    @Operation(summary = "Reporte de oferta y demanda",
            description = "Devuelve sesiones solicitadas vs tutores activos por materia, con Top 5 mayor y menor demanda")
    @GetMapping("/reportes/demanda")
    public ResponseEntity<ReporteDemandaResponse> reporteDemanda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteDemandaService.getReporte(fechaInicio, fechaFin));
    }

    @Operation(summary = "Reporte de uso por rol",
            description = "Métricas de uso de la plataforma (usuarios activos, sesiones y mensajes) diferenciadas por rol, con actividad semanal")
    @GetMapping("/reportes/uso")
    public ResponseEntity<ReporteUsoResponse> reporteUso(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteUsoService.obtenerReporte(fechaInicio, fechaFin));
    }

    @Operation(summary = "Reporte de calificación de tutores",
            description = "Ranking de tutores por calificación promedio, distribución de estrellas y comentarios de estudiantes")
    @GetMapping("/reportes/calificaciones")
    public ResponseEntity<List<ReporteCalificacionTutorResponse>> reporteCalificaciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteCalificacionTutorService.obtenerReporte(fechaInicio, fechaFin));
    }

    @Operation(summary = "Exportar reporte de demanda a CSV",
            description = "Descarga el reporte de oferta y demanda como archivo CSV")
    @GetMapping("/reportes/demanda/exportar")
    public void exportarDemandaCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {
        reporteDemandaService.generarCsv(fechaInicio, fechaFin, response);
    }

    @Operation(summary = "Desactivar tutor", description = "Desactiva un tutor por su ID")
    @PatchMapping("/tutores/{id}/desactivar")
    public ResponseEntity<TutorResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.desactivarTutor(id));
    }

    @Operation(summary = "Activar tutor", description = "Activa un tutor por su ID")
    @PatchMapping("/tutores/{id}/activar")
    public ResponseEntity<TutorResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.activarTutor(id));
    }

    @Operation(summary = "Crear materia", description = "Crea una nueva materia en el sistema")
    @PostMapping("/materias")
    public ResponseEntity<MateriaResponse> crearMateria(
            @Valid @RequestBody CrearMateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaService.crearMateria(request));
    }

    @Operation(summary = "Listar materias", description = "Obtiene todas las materias registradas")
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> listarMaterias() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }

    @Operation(summary = "Asignar materia a tutor", description = "Asigna una materia a un tutor específico")
    @PostMapping("/tutores/{id}/materias")
    public ResponseEntity<TutorResponse> asignarMateria(
            @PathVariable Long id,
            @Valid @RequestBody AsignarMateriaRequest request) {
        return ResponseEntity.ok(tutorService.asignarMateria(id, request));
    }

    @Operation(summary = "Retirar materia de tutor", description = "Retira una materia asignada a un tutor")
    @DeleteMapping("/tutores/{tutorId}/materias/{materiaId}")
    public ResponseEntity<TutorResponse> retirarMateria(
            @PathVariable Long tutorId,
            @PathVariable Long materiaId) {
        return ResponseEntity.ok(tutorService.retirarMateria(tutorId, materiaId));
    }

    public record ActualizarJornadaRequest(
            @NotBlank(message = "La jornada es requerida")
            @Pattern(regexp = "^(MANANA|TARDE|NOCHE)$",
                    message = "La jornada debe ser MANANA, TARDE o NOCHE")
            String jornadaGeneral
    ) {}

    @Operation(summary = "Actualizar jornada", description = "Actualiza la jornada laboral de un tutor")
    @PatchMapping("/tutores/{id}/jornada")
    public ResponseEntity<TutorResponse> actualizarJornada(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarJornadaRequest request) {
        return ResponseEntity.ok(tutorService.actualizarJornada(id, request.jornadaGeneral()));
    }
}

