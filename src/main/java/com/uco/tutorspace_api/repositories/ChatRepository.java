package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByTutorIdAndEstudianteId(Long tutorId, Long estudianteId);
    Boolean existsByTutorIdAndEstudianteId(Long tutorId, Long estudianteId);

    @Query("""
        SELECT c FROM Chat c
        WHERE c.tutor.id = :usuarioId
        OR c.estudiante.id = :usuarioId
    """)
    List<Chat> findAllByUsuarioId(@Param("usuarioId") Long usuarioId);
}
