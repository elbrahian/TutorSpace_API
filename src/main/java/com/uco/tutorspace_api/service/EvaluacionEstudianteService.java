package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.EvaluacionEstudiante;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.EvaluacionEstudianteRequest;
import com.uco.tutorspace_api.domain.dto.EvaluacionEstudianteResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.repositories.EvaluacionEstudianteRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionEstudianteService {

    private final EvaluacionEstudianteRepository evaluacionRepository;
    private final SesionRepository sesionRepository;
    private final TutorRepository tutorRepository;

    /**
     * HU-M13: Crear evaluación de estudiante
     * RF-03: POST /sesiones/{id}/evaluacion-estudiante con rol TUTOR
     *
     * Validaciones:
     * - RN-01: Solo pueden evaluarse sesiones con estado COMPLETADA
     * - RN-02: Un tutor solo puede evaluar al estudiante una vez por sesión
     * - RN-03: Solo el tutor participante en la sesión puede realizar la evaluación
     * - RN-04: Las observaciones son opcionales y tienen máximo 500 caracteres
     * - RNF-03: Las evaluaciones son inmutables una vez registradas
     */
    @Transactional
    public EvaluacionEstudianteResponse evaluarEstudiante(
            Long sesionId,
            Long tutorId,
            EvaluacionEstudianteRequest request) {

        // Obtener la sesión
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        // RN-01: Validar que la sesión está COMPLETADA
        if (sesion.getEstado() != EstadoSesion.COMPLETADA) {
            throw new RuntimeException(
                    "Solo se pueden evaluar sesiones con estado COMPLETADA. " +
                            "Estado actual: " + sesion.getEstado()
            );
        }

        // RN-03: Verificar que el tutor es el participante en la sesión
        if (!sesion.getTutor().getId().equals(tutorId)) {
            throw new RuntimeException(
                    "Solo el tutor participante en la sesión puede evaluar al estudiante"
            );
        }

        // RN-02: Verificar que no existe evaluación previa para esta sesión
        if (evaluacionRepository.existsBySesionId(sesionId)) {
            throw new RuntimeException(
                    "El estudiante ya ha sido evaluado en esta sesión. " +
                            "Las evaluaciones no pueden ser modificadas"
            );
        }

        // Obtener el tutor
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        // Crear evaluación
        EvaluacionEstudiante evaluacion = new EvaluacionEstudiante();
        evaluacion.setSesion(sesion);
        evaluacion.setTutor(tutor);
        evaluacion.setEstudiante(sesion.getEstudiante());
        evaluacion.setPuntuacion(request.puntuacion());
        evaluacion.setObservaciones(request.observaciones());
        evaluacion.setInmutable(true); // RNF-03

        EvaluacionEstudiante guardada = evaluacionRepository.save(evaluacion);

        return toResponse(guardada);
    }

    /**
     * Obtener evaluación de una sesión (solo para lectura)
     */
    public EvaluacionEstudianteResponse obtenerEvaluacionPorSesion(Long sesionId) {
        return evaluacionRepository.findBySesionId(sesionId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("No existe evaluación para esta sesión"));
    }

    /**
     * Verificar si existe evaluación para una sesión
     */
    public boolean existeEvaluacion(Long sesionId) {
        return evaluacionRepository.existsBySesionId(sesionId);
    }

    /**
     * RF-06: Obtener evaluaciones de un estudiante (para reportes del Administrador)
     */
    public List<EvaluacionEstudianteResponse> obtenerEvaluacionesPorEstudiante(Long estudianteId) {
        return evaluacionRepository.findByEstudianteId(estudianteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtener evaluaciones realizadas por un tutor
     */
    public List<EvaluacionEstudianteResponse> obtenerEvaluacionesPorTutor(Long tutorId) {
        return evaluacionRepository.findByTutorId(tutorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtener promedio de puntuación de un estudiante
     */
    public Double obtenerPromedioPuntuacionesEstudiante(Long estudianteId) {
        return evaluacionRepository.obtenerPromedioPuntuacionesEstudiante(estudianteId);
    }

    /**
     * Contar evaluaciones de un estudiante
     */
    public long contarEvaluacionesEstudiante(Long estudianteId) {
        return evaluacionRepository.countEvaluacionesByEstudiante(estudianteId);
    }

    // ============ Métodos Privados ============

    private EvaluacionEstudianteResponse toResponse(EvaluacionEstudiante evaluacion) {
        return new EvaluacionEstudianteResponse(
                evaluacion.getId(),
                evaluacion.getSesion().getId(),
                evaluacion.getTutor().getId(),
                evaluacion.getTutor().getNombre(),
                evaluacion.getEstudiante().getId(),
                evaluacion.getEstudiante().getNombre(),
                evaluacion.getPuntuacion(),
                evaluacion.getObservaciones(),
                evaluacion.getCreatedAt()
        );
    }
}
