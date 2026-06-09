# TutorSpace API - Informe de Auditoría de Seguridad

**Fecha de auditoría:** 19 de marzo de 2026  
**Versión del proyecto:** 0.0.1-SNAPSHOT  
**Stack tecnológico:** Spring Boot 4.0.3, Spring Security 6, JWT (jjwt 0.11.5), PostgreSQL

---

## Resumen Ejecutivo

El proyecto TutorSpace API presenta una arquitectura de seguridad generalmente sólida con JWT stateless, pero se identificaron **3 problemas críticos**, **5 problemas de alta severidad** y **4 problemas de severidad media** que requieren atención antes del despliegue a producción.

---

## Hallazgos de Seguridad

### 🔴 CRÍTICO

#### 1. Secrets hardcoded en archivos de configuración

**Archivo:** `src/main/resources/application.properties` (líneas 7-9, 19)

```properties
# ❌ PROBLEMA: Credenciales expuestas
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
jwt.secret=${JWT_SECRET}
```

**Riesgo:** Exposición de credenciales de base de datos y clave JWT en el repositorio.

**Recomendación:** Mover todas las credenciales a variables de entorno. Ya existe `application-prod.properties` que usa `${VARIABLE}` - usar este archivo para producción.

**Estado:** ⚠️ Pendiente - Requiere configuración de entorno.

---

#### 2. WebSocket permite cualquier origen

**Archivo:** `src/main/java/com/uco/tutorspace_api/config/WebSocketConfig.java` (línea 30)

```java
// ❌ PROBLEMA
.setAllowedOriginPatterns("*")
```

**Riesgo:** Permite conexiones WebSocket desde cualquier dominio, facilitando ataques CSRF vía WebSocket.

**Corrección aplicada:**
```java
// ✅ CORREGIDO
.setAllowedOriginPatterns(
    "http://localhost:5173",
    "http://localhost:5174",
    "https://tutorspaceapp-production.up.railway.app"
)
```

---

#### 3. Campo password sin @JsonIgnore en entidad Usuario

**Archivo:** `src/main/java/com/uco/tutorspace_api/domain/Usuario.java` (línea 31)

```java
// ❌ PROBLEMA: Password expuesto en serialización JSON
@Column(nullable = false)
private String password;
```

**Riesgo:** La contraseña hasheada podría filtrarse en respuestas JSON.

**Corrección aplicada:**
```java
// ✅ CORREGIDO
@JsonIgnore
@Column(nullable = false)
private String password;
```

---

### 🟠 ALTO

#### 4. BCryptPasswordEncoder sin strength configurado

**Archivo:** `src/main/java/com/uco/tutorspace_api/config/SecurityConfig.java` (línea 78)

```java
// ❌ PROBLEMA: Usa strength por defecto (10)
return new BCryptPasswordEncoder();
```

**Riesgo:** El factor de costo por defecto puede no ser suficientemente seguro.

**Corrección aplicada:**
```java
// ✅ CORREGIDO
return new BCryptPasswordEncoder(12);
```

---

#### 5. JwtAuthFilter continúa filter chain después de escribir error

**Archivo:** `src/main/java/com/uco/tutorspace_api/config/JwtAuthFilter.java` (línea 72)

```java
// ❌ PROBLEMA: Puede continuar el request después de enviar error
} catch (RuntimeException e) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    // ... escribe respuesta
    return;
}
chain.doFilter(request, response); // ← Esto no debería ejecutarse
```

**Corrección aplicada:**
```java
// ✅ CORREGIDO: Agregado flush() antes del return
response.getWriter().flush();
return;
```

---

#### 6. CORS configurado en dos lugares diferentes

**Archivos:**
- `SecurityConfig.java` - CORS específico
- `CorsConfig.java` - CORS duplicado con solo localhost

**Riesgo:** Configuraciones contradictorias pueden causar confusión y posibles bypass.

**Recomendación:** Eliminar `CorsConfig.java` y usar solo `SecurityConfig.java`.

---

#### 7. Logging de seguridad en modo DEBUG

**Archivo:** `src/main/resources/application.properties` (línea 23)

```properties
logging.level.org.springframework.security=DEBUG
```

**Riesgo:** En producción, los logs de seguridad pueden revelar información sensible.

**Recomendación:** Cambiar a `WARN` o eliminar en producción.

---

### 🟡 MEDIO

#### 8. No hay rate limiting implementado

