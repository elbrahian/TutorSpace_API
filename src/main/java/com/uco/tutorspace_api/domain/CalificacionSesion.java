package com.uco.tutorspace_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "calificacion_sesion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_calificacion_sesion_estudiante",
                columnNames = {"sesion_id", "estudiante_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CalificacionSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private Sesion sesion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private int calificacion;

    @Column(length = 500)
    private String comentario;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
