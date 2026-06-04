package com.uco.tutorspace_api.repositories;

import com.uco.tutorspace_api.domain.HistorialSesion;
import com.uco.tutorspace_api.domain.enums.EstadoSesion;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class HistorialSesionSpecification {

    public static Specification<HistorialSesion> estadoNuevo(
            EstadoSesion estadoNuevo
    ) {

        return (root, query, cb) -> {

            if (estadoNuevo == null) {
                return null;
            }

            return cb.equal(
                    root.get("estadoNuevo"),
                    estadoNuevo
            );
        };
    }

    public static Specification<HistorialSesion> estadoAnterior(
            EstadoSesion estadoAnterior
    ) {

        return (root, query, cb) -> {

            if (estadoAnterior == null) {
                return null;
            }

            return cb.equal(
                    root.get("estadoAnterior"),
                    estadoAnterior
            );
        };
    }

    public static Specification<HistorialSesion> tutor(
            String tutor
    ) {

        return (root, query, cb) -> {

            if (tutor == null || tutor.isBlank()) {
                return null;
            }

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

    public static Specification<HistorialSesion> estudiante(
            String estudiante
    ) {

        return (root, query, cb) -> {

            if (estudiante == null || estudiante.isBlank()) {
                return null;
            }

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

    public static Specification<HistorialSesion> fechaEntre(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        return (root, query, cb) -> {

            if (inicio == null && fin == null) {
                return null;
            }

            if (inicio != null && fin != null) {
                return cb.between(
                        root.get("fechaCambio"),
                        inicio,
                        fin
                );
            }

            if (inicio != null) {
                return cb.greaterThanOrEqualTo(
                        root.get("fechaCambio"),
                        inicio
                );
            }

            return cb.lessThanOrEqualTo(
                    root.get("fechaCambio"),
                    fin
            );
        };
    }

}
