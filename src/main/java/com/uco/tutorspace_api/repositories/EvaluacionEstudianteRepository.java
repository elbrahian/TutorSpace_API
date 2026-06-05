package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.EvaluacionEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionEstudianteRepository extends JpaRepository<EvaluacionEstudiante, Long> {

    // RN-02: Verificar si ya existe evaluación para una sesión
    boolean existsBySesionId(Long sesionId);

    // RN-03: Solo el tutor participante puede haber evaluado
    Optional<EvaluacionEstudiante> findBySesionIdAndTutorId(Long sesionId, Long tutorId);

    // RF-06: Administrador puede consultar evaluaciones de un estudiante
    List<EvaluacionEstudiante> findByEstudianteId(Long estudianteId);

    // Consultar evaluaciones de un tutor
    List<EvaluacionEstudiante> findByTutorId(Long tutorId);

    // Consultar evaluaciones de una sesión
    Optional<EvaluacionEstudiante> findBySesionId(Long sesionId);

    // Contar evaluaciones de un estudiante (para reportes)
    @Query("SELECT COUNT(e) FROM EvaluacionEstudiante e WHERE e.estudiante.id = :estudianteId")
    long countEvaluacionesByEstudiante(@Param("estudianteId") Long estudianteId);

    // Obtener promedio de evaluaciones de un estudiante
    @Query("SELECT AVG(e.puntuacion) FROM EvaluacionEstudiante e WHERE e.estudiante.id = :estudianteId")
    Double obtenerPromedioPuntuacionesEstudiante(@Param("estudianteId") Long estudianteId);
}
