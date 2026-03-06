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

    // HU-08 — solo el estudiante puede iniciar el chat
    public ChatResponse iniciarChat(Long estudianteId, IniciarChatRequest request) {
        // Verificar que no exista ya un chat entre estos dos
        if (chatRepository.existsByTutorIdAndEstudianteId(request.tutorId(), estudianteId)) {
            // Si ya existe, retornarlo
            Chat existente = chatRepository
                    .findByTutorIdAndEstudianteId(request.tutorId(), estudianteId)
                    .get();
            return toResponse(existente);
        }

        Tutor tutor = tutorRepository.findById(request.tutorId())
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Chat chat = new Chat();
        chat.setTutor(tutor);
        chat.setEstudiante(estudiante);
        chat.setFechaCreacion(LocalDateTime.now());

        Chat guardado = chatRepository.save(chat);

        // Notificar al tutor que tiene un nuevo chat
        notificacionService.enviarNotificacion(
                tutor,
                TipoNotificacion.NUEVO_MENSAJE,
                "El estudiante " + estudiante.getNombre() + " inició un chat contigo"
        );

        return toResponse(guardado);
    }

    // Enviar mensaje — usado tanto por REST como por WebSocket
    public MensajeResponse enviarMensaje(Long chatId, Long emisorId,
                                         EnviarMensajeRequest request) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        // Verificar que el emisor pertenece al chat
        boolean esTutor = chat.getTutor().getId().equals(emisorId);
        boolean esEstudiante = chat.getEstudiante().getId().equals(emisorId);

        if (!esTutor && !esEstudiante) {
            throw new RuntimeException("No tienes acceso a este chat");
        }

        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(request.contenido());
        mensaje.setFecha(LocalDateTime.now());

        Mensaje guardado = mensajeRepository.save(mensaje);
        MensajeResponse response = toMensajeResponse(guardado);

        // Push WebSocket al canal del chat
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);

        // Notificar al otro participante
        Usuario destinatario = esTutor ? chat.getEstudiante() : chat.getTutor();
        notificacionService.enviarNotificacion(
                destinatario,
                TipoNotificacion.NUEVO_MENSAJE,
                emisor.getNombre() + ": " + request.contenido()
        );

        return response;
    }

    public Page<MensajeResponse> obtenerHistorial(Long chatId, Long usuarioId,
                                                  Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        // Verificar acceso al chat
        boolean tieneAcceso = chat.getTutor().getId().equals(usuarioId)
                || chat.getEstudiante().getId().equals(usuarioId);

        if (!tieneAcceso) {
            throw new RuntimeException("No tienes acceso a este chat");
        }

        return mensajeRepository.findByChatIdOrderByFechaAsc(chatId, pageable)
                .map(this::toMensajeResponse);
    }

    public List<ChatResponse> obtenerChatsPorUsuario(Long usuarioId) {
        return chatRepository.findAll().stream()
                .filter(c -> c.getTutor().getId().equals(usuarioId)
                        || c.getEstudiante().getId().equals(usuarioId))
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
                m.getEmisor().getId(),
                m.getEmisor().getNombre(),
                m.getContenido(),
                m.getFecha()
        );
    }
}
