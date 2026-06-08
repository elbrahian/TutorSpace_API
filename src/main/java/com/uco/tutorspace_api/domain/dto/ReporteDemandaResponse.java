package com.uco.tutorspace_api.domain.dto;

import java.util.List;

public record ReporteDemandaResponse(
        List<MateriaDemandaResponse> materias,
        List<MateriaDemandaResponse> top5MayorDemanda,
        List<MateriaDemandaResponse> top5MenorDemanda
) {}
