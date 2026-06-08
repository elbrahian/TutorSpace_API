package com.uco.tutorspace_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones_estudiante")
@Getter
@Setter
@NoArgsConstructor
public class EvaluacionEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false, unique = true)
    private Sesion sesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Min(value = 1, message = "La puntuación debe ser mínimo 1")
    @Max(value = 5, message = "La puntuación debe ser máximo 5")
    @Column(nullable = false)
    private Integer puntuacion;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Campos de auditoría (RNF-03: Las evaluaciones son inmutables)
    @Column(nullable = false)
    private boolean inmutable = true;
}
