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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/estudiante")
    public ResponseEntity<AuthResponse> registerEstudiante(@Valid @RequestBody RegisterEstudianteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerEstudiante(request));
    }

    @PostMapping("/register/tutor")
    public ResponseEntity<AuthResponse> registerTutor(@Valid @RequestBody RegisterTutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTutor(request));
    }
}
