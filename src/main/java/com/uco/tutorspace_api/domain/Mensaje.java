package com.uco.tutorspace_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Getter @Setter @NoArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chat al que pertenece el mensaje
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    // null cuando el mensaje es del sistema (MNT-05)
    @ManyToOne
    @JoinColumn(name = "emisor_id", nullable = true)
    private Usuario emisor;

    @Column(nullable = false, length = 500)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // true = mensaje automático del sistema, no editable ni eliminable
    @Column(nullable = false)
    private boolean esSistema = false;
}