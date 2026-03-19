package com.uco.tutorspace_api.service;

import com.uco.tutorspace_api.domain.*;
import com.uco.tutorspace_api.domain.dto.ChatResponse;
import com.uco.tutorspace_api.domain.dto.EnviarMensajeRequest;
import com.uco.tutorspace_api.domain.dto.IniciarChatRequest;
import com.uco.tutorspace_api.domain.dto.MensajeResponse;
import com.uco.tutorspace_api.domain.enums.TipoNotificacion;
import com.uco.tutorspace_api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRepository chatRepository;
    @Mock private MensajeRepository mensajeRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacionService notificacionService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    private Tutor tutorMock;
    private Estudiante estudianteMock;
    private Chat chatMock;
    private Mensaje mensajeMock;

    @BeforeEach
    void setUp() {
        tutorMock = new Tutor();
        tutorMock.setId(1L);
        tutorMock.setNombre("Tutor Test");

        estudianteMock = new Estudiante();
        estudianteMock.setId(2L);
        estudianteMock.setNombre("Estudiante Test");

        chatMock = new Chat();
        chatMock.setId(1L);
        chatMock.setTutor(tutorMock);
        chatMock.setEstudiante(estudianteMock);
        chatMock.setFechaCreacion(LocalDateTime.now());

        mensajeMock = new Mensaje();
        mensajeMock.setId(1L);
        mensajeMock.setChat(chatMock);
        mensajeMock.setEmisor(estudianteMock);
        mensajeMock.setContenido("Hola tutor");
        mensajeMock.setFecha(LocalDateTime.now());
    }

    @Test
    @DisplayName("iniciarChat debe crear nuevo chat")
    void iniciarChat_shouldCreateNewChat() {
        IniciarChatRequest request = new IniciarChatRequest(1L);
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(false);
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(chatRepository.save(any(Chat.class))).thenReturn(chatMock);

        ChatResponse response = chatService.iniciarChat(2L, request);

        assertNotNull(response);
        assertEquals(1L, response.tutorId());
        assertEquals(2L, response.estudianteId());
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    @DisplayName("iniciarChat con chat existente debe retornar chat existente")
    void iniciarChat_withExistingChat_shouldReturnExistingChat() {
        IniciarChatRequest request = new IniciarChatRequest(1L);
        when(chatRepository.existsByTutorIdAndEstudianteId(1L, 2L)).thenReturn(true);
        when(chatRepository.findByTutorIdAndEstudianteId(1L, 2L)).thenReturn(Optional.of(chatMock));

        ChatResponse response = chatService.iniciarChat(2L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    @DisplayName("enviarMensaje por tutor debe guardar y hacer broadcast")
    void enviarMensaje_byTutor_shouldSaveAndBroadcast() {
        EnviarMensajeRequest request = new EnviarMensajeRequest("Hola estudiante");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chatMock));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(tutorMock));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensajeMock);

        MensajeResponse response = chatService.enviarMensaje(1L, 1L, request);

        assertNotNull(response);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any(MensajeResponse.class));
        verify(mensajeRepository).save(any(Mensaje.class));
    }

    @Test
    @DisplayName("enviarMensaje por estudiante debe guardar y hacer broadcast")
    void enviarMensaje_byEstudiante_shouldSaveAndBroadcast() {
        EnviarMensajeRequest request = new EnviarMensajeRequest("Hola tutor");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chatMock));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudianteMock));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensajeMock);

        MensajeResponse response = chatService.enviarMensaje(1L, 2L, request);

        assertNotNull(response);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any(MensajeResponse.class));
    }

    @Test
    @DisplayName("obtenerHistorial debe retornar mensajes paginados")
    void obtenerHistorial_shouldReturnPaginatedMessages() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Mensaje> mensajesPage = new PageImpl<>(List.of(mensajeMock), pageable, 1);
        
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chatMock));
        when(mensajeRepository.findByChatIdOrderByFechaAsc(1L, pageable)).thenReturn(mensajesPage);

        Page<MensajeResponse> response = chatService.obtenerHistorial(1L, 2L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("enviarMensaje por usuario no autorizado debe lanzar excepción")
    void enviarMensaje_byUnauthorizedUser_shouldThrowException() {
        EnviarMensajeRequest request = new EnviarMensajeRequest("Hola");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chatMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            chatService.enviarMensaje(1L, 999L, request)
        );

        assertEquals("No tienes acceso a este chat", exception.getMessage());
    }

    @Test
    @DisplayName("obtenerHistorial por usuario no autorizado debe lanzar excepción")
    void obtenerHistorial_byUnauthorizedUser_shouldThrowException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chatMock));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            chatService.obtenerHistorial(1L, 999L, pageable)
        );

        assertEquals("No tienes acceso a este chat", exception.getMessage());
    }
}
