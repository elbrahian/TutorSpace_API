package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Materia;
import com.uco.tutorspace_api.domain.Tutor;
import com.uco.tutorspace_api.domain.dto.AsignarMateriaRequest;
import com.uco.tutorspace_api.domain.dto.CrearTutorRequest;
import com.uco.tutorspace_api.domain.dto.MateriaResponse;
import com.uco.tutorspace_api.domain.dto.TutorResponse;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import com.uco.tutorspace_api.repositories.MateriaRepository;
import com.uco.tutorspace_api.repositories.TutorRepository;
import com.uco.tutorspace_api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TutorService {
    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final MateriaRepository materiaRepository;
    private final PasswordEncoder passwordEncoder;

    public TutorResponse registrarTutor(CrearTutorRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        Tutor tutor = new Tutor();
        tutor.setNombre(request.nombre());
        tutor.setEmail(request.email());
        tutor.setPassword(passwordEncoder.encode(request.password()));
        tutor.setRol(RolUsuario.TUTOR);
        tutor.setEstado(EstadoUsuario.ACTIVO);
        tutor.setJornadaGeneral(request.jornadaGeneral());

        Tutor guardado = tutorRepository.save(tutor);
        return toResponse(guardado);
    }

    public TutorResponse asignarMateria(Long tutorId, AsignarMateriaRequest request) {
        Tutor tutor = getTutorOrThrow(tutorId);

        Materia materia = materiaRepository.findById(request.materiaId())
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));

        boolean yaAsignada = tutor.getMaterias().stream()
                .anyMatch(m -> m.getId().equals(request.materiaId()));

        if (yaAsignada) {
            throw new IllegalStateException("La materia ya está asignada a este tutor");
        }

        tutor.getMaterias().add(materia);
        return toResponse(tutorRepository.save(tutor));
    }

    public TutorResponse actualizarJornada(Long tutorId, String nuevaJornada) {
        Tutor tutor = getTutorOrThrow(tutorId);
        tutor.setJornadaGeneral(nuevaJornada);
        return toResponse(tutorRepository.save(tutor));
    }

    public TutorResponse retirarMateria(Long tutorId, Long materiaId) {
        Tutor tutor = getTutorOrThrow(tutorId);
        boolean asignada = tutor.getMaterias().stream()
                .anyMatch(m -> m.getId().equals(materiaId));

        if (!asignada) {
            throw new RuntimeException("Este tutor no tiene asignada esta materia");
        }

        tutor.getMaterias().removeIf(m -> m.getId().equals(materiaId));
        return toResponse(tutorRepository.save(tutor));
    }

    public TutorResponse desactivarTutor(Long tutorId) {
        Tutor tutor = getTutorOrThrow(tutorId);
        if (tutor.getEstado() == EstadoUsuario.INACTIVO) {
            throw new RuntimeException("El tutor ya se encuentra inactivo");
        }
        tutor.setEstado(EstadoUsuario.INACTIVO);
        return toResponse(tutorRepository.save(tutor));
    }

    public TutorResponse getTutorById(Long tutorId) {
        return toResponse(getTutorOrThrow(tutorId));
    }

    public TutorResponse activarTutor(Long tutorId) {
        Tutor tutor = getTutorOrThrow(tutorId);
        if (tutor.getEstado() == EstadoUsuario.ACTIVO) {
            throw new RuntimeException("El tutor ya se encuentra activo");
        }
        tutor.setEstado(EstadoUsuario.ACTIVO);
        return toResponse(tutorRepository.save(tutor));
    }

    public List<TutorResponse> listarTutores() {
        return tutorRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TutorResponse toResponse(Tutor tutor) {
        List<MateriaResponse> materias = tutor.getMaterias().stream()
                .map(m -> new MateriaResponse(m.getId(), m.getNombre(), m.getCodigo()))
                .toList();

        return new TutorResponse(
                tutor.getId(),
                tutor.getNombre(),
                tutor.getEmail(),
                tutor.getJornadaGeneral(),
                tutor.getEstado(),
                materias
        );
    }

    private Tutor getTutorOrThrow(Long id) {
        return tutorRepository.findByIdWithMaterias(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
    }
}