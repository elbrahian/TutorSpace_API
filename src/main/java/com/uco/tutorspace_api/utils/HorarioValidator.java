package com.uco.tutorspace_api.utils;

import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class HorarioValidator {
    public boolean esRangoValido(LocalTime horaInicio, LocalTime horaFin) {
        return horaInicio != null && horaFin != null && horaInicio.isBefore(horaFin);
    }

    public boolean seSolapan(LocalTime inicio1, LocalTime fin1,
                             LocalTime inicio2, LocalTime fin2) {
        return inicio1.isBefore(fin2) && fin1.isAfter(inicio2);
    }
}
