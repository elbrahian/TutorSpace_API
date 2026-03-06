package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.ChatResponse;
import com.uco.tutorspace_api.domain.dto.EnviarMensajeRequest;
import com.uco.tutorspace_api.domain.dto.IniciarChatRequest;
import com.uco.tutorspace_api.domain.dto.MensajeResponse;
import com.uco.tutorspace_api.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatControler {
    private final ChatService chatService;

    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    // Iniciar chat — solo ESTUDIANTE
    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ChatResponse> iniciar(
            @Valid @RequestBody IniciarChatRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.iniciarChat(getUserId(auth), request));
    }

    // Listar chats del usuario autenticado
    @GetMapping
    public ResponseEntity<List<ChatResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(chatService.obtenerChatsPorUsuario(getUserId(auth)));
    }

    // Historial de mensajes paginado
    @GetMapping("/{chatId}/mensajes")
    public ResponseEntity<Page<MensajeResponse>> historial(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                chatService.obtenerHistorial(chatId, getUserId(auth), pageable)
        );
    }

    // Enviar mensaje vía REST (alternativa al WebSocket)
    @PostMapping("/{chatId}/mensajes")
    public ResponseEntity<MensajeResponse> enviar(
            @PathVariable Long chatId,
            @Valid @RequestBody EnviarMensajeRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.enviarMensaje(chatId, getUserId(auth), request));
    }
}
