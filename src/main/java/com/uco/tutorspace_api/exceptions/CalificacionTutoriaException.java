package com.uco.tutorspace_api.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio del flujo de evaluación de tutorías (MNT-12).
 * Lleva su propio HttpStatus para que el GlobalExceptionHandler responda
 * con el código correcto (p. ej. 409 CONFLICT cuando la sesión ya fue evaluada).
 */
public class CalificacionTutoriaException extends RuntimeException {

    private final HttpStatus status;

    public CalificacionTutoriaException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
