package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.HistorialSesion;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Clase que define especificaciones JPA para filtrar registros
 * de HistorialSesion de forma dinamica y componible.
 */
public class HistorialSesionSpecification {

    /**
     * Filtra registros por el estado nuevo al que cambio la sesion.
     */
    public static Specification<HistorialSesion> estadoNuevo(
            EstadoSesion estadoNuevo
    ) {
        return (root, query, cb) -> {

            // Si no se proporciona valor, se omite este filtro
            if (estadoNuevo == null) {
                return null;
            }

            // Predicado: WHERE estado_nuevo = :estadoNuevo
            return cb.equal(
                    root.get("estadoNuevo"),
                    estadoNuevo
            );
        };
    }

    /**
     * Filtra registros por el estado anterior que tenía la sesión antes del cambio.
     */
    public static Specification<HistorialSesion> estadoAnterior(
            EstadoSesion estadoAnterior
    ) {
        return (root, query, cb) -> {

            // Si no se proporciona valor, se omite este filtro
            if (estadoAnterior == null) {
                return null;
            }

            // Predicado: WHERE estado_anterior = :estadoAnterior
            return cb.equal(
                    root.get("estadoAnterior"),
                    estadoAnterior
            );
        };
    }

    /**
     * Filtra registros por el nombre del tutor asociado a la sesión.
     */
    public static Specification<HistorialSesion> tutor(
            String tutor
    ) {
        return (root, query, cb) -> {

            // Si no se proporciona valor o está vacío, se omite este filtro
            if (tutor == null || tutor.isBlank()) {
                return null;
            }

            // Predicado: WHERE LOWER(sesion.tutor.nombre) LIKE '%tutor%'
            return cb.like(
                    cb.lower(
                            root.get("sesion")
                                    .get("tutor")
                                    .get("nombre")
                    ),
                    "%" + tutor.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filtra registros por el nombre del estudiante asociado a la sesión.
     */
    public static Specification<HistorialSesion> estudiante(
            String estudiante
    ) {
        return (root, query, cb) -> {

            // Si no se proporciona valor o está vacío, se omite este filtro
            if (estudiante == null || estudiante.isBlank()) {
                return null;
            }

            // Predicado: WHERE LOWER(sesion.estudiante.nombre) LIKE '%estudiante%'
            return cb.like(
                    cb.lower(
                            root.get("sesion")
                                    .get("estudiante")
                                    .get("nombre")
                    ),
                    "%" + estudiante.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filtra registros cuya fecha de cambio se encuentre dentro de un rango.
     */
    public static Specification<HistorialSesion> fechaEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return (root, query, cb) -> {

            // Si no se proporcionan fechas, se omite este filtro
            if (inicio == null && fin == null) {
                return null;
            }

            // Rango completo: WHERE fecha_cambio BETWEEN :inicio AND :fin
            if (inicio != null && fin != null) {
                return cb.between(
                        root.get("fechaCambio"),
                        inicio,
                        fin
                );
            }

            // Solo límite inferior: WHERE fecha_cambio >= :inicio
            if (inicio != null) {
                return cb.greaterThanOrEqualTo(
                        root.get("fechaCambio"),
                        inicio
                );
            }

            // Solo límite superior: WHERE fecha_cambio <= :fin
            return cb.lessThanOrEqualTo(
                    root.get("fechaCambio"),
                    fin
            );
        };
    }

}