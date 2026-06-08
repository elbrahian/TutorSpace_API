package com.uco.tutorspace_api.service;
import com.uco.tutorspace_api.domain.dto.EstudianteDashboard;

public interface EstudianteEstadisticasService {
    EstudianteDashboard obtenerEstadisticas(Long estudianteId);
}

