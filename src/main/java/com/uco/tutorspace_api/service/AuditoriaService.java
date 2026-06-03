package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.dto.AuditoriaSesionResponse;
import com.uco.tutorspace_api.repositories.HistorialSesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final HistorialSesionRepository historialRepository;

    public Page<AuditoriaSesionResponse> obtenerAuditoria(
            Pageable pageable
    ) {

        return historialRepository.findAll(pageable)
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
