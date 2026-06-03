package com.uco.tutorspace_api.domain.dto;

public record ReporteDesempenoTutorResponse(
        Long tutorId,
        String nombreTutor,
        Long totalSesiones,
        Long sesionesCompletadas,
        Long sesionesCanceladas,
        Double porcentajeCancelacion,
        Double promedioCalificacion
) {
}
