package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.CalificacionSesion;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.ComentarioCalificacionTutorResponse;
import com.uco.tutorspace_api.domain.dto.ReporteCalificacionTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.CalificacionSesionRepository;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.SesionRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas funcionales del reporte de calificación de tutores (MNT-10).
 * Inserta evaluaciones (MNT-12) con calificaciones, comentarios y fechas
 * conocidas y verifica el ranking por promedio, la distribución de estrellas,
 * la agrupación de comentarios y el filtrado por rango de fechas de sesión.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReporteCalificacionTutorServiceTest {

    @Autowired private ReporteCalificacionTutorService reporteCalificacionTutorService;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private CalificacionSesionRepository calificacionSesionRepository;

    private static final LocalDate FECHA = LocalDate.of(2026, 5, 4);
    private static final LocalDate FECHA_VIEJA = LocalDate.of(2020, 1, 15);

    private Estudiante estudiante;

    @BeforeEach
    void setUp() {
        calificacionSesionRepository.deleteAll();
        sesionRepository.deleteAll();
        usuarioRepository.deleteAll();

        estudiante = new Estudiante();
        estudiante.setNombre("Estudiante Calif");
        estudiante.setEmail("estudiante.calif@uco.net.co");
        estudiante.setPassword("x");
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        estudiante = estudianteRepository.save(estudiante);
    }

    private Tutor nuevoTutor(String nombre, String email) {
        Tutor tutor = new Tutor();
        tutor.setNombre(nombre);
        tutor.setEmail(email);
        tutor.setPassword("x");
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setEstado(EstadoUsuario.ACTIVO);
        return tutorRepository.save(tutor);
    }

    private void calificar(Tutor tutor, LocalDate fecha, int calificacion, String comentario) {
        Sesion sesion = new Sesion();
        sesion.setTutor(tutor);
        sesion.setEstudiante(estudiante);
        sesion.setFecha(fecha);
        sesion.setHoraInicio(LocalTime.of(8, 0));
        sesion.setHoraFin(LocalTime.of(9, 0));
        sesion.setEstado(EstadoSesion.COMPLETADA);
        sesion = sesionRepository.save(sesion);

        CalificacionSesion calif = new CalificacionSesion();
        calif.setSesion(sesion);
        calif.setEstudiante(estudiante);
        calif.setCalificacion(calificacion);
        calif.setComentario(comentario);
        calificacionSesionRepository.save(calif);
    }

    private ReporteCalificacionTutorResponse fila(List<ReporteCalificacionTutorResponse> reporte, String nombreTutor) {
        return reporte.stream()
                .filter(r -> r.nombreTutor().equals(nombreTutor))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("CP-MNT-10-01: ordena el ranking por promedio descendente")
    void obtenerReporte_ordenaPorPromedioDescendente() {
        Tutor a = nuevoTutor("Tutor A", "a@uco.net.co");
        Tutor b = nuevoTutor("Tutor B", "b@uco.net.co");
        Tutor c = nuevoTutor("Tutor C", "c@uco.net.co");

        // A promedio 5.0, B promedio 4.0, C promedio 3.0
        calificar(a, FECHA, 5, null);
        calificar(a, FECHA, 5, null);
        calificar(b, FECHA, 4, null);
        calificar(b, FECHA, 4, null);
        calificar(c, FECHA, 3, null);
        calificar(c, FECHA, 3, null);

        List<ReporteCalificacionTutorResponse> reporte = reporteCalificacionTutorService.obtenerReporte(null, null);

        assertEquals(3, reporte.size());
        assertEquals("Tutor A", reporte.get(0).nombreTutor());
        assertEquals("Tutor B", reporte.get(1).nombreTutor());
        assertEquals("Tutor C", reporte.get(2).nombreTutor());

        assertEquals(5.0, reporte.get(0).promedioCalificacion());
        assertEquals(2, reporte.get(0).totalEvaluaciones());
        assertEquals(2, reporte.get(0).estrellas5());
        assertEquals(2, reporte.get(1).estrellas4());
        assertEquals(2, reporte.get(2).estrellas3());
    }

    @Test
    @DisplayName("CP-MNT-10-01: a igual promedio desempata por total de evaluaciones descendente")
    void obtenerReporte_desempataPorTotalEvaluaciones() {
        Tutor pocas = nuevoTutor("Tutor Pocas", "pocas@uco.net.co");
        Tutor muchas = nuevoTutor("Tutor Muchas", "muchas@uco.net.co");

        // Mismo promedio (5.0) pero distinto total de evaluaciones
        calificar(pocas, FECHA, 5, null);
        calificar(muchas, FECHA, 5, null);
        calificar(muchas, FECHA, 5, null);

        List<ReporteCalificacionTutorResponse> reporte = reporteCalificacionTutorService.obtenerReporte(null, null);

        assertEquals("Tutor Muchas", reporte.get(0).nombreTutor());
        assertEquals("Tutor Pocas", reporte.get(1).nombreTutor());
    }

    @Test
    @DisplayName("CP-MNT-10-02: agrupa comentarios por tutor y omite los vacíos")
    void obtenerReporte_agrupaComentariosYOmiteVacios() {
        Tutor conComentarios = nuevoTutor("Tutor Con", "con@uco.net.co");
        Tutor sinComentarios = nuevoTutor("Tutor Sin", "sin@uco.net.co");

        calificar(conComentarios, FECHA, 5, "Excelente tutor");
        calificar(conComentarios, FECHA, 4, "   "); // en blanco: no debe contar
        calificar(conComentarios, FECHA, 4, null);   // sin comentario
        calificar(sinComentarios, FECHA, 3, null);

        List<ReporteCalificacionTutorResponse> reporte = reporteCalificacionTutorService.obtenerReporte(null, null);

        List<ComentarioCalificacionTutorResponse> comentariosCon = fila(reporte, "Tutor Con").comentarios();
        assertEquals(1, comentariosCon.size());
        assertEquals("Excelente tutor", comentariosCon.get(0).comentario());
        assertEquals(5, comentariosCon.get(0).calificacion());
        assertEquals("Estudiante Calif", comentariosCon.get(0).nombreEstudiante());

        assertTrue(fila(reporte, "Tutor Sin").comentarios().isEmpty());
    }

    @Test
    @DisplayName("El rango de fechas filtra evaluaciones fuera del período")
    void obtenerReporte_filtraPorRangoDeFechas() {
        Tutor tutor = nuevoTutor("Tutor Rango", "rango@uco.net.co");

        calificar(tutor, FECHA, 5, null);        // dentro del rango
        calificar(tutor, FECHA_VIEJA, 1, null);  // fuera del rango

        List<ReporteCalificacionTutorResponse> reporte = reporteCalificacionTutorService.obtenerReporte(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertEquals(1, reporte.size());
        ReporteCalificacionTutorResponse fila = reporte.get(0);
        assertEquals(1, fila.totalEvaluaciones());
        assertEquals(5.0, fila.promedioCalificacion());
        assertEquals(1, fila.estrellas5());
        assertEquals(0, fila.estrellas1());
    }

    @Test
    @DisplayName("Sin evaluaciones en el período devuelve una lista vacía, no error")
    void obtenerReporte_sinDatos_devuelveListaVacia() {
        nuevoTutor("Tutor Aislado", "aislado@uco.net.co");

        List<ReporteCalificacionTutorResponse> reporte = reporteCalificacionTutorService.obtenerReporte(
                LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));

        assertTrue(reporte.isEmpty());
    }
}
