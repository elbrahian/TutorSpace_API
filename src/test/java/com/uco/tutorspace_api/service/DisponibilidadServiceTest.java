package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.Utils.HorarioValidator;
import com.uco.tutorspace_api.domain.Disponibilidad;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.CrearDisponibilidadRequest;
import com.uco.tutorspace_api.domain.dto.DisponibilidadResponse;
import com.uco.tutorspace_api.domain.enums.EstadoDisponibilidad;
import com.uco.tutorspace_api.repositories.DisponibilidadRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private HorarioValidator horarioValidator;

    @InjectMocks
    private DisponibilidadService disponibilidadService;

    private Tutor tutorMock;
    private Disponibilidad disponibilidadMock;
    private CrearDisponibilidadRequest requestValido;

    @BeforeEach
    void setUp() {
        tutorMock = new Tutor();
        tutorMock.setId(1L);
        tutorMock.setNombre("Tutor Test");

        disponibilidadMock = new Disponibilidad();
        disponibilidadMock.setId(1L);
        disponibilidadMock.setTutor(tutorMock);
        disponibilidadMock.setDia("LUNES");
        disponibilidadMock.setHoraInicio(LocalTime.of(8, 0));
        disponibilidadMock.setHoraFin(LocalTime.of(10, 0));
        disponibilidadMock.setEstado(EstadoDisponibilidad.DISPONIBLE);

        requestValido = new CrearDisponibilidadRequest("LUNES", LocalTime.of(8, 0), LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("crearDisponibilidad con datos válidos debe guardar")
    void crearDisponibilidad_withValidData_shouldSave() {
        when(horarioValidator.esRangoValido(any(), any())).thenReturn(true);
        when(disponibilidadRepository.findSolapadas(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(tutorRepository.findById(any())).thenReturn(Optional.of(tutorMock));
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenReturn(disponibilidadMock);

        DisponibilidadResponse response = disponibilidadService.crearDisponibilidad(1L, requestValido);

        assertNotNull(response);
        assertEquals("LUNES", response.dia());
        verify(disponibilidadRepository).save(any(Disponibilidad.class));
    }

    @Test
    @DisplayName("crearDisponibilidad debe establecer estado DISPONIBLE")
    void crearDisponibilidad_shouldSetEstadoDisponible() {
        when(horarioValidator.esRangoValido(any(), any())).thenReturn(true);
        when(disponibilidadRepository.findSolapadas(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(tutorRepository.findById(any())).thenReturn(Optional.of(tutorMock));
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(invocation -> {
            Disponibilidad d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        disponibilidadService.crearDisponibilidad(1L, requestValido);

        verify(disponibilidadRepository).save(argThat(d -> d.getEstado() == EstadoDisponibilidad.DISPONIBLE));
    }

    @Test
    @DisplayName("listarPorTutor debe retornar solo disponibilidades del tutor")
    void listarPorTutor_shouldReturnOnlyTutorDisponibilidades() {
        when(disponibilidadRepository.findByTutorId(1L)).thenReturn(List.of(disponibilidadMock));

        List<DisponibilidadResponse> result = disponibilidadService.listarPorTutor(1L);

        assertEquals(1, result.size());
        assertEquals("LUNES", result.get(0).dia());
    }

    @Test
    @DisplayName("eliminarDisponibilidad con estado DISPONIBLE debe eliminar")
    void eliminarDisponibilidad_withDisponibleEstado_shouldDelete() {
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        disponibilidadService.eliminarDisponibilidad(1L, 1L);

        verify(disponibilidadRepository).delete(disponibilidadMock);
    }

    @Test
    @DisplayName("bloquearFranja debe establecer estado BLOQUEADA")
    void bloquearFranja_shouldSetEstadoBloqueada() {
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        disponibilidadService.bloquearFranja(1L);

        verify(disponibilidadRepository).save(argThat(d -> d.getEstado() == EstadoDisponibilidad.BLOQUEADA));
    }

    @Test
    @DisplayName("liberarFranja debe establecer estado DISPONIBLE")
    void liberarFranja_shouldSetEstadoDisponible() {
        disponibilidadMock.setEstado(EstadoDisponibilidad.BLOQUEADA);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        disponibilidadService.liberarFranja(1L);

        verify(disponibilidadRepository).save(argThat(d -> d.getEstado() == EstadoDisponibilidad.DISPONIBLE));
    }

    @Test
    @DisplayName("crearDisponibilidad con horario solapado debe lanzar excepción")
    void crearDisponibilidad_withOverlappingSchedule_shouldThrowException() {
        when(horarioValidator.esRangoValido(any(), any())).thenReturn(true);
        when(disponibilidadRepository.findSolapadas(any(), any(), any(), any())).thenReturn(List.of(disponibilidadMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            disponibilidadService.crearDisponibilidad(1L, requestValido)
        );

        assertEquals("La franja se solapa con una disponibilidad existente", exception.getMessage());
    }

    @Test
    @DisplayName("crearDisponibilidad con rango de tiempo inválido debe lanzar excepción")
    void crearDisponibilidad_withInvalidTimeRange_shouldThrowException() {
        when(horarioValidator.esRangoValido(any(), any())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            disponibilidadService.crearDisponibilidad(1L, requestValido)
        );

        assertEquals("La hora de inicio debe ser anterior a la hora de fin", exception.getMessage());
    }

    @Test
    @DisplayName("eliminarDisponibilidad con estado BLOQUEADA debe lanzar excepción")
    void eliminarDisponibilidad_withBloqueadaEstado_shouldThrowException() {
        disponibilidadMock.setEstado(EstadoDisponibilidad.BLOQUEADA);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            disponibilidadService.eliminarDisponibilidad(1L, 1L)
        );

        assertEquals("No se puede eliminar una franja bloqueada por una sesión", exception.getMessage());
    }

    @Test
    @DisplayName("eliminarDisponibilidad por tutor diferente debe lanzar excepción")
    void eliminarDisponibilidad_byDifferentTutor_shouldThrowException() {
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(disponibilidadMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            disponibilidadService.eliminarDisponibilidad(1L, 999L)
        );

        assertEquals("No tienes permiso para eliminar esta disponibilidad", exception.getMessage());
    }
}
