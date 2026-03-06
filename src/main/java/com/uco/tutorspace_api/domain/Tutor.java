package com.uco.tutorspace_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutores")
@DiscriminatorValue("TUTOR")
@Getter
@Setter
@NoArgsConstructor
public class Tutor extends Usuario{

    @Column(name = "jornada_general", length = 50)
    private String jornadaGeneral;

    @ManyToMany
    @JoinTable(
            name = "tutor_materia",
            joinColumns = @JoinColumn(name = "tutor_id"),
            inverseJoinColumns = @JoinColumn(name = "materia_id")
    )
    private List<Materia> materias = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<Disponibilidad> disponibilidades = new ArrayList<>();
}
