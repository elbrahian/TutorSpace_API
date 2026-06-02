package com.uco.tutorspace_api.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TutorPromotionRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public void promoverUsuarioATutor(Long usuarioId) {
        entityManager.createNativeQuery("""
                INSERT INTO tutores (id, jornada_general)
                SELECT ?1, NULL
                WHERE NOT EXISTS (
                    SELECT 1 FROM tutores WHERE id = ?1
                )
                """)
                .setParameter(1, usuarioId)
                .executeUpdate();

        entityManager.createNativeQuery("""
                UPDATE usuarios
                SET rol = 'TUTOR', tipo = 'TUTOR'
                WHERE id = ?1
                """)
                .setParameter(1, usuarioId)
                .executeUpdate();
    }
}
