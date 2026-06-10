package com.uco.tutorspace_api.domain;

import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_tutor")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 1000)
    private String justificacion;

    @Column(length = 1000)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitudTutor estado = EstadoSolicitudTutor.PENDIENTE;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @OneToMany(mappedBy = "solicitudTutor", cascade = CascadeType.ALL)
    private List<SolicitudTutorMateria> materiasSolicitadas = new ArrayList<>();

    public void agregarMateria(Materia materia) {
        SolicitudTutorMateria solicitudMateria = new SolicitudTutorMateria();
        solicitudMateria.setSolicitudTutor(this);
        solicitudMateria.setMateria(materia);
        materiasSolicitadas.add(solicitudMateria);
    }
}
