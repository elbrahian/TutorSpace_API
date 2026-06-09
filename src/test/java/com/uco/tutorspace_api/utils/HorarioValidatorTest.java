package com.uco.tutorspace_api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HorarioValidatorTest {

    @InjectMocks
    private HorarioValidator horarioValidator;

    @Test
    @DisplayName("esRangoValido con rango válido debe retornar true")
    void esRangoValido_withValidRange_shouldReturnTrue() {
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(10, 0);

        assertTrue(horarioValidator.esRangoValido(horaInicio, horaFin));
    }

    @Test
    @DisplayName("esRangoValido con horas iguales debe retornar false")
    void esRangoValido_withEqualTimes_shouldReturnFalse() {
        LocalTime hora = LocalTime.of(8, 0);

        assertFalse(horarioValidator.esRangoValido(hora, hora));
    }

    @Test
    @DisplayName("esRangoValido con rango invertido debe retornar false")
    void esRangoValido_withInvertedRange_shouldReturnFalse() {
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(8, 0);

        assertFalse(horarioValidator.esRangoValido(horaInicio, horaFin));
    }

    @Test
    @DisplayName("esRangoValido con horaInicio null debe retornar false")
    void esRangoValido_withNullInicio_shouldReturnFalse() {
        assertFalse(horarioValidator.esRangoValido(null, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("esRangoValido con horaFin null debe retornar false")
    void esRangoValido_withNullFin_shouldReturnFalse() {
        assertFalse(horarioValidator.esRangoValido(LocalTime.of(8, 0), null));
    }

    @Test
    @DisplayName("seSolapan con horarios que se solapan debe retornar true")
    void seSolapan_withOverlappingSchedules_shouldReturnTrue() {
        assertTrue(horarioValidator.seSolapan(
                LocalTime.of(8, 0), LocalTime.of(10, 0),
                LocalTime.of(9, 0), LocalTime.of(11, 0)
        ));
    }

    @Test
    @DisplayName("seSolapan con horarios adyacentes debe retornar false")
    void seSolapan_withAdjacentSchedules_shouldReturnFalse() {
        assertFalse(horarioValidator.seSolapan(
                LocalTime.of(8, 0), LocalTime.of(10, 0),
                LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    @Test
    @DisplayName("seSolapan con horarios que no se solapan debe retornar false")
    void seSolapan_withNonOverlappingSchedules_shouldReturnFalse() {
        assertFalse(horarioValidator.seSolapan(
                LocalTime.of(8, 0), LocalTime.of(10, 0),
                LocalTime.of(11, 0), LocalTime.of(13, 0)
        ));
    }

    @Test
    @DisplayName("seSolapan con uno conteniendo al otro debe retornar true")
    void seSolapan_withContainedSchedule_shouldReturnTrue() {
        assertTrue(horarioValidator.seSolapan(
                LocalTime.of(8, 0), LocalTime.of(14, 0),
                LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }
}
