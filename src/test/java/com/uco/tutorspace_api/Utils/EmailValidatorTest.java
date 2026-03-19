package com.uco.tutorspace_api.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

    @InjectMocks
    private EmailValidator emailValidator;

    @Test
    @DisplayName("esCorreoInstitucional con email UCO debe retornar true")
    void esCorreoInstitucional_withUCOEmail_shouldReturnTrue() {
        assertTrue(emailValidator.esCorreoInstitucional("usuario@uco.net.co"));
        assertTrue(emailValidator.esCorreoInstitucional("test.user@uco.net.co"));
        assertTrue(emailValidator.esCorreoInstitucional("name123@uco.net.co"));
    }

    @Test
    @DisplayName("esCorreoInstitucional con email gmail debe retornar false")
    void esCorreoInstitucional_withGmailEmail_shouldReturnFalse() {
        assertFalse(emailValidator.esCorreoInstitucional("usuario@gmail.com"));
        assertFalse(emailValidator.esCorreoInstitucional("test@gmail.com"));
    }

    @Test
    @DisplayName("esCorreoInstitucional con email hotmail debe retornar false")
    void esCorreoInstitucional_withHotmailEmail_shouldReturnFalse() {
        assertFalse(emailValidator.esCorreoInstitucional("usuario@hotmail.com"));
        assertFalse(emailValidator.esCorreoInstitucional("test@outlook.com"));
    }

    @Test
    @DisplayName("esCorreoInstitucional con email UCO en mayúsculas debe retornar true")
    void esCorreoInstitucional_withUppercaseUCO_shouldReturnTrue() {
        assertTrue(emailValidator.esCorreoInstitucional("USUARIO@UCO.NET.CO"));
        assertTrue(emailValidator.esCorreoInstitucional("Test.User@UCO.NET.CO"));
    }

    @Test
    @DisplayName("esCorreoInstitucional con email null debe retornar false")
    void esCorreoInstitucional_withNullEmail_shouldReturnFalse() {
        assertFalse(emailValidator.esCorreoInstitucional(null));
    }

    @Test
    @DisplayName("esCorreoInstitucional con email vacío debe retornar false")
    void esCorreoInstitucional_withEmptyEmail_shouldReturnFalse() {
        assertFalse(emailValidator.esCorreoInstitucional(""));
        assertFalse(emailValidator.esCorreoInstitucional("   "));
    }

    @Test
    @DisplayName("esCorreoInstitucional con otros dominios debe retornar false")
    void esCorreoInstitucional_withOtherDomains_shouldReturnFalse() {
        assertFalse(emailValidator.esCorreoInstitucional("user@yahoo.com"));
        assertFalse(emailValidator.esCorreoInstitucional("user@udea.edu.co"));
        assertFalse(emailValidator.esCorreoInstitucional("user@uco.com"));
        assertFalse(emailValidator.esCorreoInstitucional("user@uco.net"));
        assertFalse(emailValidator.esCorreoInstitucional("user@uco.net.co.com"));
    }
}
