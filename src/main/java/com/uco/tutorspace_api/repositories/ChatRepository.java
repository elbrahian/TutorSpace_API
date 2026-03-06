package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> fingByTutorIdEstudianteId(Long tutorId, Long estudianteId);
    Boolean existsByTutorIdEstudianteId(Long tutorId, Long estudianteId);
}
