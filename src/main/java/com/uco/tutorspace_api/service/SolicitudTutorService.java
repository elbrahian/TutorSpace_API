package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.SolicitudTutor;
import com.uco.tutorspace_api.domain.SolicitudTutorMateria;
import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.dto.CrearSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.domain.dto.RevisarSolicitudTutorRequest;
import com.uco.tutorspace_api.domain.dto.SolicitudTutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import com.uco.tutorspace_api.repositories.SolicitudTutorRepository;
import com.uco.tutorspace_api.repositories.TutorPromotionRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SolicitudTutorService {
    private final SolicitudTutorRepository solicitudTutorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final TutorPromotionRepository tutorPromotionRepository;
    private final EmailService emailService;

    @Transactional
    public SolicitudTutorResponse crearSolicitud(Long solicitanteId,
                                                 CrearSolicitudTutorRequest request) {
        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarSolicitante(solicitante);
        validarSinSolicitudPendiente(solicitanteId);

        List<Materia> materias = obtenerMaterias(request.materiaIds());

        SolicitudTutor solicitud = new SolicitudTutor();
        solicitud.setSolicitante(solicitante);
        solicitud.setJustificacion(request.justificacion().trim());
        solicitud.setEstado(EstadoSolicitudTutor.PENDIENTE);
        solicitud.setFechaEnvio(LocalDateTime.now());
        materias.forEach(solicitud::agregarMateria);

        SolicitudTutor guardada = solicitudTutorRepository.save(solicitud);
        emailService.enviarEstadoSolicitudTutor(
                solicitante,
                EstadoSolicitudTutor.PENDIENTE,
                "Tu solicitud fue recibida y está pendiente de revisión."
        );

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<SolicitudTutorResponse> listarSolicitudes(EstadoSolicitudTutor estado,
                                                          LocalDate inicio,
                                                          LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha fin");
        }

        LocalDateTime fechaInicio = inicio == null ? null : inicio.atStartOfDay();
        LocalDateTime fechaFin = fin == null ? null : fin.atTime(LocalTime.MAX);

        return solicitudTutorRepository.findAll(
                        crearFiltro(estado, fechaInicio, fechaFin),
                        Sort.by(Sort.Direction.DESC, "fechaEnvio")
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitudTutorResponse> obtenerMiSolicitud(Long solicitanteId) {
        return solicitudTutorRepository
                .findFirstBySolicitanteIdOrderByFechaEnvioDesc(solicitanteId)
                .map(this::toResponse);
    }

    @Transactional
    public SolicitudTutorResponse revisarSolicitud(Long solicitudId,
                                                   RevisarSolicitudTutorRequest request) {
        SolicitudTutor solicitud = solicitudTutorRepository.findByIdWithDetalle(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud de tutor no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitudTutor.PENDIENTE) {
            throw new RuntimeException("Solo se pueden revisar solicitudes pendientes");
        }

        if (request.nuevoEstado() == EstadoSolicitudTutor.PENDIENTE) {
            throw new RuntimeException("La revisión debe aprobar o rechazar la solicitud");
        }

        solicitud.setEstado(request.nuevoEstado());
        solicitud.setObservaciones(normalizarObservaciones(request.observaciones()));
        solicitud.setFechaRevision(LocalDateTime.now());

        if (request.nuevoEstado() == EstadoSolicitudTutor.APROBADA) {
            Long solicitanteId = solicitud.getSolicitante().getId();
            tutorPromotionRepository.eliminarChatsUsuario(solicitanteId);
            tutorPromotionRepository.promoverUsuarioATutor(solicitanteId);
            tutorPromotionRepository.asignarMateriasSolicitadas(solicitanteId, solicitud.getId());
        }

        SolicitudTutor actualizada = solicitudTutorRepository.save(solicitud);
        emailService.enviarEstadoSolicitudTutor(
                actualizada.getSolicitante(),
                actualizada.getEstado(),
                actualizada.getObservaciones()
        );

        return toResponse(actualizada);
    }

    private void validarSolicitante(Usuario solicitante) {
        if (solicitante.getRol() != RolUsuario.ESTUDIANTE) {
            throw new RuntimeException("Solo estudiantes pueden solicitar convertirse en tutor");
        }

        if (solicitante.getEstado() != EstadoUsuario.ACTIVO) {
            throw new RuntimeException("La cuenta debe estar activa para enviar la solicitud");
        }
    }

    private void validarSinSolicitudPendiente(Long solicitanteId) {
        boolean existePendiente = solicitudTutorRepository.existsBySolicitanteIdAndEstado(
                solicitanteId,
                EstadoSolicitudTutor.PENDIENTE
        );

        if (existePendiente) {
            throw new RuntimeException("Ya existe una solicitud pendiente para este usuario");
        }
    }

    private List<Materia> obtenerMaterias(List<Long> materiaIds) {
        Set<Long> materiaIdsUnicos = new LinkedHashSet<>(materiaIds);
        List<Materia> materias = materiaRepository.findAllById(materiaIdsUnicos);

        if (materias.size() != materiaIdsUnicos.size()) {
            throw new RuntimeException("Una o más materias no fueron encontradas");
        }

        return materias;
    }

    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        return observaciones.trim();
    }

    private Specification<SolicitudTutor> crearFiltro(EstadoSolicitudTutor estado,
                                                      LocalDateTime fechaInicio,
                                                      LocalDateTime fechaFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (fechaInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaEnvio"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaEnvio"), fechaFin));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public SolicitudTutorResponse toResponse(SolicitudTutor solicitud) {
        List<MateriaResponse> materias = solicitud.getMateriasSolicitadas()
                .stream()
                .map(SolicitudTutorMateria::getMateria)
                .map(materia -> new MateriaResponse(
                        materia.getId(),
                        materia.getNombre(),
                        materia.getCodigo()
                ))
                .toList();

        Usuario solicitante = solicitud.getSolicitante();
        return new SolicitudTutorResponse(
                solicitud.getId(),
                solicitante.getId(),
                solicitante.getNombre(),
                solicitante.getEmail(),
                solicitud.getJustificacion(),
                solicitud.getObservaciones(),
                solicitud.getEstado(),
                solicitud.getFechaEnvio(),
                solicitud.getFechaRevision(),
                materias
        );
    }
}
