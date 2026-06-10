package com.uco.tutorspace_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "solicitud_tutor_materia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"solicitud_tutor_id", "materia_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTutorMateria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_tutor_id", nullable = false)
    private SolicitudTutor solicitudTutor;

    @ManyToOne
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;
}
