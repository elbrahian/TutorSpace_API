package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.EstadoUsuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Cantidad de usuarios de un rol en un estado dado (p. ej. administradores ACTIVOS). */
    long countByRolAndEstado(RolUsuario rol, EstadoUsuario estado);
}
