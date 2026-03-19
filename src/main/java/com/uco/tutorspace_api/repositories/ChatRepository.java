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
        SELECT DISTINCT c FROM Chat c
        LEFT JOIN FETCH c.tutor t
        LEFT JOIN FETCH c.estudiante e
        WHERE t.id = :usuarioId OR e.id = :usuarioId
    """)
    List<Chat> findAllByUsuarioId(@Param("usuarioId") Long usuarioId);
}
