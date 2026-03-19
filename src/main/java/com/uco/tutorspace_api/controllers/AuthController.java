package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.domain.dto.AuthResponse;
import com.uco.tutorspace_api.domain.dto.LoginRequest;
import com.uco.tutorspace_api.domain.dto.RegisterRequest;
import com.uco.tutorspace_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Registrar estudiante",
            description = "Registra un nuevo estudiante con correo @uco.net.co")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudiante registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Email inválido o ya registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarEstudiante(request));
    }

    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

}
