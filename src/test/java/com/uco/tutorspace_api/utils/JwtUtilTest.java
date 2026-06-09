package com.uco.tutorspace_api.utils;

import com.uco.tutorspace_api.domain.Usuario;
import com.uco.tutorspace_api.domain.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "dHV0b3JzcGFjZV9qd3Rfc2VjcmV0X2tleV8yMDI0X3Vjbw==");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        
        usuarioMock = new Usuario() {};
        usuarioMock.setId(1L);
        usuarioMock.setEmail("test@uco.net.co");
        usuarioMock.setNombre("Test User");
        usuarioMock.setRol(RolUsuario.ESTUDIANTE);
    }

    @Test
    @DisplayName("generateToken debe contener claims correctos")
    void generateToken_shouldContainCorrectClaims() {
        String token = jwtUtil.generateToken(usuarioMock);

        assertNotNull(token);
        assertEquals("test@uco.net.co", jwtUtil.extractEmail(token));
        assertEquals("ESTUDIANTE", jwtUtil.extractRol(token));
        assertEquals(1L, jwtUtil.extractId(token));
        assertEquals("Test User", jwtUtil.extractNombre(token));
    }

    @Test
    @DisplayName("generateToken debe expirar después del tiempo configurado")
    void generateToken_shouldExpireAfterConfiguredTime() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        
        String token = jwtUtil.generateToken(usuarioMock);
        
        assertThrows(RuntimeException.class, () -> jwtUtil.isTokenValid(token));
    }

    @Test
    @DisplayName("extractEmail debe retornar email correcto")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken(usuarioMock);

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@uco.net.co", email);
    }

    @Test
    @DisplayName("extractRol debe retornar rol correcto")
    void extractRol_shouldReturnCorrectRol() {
        String token = jwtUtil.generateToken(usuarioMock);

        String rol = jwtUtil.extractRol(token);

        assertEquals("ESTUDIANTE", rol);
    }

    @Test
    @DisplayName("extractId debe retornar id correcto")
    void extractId_shouldReturnCorrectId() {
        String token = jwtUtil.generateToken(usuarioMock);

        Long id = jwtUtil.extractId(token);

        assertEquals(1L, id);
    }

    @Test
    @DisplayName("isTokenValid con token válido debe retornar true")
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(usuarioMock);

        boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid con token expirado debe lanzar excepción")
    void isTokenValid_withExpiredToken_shouldThrowException() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken(usuarioMock);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            jwtUtil.isTokenValid(token)
        );

        assertTrue(exception.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("isTokenValid con firma inválida debe lanzar excepción")
    void isTokenValid_withInvalidSignature_shouldThrowException() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            jwtUtil.isTokenValid("invalid.token.here")
        );

        assertTrue(exception.getMessage().contains("inválido"));
    }

    @Test
    @DisplayName("isTokenValid con token malformado debe lanzar excepción")
    void isTokenValid_withMalformedToken_shouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            jwtUtil.isTokenValid("malformed-token")
        );

        assertTrue(exception.getMessage().contains("inválido"));
    }
}
