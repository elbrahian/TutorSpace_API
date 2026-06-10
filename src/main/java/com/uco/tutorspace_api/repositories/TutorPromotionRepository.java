package com.uco.tutorspace_api.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TutorPromotionRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public void eliminarChatsUsuario(Long usuarioId) {
        entityManager.createNativeQuery("""
                DELETE FROM mensajes
                WHERE chat_id IN (
                    SELECT id
                    FROM chats
                    WHERE tutor_id = ?1 OR estudiante_id = ?1
                )
                """)
                .setParameter(1, usuarioId)
                .executeUpdate();

        entityManager.createNativeQuery("""
                DELETE FROM chats
                WHERE tutor_id = ?1 OR estudiante_id = ?1
                """)
                .setParameter(1, usuarioId)
                .executeUpdate();
    }

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

    public void asignarMateriasSolicitadas(Long usuarioId, Long solicitudId) {
        entityManager.createNativeQuery("""
                INSERT INTO tutor_materia (tutor_id, materia_id)
                SELECT ?1, solicitud_materia.materia_id
                FROM solicitud_tutor_materia solicitud_materia
                WHERE solicitud_materia.solicitud_tutor_id = ?2
                AND NOT EXISTS (
                    SELECT 1
                    FROM tutor_materia tutor_materia
                    WHERE tutor_materia.tutor_id = ?1
                    AND tutor_materia.materia_id = solicitud_materia.materia_id
                )
                """)
                .setParameter(1, usuarioId)
                .setParameter(2, solicitudId)
                .executeUpdate();
    }
}
