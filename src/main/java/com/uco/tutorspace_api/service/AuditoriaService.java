package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.HistorialSesion;
import com.uco.tutorspace_api.domain.dto.AuditoriaSesionResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.repositories.HistorialSesionRepository;
import com.uco.tutorspace_api.repositories.HistorialSesionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la lógica de negocio para la consulta de auditoría de sesiones.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    // Repositorio que provee acceso a los registros de historial de sesiones
    private final HistorialSesionRepository historialRepository;

    /**
     * Obtiene de forma paginada el historial de auditoría de sesiones,
     * aplicando filtros opcionales por estado, tutor y estudiante.
     */
    public Page<AuditoriaSesionResponse> obtenerAuditoria(
            EstadoSesion estadoNuevo,
            EstadoSesion estadoAnterior,
            String tutor,
            String estudiante,
            Pageable pageable
    ) {
        // Se compone la especificación combinando cada filtro con AND.
        // Los filtros con valor null son ignorados automáticamente por Spring Data.
        Specification<HistorialSesion> spec =
                Specification.where(
                                HistorialSesionSpecification.estadoNuevo(estadoNuevo)
                        )
                        .and(HistorialSesionSpecification.estadoAnterior(estadoAnterior))
                        .and(HistorialSesionSpecification.tutor(tutor))
                        .and(HistorialSesionSpecification.estudiante(estudiante));

        // Se ejecuta la consulta paginada y se proyecta cada entidad
        // HistorialSesion al DTO de respuesta AuditoriaSesionResponse
        return historialRepository.findAll(spec, pageable)
                .map(h -> new AuditoriaSesionResponse(
                        h.getSesion().getId(),               // ID de la sesión
                        h.getSesion().getTutor().getNombre(), // Nombre del tutor
                        h.getSesion().getEstudiante().getNombre(), // Nombre del estudiante
                        h.getEstadoAnterior(),               // Estado previo de la sesión
                        h.getEstadoNuevo(),                  // Estado nuevo de la sesión
                        h.getFechaCambio()                   // Fecha en que ocurrió el cambio
                ));
    }
}