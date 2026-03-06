package com.uco.tutorspace_api.Utils;

import org.springframework.stereotype.Component;

@Component
public class EmailValidator {
    private static final String DOMINIO_INSTITUCIONAL = "@uco.net.co";

    public boolean esCorreoInstitucional(String email) {
        return email != null && email.toLowerCase().endsWith(DOMINIO_INSTITUCIONAL);
    }
}
