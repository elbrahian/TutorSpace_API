package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.HistorialSesion;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.repositories.HistorialSesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialSesionService {
    private final HistorialSesionRepository historialSesionRepository;

    // Llamado automáticamente desde SesionService al cambiar estado
    public void registrarCambio(Sesion sesion, EstadoSesion estadoAnterior,
                                EstadoSesion estadoNuevo) {
        HistorialSesion historial = new HistorialSesion();
        historial.setSesion(sesion);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setFechaCambio(LocalDateTime.now());

        historialSesionRepository.save(historial);
    }

    public List<HistorialSesion> obtenerHistorialDeSesion(Long sesionId) {
        return historialSesionRepository.findBySesionId(sesionId);
    }
}
