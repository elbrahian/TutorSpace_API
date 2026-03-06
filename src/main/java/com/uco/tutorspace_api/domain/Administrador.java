package com.uco.tutorspace_api.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administradores")
@DiscriminatorValue("ADMINISTRADOR")
@Getter @Setter @NoArgsConstructor
public class Administrador extends Usuario{
}
