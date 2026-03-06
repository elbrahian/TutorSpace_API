package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByTutorIdAndEstudianteId(Long tutorId, Long estudianteId);
    Boolean existsByTutorIdAndEstudianteId(Long tutorId, Long estudianteId);
}
