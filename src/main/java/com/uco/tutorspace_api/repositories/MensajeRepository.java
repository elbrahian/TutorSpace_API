package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    Page<Mensaje>  findByChatIdOrderByFechaAsc(Long chatId, Pageable pageable);

}
