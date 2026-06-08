package com.uco.tutorspace_api.controllers;

import com.uco.tutorspace_api.config.CustomUserDetails;
import com.uco.tutorspace_api.domain.dto.EnviarMensajeRequest;
import com.uco.tutorspace_api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;

    @MessageMapping("/chat/{chatId}/mensaje")
    public void recibirMensaje(@DestinationVariable Long chatId,
                               @Payload EnviarMensajeRequest request,
                               Principal principal) {
        Long emisorId = ((CustomUserDetails) principal).getId();
        chatService.enviarMensaje(chatId, emisorId, request);
    }
}