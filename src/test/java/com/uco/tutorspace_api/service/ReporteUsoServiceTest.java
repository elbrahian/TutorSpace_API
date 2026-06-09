package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Administrador;
import com.uco.tutorspace_api.domain.Chat;
import com.uco.tutorspace_api.domain.Estudiante;
import com.uco.tutorspace_api.domain.Mensaje;
import com.uco.tutorspace_api.domain.Sesion;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.ReporteUsoResponse;
import com.uco.tutorspace_api.domain.dto.UsoPorRolResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.ChatRepository;
import com.uco.tutorspace_api.repositories.EstudianteRepository;
import com.uco.tutorspace_api.repositories.MensajeRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas funcionales del cálculo de métricas del reporte de uso por rol (MNT-11).
 * Inserta sesiones y mensajes con fechas/roles conocidos y verifica los totales,
 * el desglose por rol, la actividad semanal y el filtrado por rango de fechas.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReporteUsoServiceTest {

    @Autowired private ReporteUsoService reporteUsoService;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SesionRepository sesionRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private ChatRepository chatRepository;

    // Semana A: lunes 2026-05-04 ; Semana B: lunes 2026-05-11
    private static final LocalDate SEMANA_A = LocalDate.of(2026, 5, 4);
    private static final LocalDate SEMANA_B = LocalDate.of(2026, 5, 11);

    @BeforeEach
    void setUp() {
        // Orden de borrado respetando llaves foráneas
        mensajeRepository.deleteAll();
        chatRepository.deleteAll();
        sesionRepository.deleteAll();
        usuarioRepository.deleteAll();

        Tutor tutor = new Tutor();
        tutor.setNombre("Tutor Uso");
        tutor.setEmail("tutor.uso@uco.net.co");
        tutor.setPassword("x");
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setEstado(EstadoUsuario.ACTIVO);
        tutor = tutorRepository.save(tutor);

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Estudiante Uso");
        estudiante.setEmail("estudiante.uso@uco.net.co");
        estudiante.setPassword("x");
        estudiante.setRol(RolUsuario.ESTUDIANTE);
        estudiante.setEstado(EstadoUsuario.ACTIVO);
        estudiante = estudianteRepository.save(estudiante);

        Administrador admin = new Administrador();
        admin.setNombre("Admin Uso");
        admin.setEmail("admin.uso@uco.net.co");
        admin.setPassword("x");
        admin.setRol(RolUsuario.ADMIN);
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin = usuarioRepository.save(admin);

        // 2 sesiones: una por semana
        sesionRepository.save(nuevaSesion(tutor, estudiante, SEMANA_A));
        sesionRepository.save(nuevaSesion(tutor, estudiante, SEMANA_B));

        Chat chat = new Chat();
        chat.setTutor(tutor);
        chat.setEstudiante(estudiante);
        chat = chatRepository.save(chat);

        // Mensajes: estudiante x2 (semana A), tutor x1 (semana A), admin x1 (semana B)
        mensajeRepository.save(nuevoMensaje(chat, estudiante, SEMANA_A.atTime(10, 0)));
        mensajeRepository.save(nuevoMensaje(chat, estudiante, SEMANA_A.plusDays(1).atTime(10, 0)));
        mensajeRepository.save(nuevoMensaje(chat, tutor, SEMANA_A.atTime(11, 0)));
        mensajeRepository.save(nuevoMensaje(chat, admin, SEMANA_B.plusDays(1).atTime(9, 0)));
    }

    private Sesion nuevaSesion(Tutor tutor, Estudiante estudiante, LocalDate fecha) {
        Sesion s = new Sesion();
        s.setTutor(tutor);
        s.setEstudiante(estudiante);
        s.setFecha(fecha);
        s.setHoraInicio(LocalTime.of(8, 0));
        s.setHoraFin(LocalTime.of(9, 0));
        s.setEstado(EstadoSesion.APROBADA);
        return s;
    }

    private Mensaje nuevoMensaje(Chat chat, com.uco.tutorspace_api.domain.Usuario emisor, java.time.LocalDateTime fecha) {
        Mensaje m = new Mensaje();
        m.setChat(chat);
        m.setEmisor(emisor);
        m.setContenido("hola");
        m.setFecha(fecha);
        return m;
    }

    private UsoPorRolResponse metrica(ReporteUsoResponse r, String rol) {
        return r.metricasPorRol().stream()
                .filter(m -> m.rol().equals(rol))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Sin fechas calcula todos los registros y el desglose por rol")
    void obtenerReporte_sinFechas_calculaTodo() {
        ReporteUsoResponse r = reporteUsoService.obtenerReporte(null, null);

        // Usuarios activos
        assertEquals(1, r.estudiantesActivos());
        assertEquals(1, r.tutoresActivos());
        assertEquals(1, r.adminsActivos()); // 1 administrador ACTIVO registrado
        assertEquals(1, metrica(r, "ADMIN").usuariosActivos());

        // Totales
        assertEquals(2, r.totalSesionesCreadas());
        assertEquals(4, r.totalMensajesEnviados()); // 2 estudiante + 1 tutor + 1 admin

        // Desglose de mensajes por rol
        assertEquals(2, metrica(r, "ESTUDIANTE").mensajesEnviados());
        assertEquals(1, metrica(r, "TUTOR").mensajesEnviados());
        assertEquals(1, metrica(r, "ADMIN").mensajesEnviados());

        // Dos semanas con actividad
        assertEquals(2, r.actividadSemanal().size());
    }

    @Test
    @DisplayName("El rango de fechas filtra la actividad fuera de él")
    void obtenerReporte_conRango_filtra() {
        // Solo la semana A (04 al 07 de mayo): 1 sesión, 3 mensajes (2 estudiante + 1 tutor)
        ReporteUsoResponse r = reporteUsoService.obtenerReporte(
                LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 7));

        assertEquals(1, r.totalSesionesCreadas());
        assertEquals(3, r.totalMensajesEnviados());
        assertEquals(2, metrica(r, "ESTUDIANTE").mensajesEnviados());
        assertEquals(1, metrica(r, "TUTOR").mensajesEnviados());
        // Los mensajes del admin SÍ dependen del rango: su mensaje fue en la semana B
        assertEquals(0, metrica(r, "ADMIN").mensajesEnviados());
        // Los administradores activos son los registrados, no dependen del rango
        assertEquals(1, r.adminsActivos());
        assertEquals(1, metrica(r, "ADMIN").usuariosActivos());
        assertEquals(1, r.actividadSemanal().size());
    }

    @Test
    @DisplayName("Un rango sin datos devuelve métricas en cero, no error")
    void obtenerReporte_rangoVacio_devuelveCeros() {
        ReporteUsoResponse r = reporteUsoService.obtenerReporte(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));

        assertEquals(0, r.totalSesionesCreadas());
        assertEquals(0, r.totalMensajesEnviados());
        assertEquals(3, r.metricasPorRol().size()); // siempre los 3 roles
        // Aun sin actividad en el rango, el admin registrado se reporta como activo
        assertEquals(1, metrica(r, "ADMIN").usuariosActivos());
        assertTrue(r.actividadSemanal().isEmpty());
    }
}
