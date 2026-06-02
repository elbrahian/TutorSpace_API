package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.EstadoSolicitudTutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${tutorspace.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${tutorspace.mail.from:no-reply@tutorspace.local}")
    private String from;

    public void enviarEstadoSolicitudTutor(Usuario solicitante,
                                           EstadoSolicitudTutor estado,
                                           String observaciones) {
        if (!mailEnabled) {
            log.info("Correo omitido para {}. Estado solicitud tutor: {}", solicitante.getEmail(), estado);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Correo no enviado: JavaMailSender no está configurado");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(solicitante.getEmail());
        message.setSubject("Estado de tu solicitud para ser tutor");
        message.setText(construirMensaje(solicitante, estado, observaciones));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("No se pudo enviar correo de solicitud tutor a {}: {}",
                    solicitante.getEmail(),
                    exception.getMessage());
        }
    }

    private String construirMensaje(Usuario solicitante,
                                    EstadoSolicitudTutor estado,
                                    String observaciones) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Hola ").append(solicitante.getNombre()).append(",\n\n");
        mensaje.append("Tu solicitud para convertirte en tutor está en estado: ")
                .append(estado)
                .append(".\n");

        if (observaciones != null && !observaciones.isBlank()) {
            mensaje.append("\nObservaciones del administrador:\n")
                    .append(observaciones)
                    .append("\n");
        }

        mensaje.append("\nTutorSpace");
        return mensaje.toString();
    }
}
