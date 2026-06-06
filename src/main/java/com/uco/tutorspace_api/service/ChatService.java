package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.*;
import com.uco.tutorspace_api.domain.dto.ChatResponse;
import com.uco.tutorspace_api.domain.dto.EnviarMensajeRequest;
import com.uco.tutorspace_api.domain.dto.IniciarChatRequest;
import com.uco.tutorspace_api.domain.dto.MensajeResponse;
import com.uco.tutorspace_api.domain.enums.TipoNotificacion;
import com.uco.tutorspace_api.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MensajeRepository mensajeRepository;
    private final TutorRepository tutorRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatResponse iniciarChat(Long estudianteId, IniciarChatRequest request) {
        if (chatRepository.existsByTutorIdAndEstudianteId(request.tutorId(), estudianteId)) {
            Chat existente = chatRepository
                    .findByTutorIdAndEstudianteId(request.tutorId(), estudianteId)
                    .get();
            return toResponse(existente);
        }

        Tutor tutor = tutorRepository.findById(request.tutorId())
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new NoSuchElementException("Estudiante no encontrado"));

        Chat chat = new Chat();
        chat.setTutor(tutor);
        chat.setEstudiante(estudiante);
        chat.setFechaCreacion(LocalDateTime.now());

        Chat guardado = chatRepository.save(chat);

        notificacionService.enviarNotificacion(
                tutor,
                TipoNotificacion.NUEVO_MENSAJE,
                "El estudiante " + estudiante.getNombre() + " inició un chat contigo"
        );

        return toResponse(guardado);
    }

    public MensajeResponse enviarMensaje(Long chatId, Long emisorId,
                                         EnviarMensajeRequest request) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NoSuchElementException("Chat no encontrado"));

        boolean esTutor = chat.getTutor().getId().equals(emisorId);
        boolean esEstudiante = chat.getEstudiante().getId().equals(emisorId);

        if (!esTutor && !esEstudiante) {
            throw new IllegalStateException("No tienes acceso a este chat");
        }

        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(sanitizar(request.contenido()));
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setEsSistema(false);

        Mensaje guardado = mensajeRepository.save(mensaje);
        MensajeResponse response = toMensajeResponse(guardado);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);

        Usuario destinatario = esTutor ? chat.getEstudiante() : chat.getTutor();
        notificacionService.enviarNotificacion(
                destinatario,
                TipoNotificacion.NUEVO_MENSAJE,
                emisor.getNombre() + ": " + request.contenido()
        );

        return response;
    }

    // MNT-05 — mensaje automático del sistema
    public MensajeResponse enviarMensajeSistema(Long tutorId, Long estudianteId,
                                                String contenido) {
        Chat chat;
        if (chatRepository.existsByTutorIdAndEstudianteId(tutorId, estudianteId)) {
            chat = chatRepository.findByTutorIdAndEstudianteId(tutorId, estudianteId)
                    .orElseThrow(() -> new NoSuchElementException("Chat no encontrado"));
        } else {
            Tutor tutor = tutorRepository.findById(tutorId)
                    .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
            Estudiante estudiante = estudianteRepository.findById(estudianteId)
                    .orElseThrow(() -> new NoSuchElementException("Estudiante no encontrado"));

            Chat nuevoChat = new Chat();
            nuevoChat.setTutor(tutor);
            nuevoChat.setEstudiante(estudiante);
            nuevoChat.setFechaCreacion(LocalDateTime.now());
            chat = chatRepository.save(nuevoChat);
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setEmisor(null);
        mensaje.setContenido(contenido);
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setEsSistema(true);

        Mensaje guardado = mensajeRepository.save(mensaje);

        MensajeResponse response = new MensajeResponse(
                guardado.getId(),
                null,
                "Sistema",
                guardado.getContenido(),
                guardado.getFecha(),
                true
        );

        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), response);

        return response;
    }

    public Page<MensajeResponse> obtenerHistorial(Long chatId, Long usuarioId,
                                                  Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NoSuchElementException("Chat no encontrado"));

        boolean tieneAcceso = chat.getTutor().getId().equals(usuarioId)
                || chat.getEstudiante().getId().equals(usuarioId);

        if (!tieneAcceso) {
            throw new IllegalStateException("No tienes acceso a este chat");
        }

        return mensajeRepository.findByChatIdOrderByFechaAsc(chatId, pageable)
                .map(this::toMensajeResponse);
    }

    public List<ChatResponse> obtenerChatsPorUsuario(Long usuarioId) {
        return chatRepository.findAllByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ChatResponse toResponse(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getTutor().getId(),
                chat.getTutor().getNombre(),
                chat.getEstudiante().getId(),
                chat.getEstudiante().getNombre(),
                chat.getFechaCreacion()
        );
    }

    public MensajeResponse toMensajeResponse(Mensaje m) {
        return new MensajeResponse(
                m.getId(),
                m.getEmisor() != null ? m.getEmisor().getId() : null,
                m.getEmisor() != null ? m.getEmisor().getNombre() : "Sistema",
                m.getContenido(),
                m.getFecha(),
                m.isEsSistema()
        );
    }

    private String sanitizar(String contenido) {
        if (contenido == null) return "";
        return contenido
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .trim();
    }
}