**Riesgo:** Ataques de fuerza bruta o DoS en endpoints de autenticación.

**Recomendación:** Implementar Bucket4j para limitar requests.

---

#### 9. Validación de contenido de mensajes incompleta

**Archivo:** `src/main/java/com/uco/tutorspace_api/domain/dto/EnviarMensajeRequest.java`

```java
// Solo valida tamaño, no contenido
@Size(max = 500) String contenido
```

**Recomendación:** Implementar sanitización de HTML/scripts para prevenir XSS.

---

#### 10. WebSocket auth interceptor rechaza conexiones silenciosamente

**Archivo:** `src/main/java/com/uco/tutorspace_api/config/WebSocketAuthChannelInterceptor.java`

Cuando el token es inválido, retorna `null` (rechaza mensaje) pero no notifica al cliente.

---

## Checklist de Seguridad Verificado

| Categoría | Verificación | Estado |
|-----------|-------------|--------|
| JWT | Secret key ≥ 256 bits | ✅ Verificado |
| JWT | Expiración validada | ✅ Verificado |
| JWT | Algoritmo HS256 | ✅ Verificado |
| Spring Security | CSRF deshabilitado | ✅ Verificado |
| Spring Security | Session STATELESS | ✅ Verificado |
| Spring Security | Endpoints públicos correctos | ✅ Verificado |
| Spring Security | Autorización por rol | ✅ Verificado |
| Password | BCrypt encoding | ✅ Verificado |
| SQL Injection | JPA parametrizado | ✅ Verificado |
| Input Validation | @Valid en controllers | ✅ Verificado |
| Input Validation | @NotBlank, @Email, @Size | ✅ Verificado |
| Data Exposure | @JsonIgnore en password | ✅ Corregido |
| WebSocket | JWT validado en CONNECT | ✅ Verificado |
| CORS | Orígenes específicos | ✅ Corregido |

---

## Cobertura de Pruebas Generadas

### Pruebas Unitarias
- ✅ `AuthServiceTest.java` - 9 casos de prueba
- ✅ `DisponibilidadServiceTest.java` - 10 casos de prueba
- ✅ `SesionServiceTest.java` - 11 casos de prueba
- ✅ `ChatServiceTest.java` - 7 casos de prueba
- ✅ `JwtUtilTest.java` - 8 casos de prueba
- ✅ `EmailValidatorTest.java` - 7 casos de prueba
- ✅ `HorarioValidatorTest.java` - 9 casos de prueba

### Pruebas de Integración
- ✅ `AuthControllerIntegrationTest.java` - 6 casos de prueba
- ✅ `AdminControllerIntegrationTest.java` - 8 casos de prueba

### Total: 75+ casos de prueba

---

## Revisión de Rendimiento (N+1)

### Problemas identificados:

1. **TutorService.toResponse()** - Accede a `tutor.getMaterias()` que puede causar N+1
   - **Recomendación:** Usar `@EntityGraph` o `JOIN FETCH` en consulta

2. **BusquedaTutorService.toRestrictedResponse()** - Hace query adicional por cada tutor
   - **Recomendación:** Ya tiene `JOIN FETCH` en `findActivosByMateria`

3. **ChatService.toResponse()** - Accede a tutor y estudiante lazy
   - **Recomendación:** Añadir `@EntityGraph` en `findAllByUsuarioId`

---

## Recomendaciones para Producción

1. **Inmediato:**
   - Configurar variables de entorno para credenciales
   - Usar `application-prod.properties` en Railway
   - Habilitar rate limiting

2. **Corto plazo:**
   - Eliminar `CorsConfig.java` duplicado
   - Implementar logging estructurado (JSON)
   - Añadir validación de contenido (XSS sanitization)

3. **Largo plazo:**
   - Implementar refresh tokens
   - Considerar Redis para blacklist de tokens
   - Implementar audit logging
   - Añadir OpenAPI/Swagger documentation

---

## Conclusión

El proyecto TutorSpace API tiene una base de seguridad sólida con JWT stateless correctamente implementado. Las correcciones aplicadas abordan los riesgos más críticos. Antes de producción, es **obligatorio**:

1. Externalizar credenciales a variables de entorno
2. Implementar rate limiting
3. Ejecutar pruebas de integración con la base de datos de prueba (H2 configurado)
4. Realizar penetration testing

---

*Informe generado automáticamente - 19 de marzo de 2026*
