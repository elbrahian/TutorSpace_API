package com.uco.tutorspace_api.auth.controller;

import com.uco.tutorspace_api.auth.dto.AuthResponse;
import com.uco.tutorspace_api.auth.dto.LoginRequest;
import com.uco.tutorspace_api.auth.dto.RegisterEstudianteRequest;
import com.uco.tutorspace_api.auth.dto.RegisterTutorRequest;
import com.uco.tutorspace_api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para los endpoints publicos de autenticacion.
 *
 * <p>Todos los endpoints bajo {@code /auth/**} estan configurados como
 * {@code permitAll()} en {@link com.uco.tutorspace_api.auth.config.SecurityConfig},
 * por lo que no requieren token JWT para ser invocados.
 *
 * <p>Endpoints disponibles:
 * <ul>
 *   <li>{@code POST /auth/login}               — iniciar sesion</li>
 *   <li>{@code POST /auth/register/estudiante} — registro de estudiante</li>
 *   <li>{@code POST /auth/register/tutor}      — registro de tutor</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica un usuario existente y retorna un token JWT.
     *
     * @param request body JSON con {@code email} y {@code password}
     * @return HTTP 200 con {@link AuthResponse} (token, email, nombre, rol)
     *         HTTP 401 si las credenciales son invalidas
     *         HTTP 403 si la cuenta esta inactiva
     *         HTTP 400 si el body no pasa las validaciones (@NotBlank, @Email)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Registra un nuevo estudiante y retorna un token JWT activo de inmediato.
     *
     * @param request body JSON con {@code nombre}, {@code email} y {@code password}
     * @return HTTP 201 Created con {@link AuthResponse}
     *         HTTP 409 si el email ya esta registrado
     *         HTTP 400 si el body no pasa las validaciones (@NotBlank, @Email, @Size)
     */
    @PostMapping("/register/estudiante")
    public ResponseEntity<AuthResponse> registerEstudiante(@Valid @RequestBody RegisterEstudianteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerEstudiante(request));
    }

    /**
     * Registra un nuevo tutor y retorna un token JWT activo de inmediato.
     *
     * @param request body JSON con {@code nombre}, {@code email}, {@code password}
     *                y opcionalmente {@code jornadaGeneral}
     * @return HTTP 201 Created con {@link AuthResponse}
     *         HTTP 409 si el email ya esta registrado
     *         HTTP 400 si el body no pasa las validaciones
     */
    @PostMapping("/register/tutor")
    public ResponseEntity<AuthResponse> registerTutor(@Valid @RequestBody RegisterTutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTutor(request));
    }
}
