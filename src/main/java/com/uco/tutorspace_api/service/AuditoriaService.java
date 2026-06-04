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


// Se crea el service de auditoria donde se puede consultar todos los campos necesarios

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final HistorialSesionRepository historialRepository;

    public Page<AuditoriaSesionResponse> obtenerAuditoria(
            EstadoSesion estadoNuevo,
            EstadoSesion estadoAnterior,
            String tutor,
            String estudiante,
            Pageable pageable
    ) {

        Specification<HistorialSesion> spec =
                Specification.where(
                                HistorialSesionSpecification.estadoNuevo(estadoNuevo)
                        )
                        .and(
                                HistorialSesionSpecification.estadoAnterior(estadoAnterior)
                        )
                        .and(
                                HistorialSesionSpecification.tutor(tutor)
                        )
                        .and(
                                HistorialSesionSpecification.estudiante(estudiante)
                        );

        return historialRepository.findAll(spec, pageable)
                .map(h -> new AuditoriaSesionResponse(
                        h.getSesion().getId(),
                        h.getSesion().getTutor().getNombre(),
                        h.getSesion().getEstudiante().getNombre(),
                        h.getEstadoAnterior(),
                        h.getEstadoNuevo(),
                        h.getFechaCambio()
                ));
    }
}
