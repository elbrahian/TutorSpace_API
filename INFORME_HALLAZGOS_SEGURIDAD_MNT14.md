# Informe Tecnico de Seguridad
## MNT-14 - Mejoras de Seguridad
### TutorSpace API - Sprint 1 Junio 2026

| Campo                     | Detalle                                                                  |
|---------------------------|--------------------------------------------------------------------------|
| Documento                 | INFORME-MNT-14                                                           |
| Proyecto                  | TutorSpace - Plataforma de Tutorias Academicas                           |
| Sprint                    | Sprint 1 - Junio 2026                                                    |
| Rama de trabajo           | fix/mnt-14-mejoras-seguridad                                             |
| Version del proyecto      | 0.0.1-SNAPSHOT                                                           |
| Herramienta principal     | OWASP ZAP 2.17.0                                                         |
| Entorno de prueba         | Local (localhost:8080, perfil dev / equivalente al perfil zap de GCS-03) |
| Stack                     | Spring Boot 4.0.3, Spring Security 7.0.3, JWT jjwt 0.11.5, H2 2.2.224    |
| Responsable               | Desarrollador MNT-14                                                     |
| Documento base de pruebas | GCS-04 - Casos de Prueba Sprint 1                                        |
| Convencion de commits     | fix(mnt-14): descripcion                                                 |
| Fecha                     | 6 de junio de 2026                                                       |

---

## 1. Resumen Ejecutivo

Se ejecutaron pruebas de seguridad combinadas sobre la API REST de TutorSpace cubriendo tres frentes: analisis estatico de codigo fuente, escaneo dinamico con OWASP ZAP 2.17.0 y pruebas manuales endpoint por endpoint con los tres roles del sistema (ADMIN, TUTOR, ESTUDIANTE). Se ejecutaron los 7 casos de prueba definidos en GCS-04 para MNT-14.

**Resultado por severidad:**

| Severidad       | Encontrados | Resueltos | Pendientes |
|-----------------|-------------|-----------|------------|
| Critica         | 0           | -         | 0          |
| Alta            | 4           | 0         | 4          |
| Media           | 7           | 0         | 7          |
| Baja            | 3           | 0         | 3          |
| Informativo ZAP | 2           | N/A       | N/A        |

**Estado actual frente al criterio de salida GCS-03:**
El criterio de salida establece 0 vulnerabilidades Alta o Critica abiertas al cierre del sprint. Los 4 hallazgos de severidad Alta estan documentados con plan de accion y deben corregirse antes del cierre. Las correcciones se implementan en esta misma rama (fix/mnt-14-mejoras-seguridad) y se ejecutara un scan de verificacion final con ZAP una vez aplicadas.

**Controles verificados como correctos:** autenticacion JWT, control de acceso por roles RBAC, proteccion contra SQL Injection via JPA parametrizado, rate limiting en autenticacion, y sanitizacion de contenido en mensajes de chat.

---

## 2. Condiciones del Analisis

### 2.1 Entorno de pruebas
- Aplicacion corriendo en `http://localhost:8080` con perfil `dev`
- El perfil `dev` es funcionalmente equivalente al perfil `zap` descrito en GCS-03: usa H2 file-based aislado de datos reales
- 7 usuarios de prueba precargados via `data.sql`: 1 ADMIN, 3 TUTOR, 3 ESTUDIANTE
- Datos de prueba incluyen payloads maliciosos en mensajes para verificar sanitizacion
- El perfil `dev` estaba forzado temporalmente en `application.properties` durante las pruebas (se revierte antes del merge segun H-14)

### 2.2 Metodologia
1. Analisis estatico de codigo fuente (DTOs, servicios, configuracion de seguridad)
2. Escaneo activo con OWASP ZAP 2.17.0 desde el Solicitante interno de ZAP
3. Pruebas manuales endpoint por endpoint con tokens JWT frescos por cada rol
4. Verificacion de resultados directamente en base de datos H2 via consola
5. Cruce de hallazgos entre las tres fuentes para eliminar falsos positivos
6. Documentacion de cada hallazgo alineada con los casos de prueba GCS-04

### 2.3 Cobertura de endpoints probados

| Rol        | Endpoints cubiertos                                                                                                                                                                                                                                                                           |
|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Publico    | `POST /auth/login`, `POST /auth/register`                                                                                                                                                                                                                                                     |
| ADMIN      | `GET/POST /admin/tutores`, `PATCH .../activar`, `PATCH .../desactivar`, `PATCH .../jornada`, `POST/DELETE .../materias`, `GET/POST /admin/materias`                                                                                                                                           |
| TUTOR      | `GET /tutor/perfil`, `GET/POST/DELETE /tutor/disponibilidad`, `POST /sesiones`, `GET /sesiones/tutor`, `PATCH /sesiones/{id}/estado`                                                                                                                                                          |
| ESTUDIANTE | `GET /estudiante/tutores/buscar`, `GET /estudiante/materias`, `GET/POST /chat`, `POST /chat/iniciar`, `GET/POST /chat/{id}/mensajes`, `GET /sesiones/estudiante`, `GET /sesiones/estudiante/rango`, `GET /notificaciones`, `GET /notificaciones/no-leidas`, `PATCH /notificaciones/{id}/leer` |

---

## 3. Resultados OWASP ZAP

### 3.1 Resumen de alertas automatizadas

| Severidad   | Cantidad | Accion requerida |
|-------------|----------|------------------|
| Alto        | 0        | Ninguna          |
| Medio       | 0        | Ninguna          |
| Bajo        | 0        | Ninguna          |
| Informativo | 2        | Ninguna          |

### 3.2 Alertas informativas (sin accion requerida)

**ZAP-01 - Peticion de Autenticacion Identificada**
- **Severidad:** Informativo / Confianza: Alta
- **Descripcion:** ZAP identifico `/auth/login` como punto de autenticacion del sistema.
- **Evaluacion:** Comportamiento esperado. El endpoint esta protegido con rate limiting activo de 20 req/min por IP mediante `RateLimitFilter`.

**ZAP-02 - Respuesta de Gestion de Sesion Identificada**
- **Severidad:** Informativo / Confianza: Media
- **Descripcion:** ZAP identifico el uso de JWT en las respuestas de autenticacion.
- **Evaluacion:** Comportamiento esperado para arquitectura stateless con JWT.

### 3.3 Observacion sobre el escaneo

El alto porcentaje de respuestas 4xx (97%) confirma que el control de acceso por JWT funciona correctamente. ZAP no pudo penetrar los endpoints protegidos sin token valido. Los endpoints publicos (`/auth/**`) respondieron correctamente con 200/201/400 segun el caso.

---

## 4. Ejecucion de Casos de Prueba GCS-04

| ID Caso     | Nombre                                                       | Resultado                     | Evidencia                                                                                                                                                                                                                                                 |
|-------------|--------------------------------------------------------------|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CP-MNT14-01 | Verificar headers de seguridad HTTP                          | **Parcial**                   | X-Content-Type-Options y X-Frame-Options presentes por Spring Security 7 default. CSP, HSTS y Permissions-Policy ausentes. Ver H-05.                                                                                                                      |
| CP-MNT14-02 | Ejecutar OWASP ZAP - 0 vulnerabilidades Alta/Critica         | **Pendiente**                 | ZAP no reporto Alta/Critica automatizadas. Los hallazgos Alta (H-01 a H-04) son de analisis manual/estatico. Pendiente scan de verificacion post-correcciones.                                                                                            |
| CP-MNT14-03 | Intentar inyeccion SQL en campos de busqueda                 | **Ejecutado - Mitigado**      | ZAP inserto payloads SQL en campos `codigo` y `email`. JPA parametrizado no ejecuto ningun payload como SQL. Los payloads quedaron como datos literales. BD no comprometida. Ver H-03.                                                                    |
| CP-MNT14-04 | Intentar XSS en campos de comentario y justificacion         | **Ejecutado - Parcial**       | `ChatService.sanitizar()` escapa correctamente HTML en mensajes guardados. Sin embargo, `notificacionService` recibe el contenido original sin sanitizar. Ver H-08. Payloads XSS en tabla mensajes del seed no causan ejecucion de script a nivel de API. |
| CP-MNT14-05 | Verificar ausencia de secrets en el repositorio              | **Ejecutado - Accion tomada** | `application-local.properties` contenia credenciales reales de BD y JWT secret. `application-dev.properties` contenia JWT secret. Ambos agregados a `.gitignore`. Pendiente `git rm --cached` y rotacion de credenciales. Ver H-01 y H-02.                |
| CP-MNT14-06 | Acceder a endpoint Admin con token de Estudiante             | **Ejecutado - Aprobado**      | Token de rol ESTUDIANTE en `GET /admin/tutores` retorna 403. Token de rol TUTOR en `GET /admin/tutores` retorna 403. Solo token ADMIN accede a `/admin/**`. Control RBAC funciona correctamente.                                                          |
| CP-MNT14-07 | Verificar que contrasenas no se devuelven en ningun endpoint | **Ejecutado - Aprobado**      | Campo `password` en entidad `Usuario` tiene `@JsonIgnore`. Ninguna respuesta de ningun endpoint incluye el campo password ni el hash BCrypt. Verificado en respuestas de login, perfil de tutor, listado de tutores y busqueda.                           |

---

## 5. Hallazgos de Seguridad

### 5.1 Severidad Alta

---

#### H-01 - Credenciales reales expuestas en repositorio

- **Caso de prueba:** CP-MNT14-05
- **Requisito:** RF-05, RN-04, RGC-03 (GCS-01)
- **Archivo:** `src/main/resources/application-local.properties`
- **Descripcion:** El archivo contenia credenciales reales de la base de datos PostgreSQL en Aiven (ahora Railway) y el JWT secret hardcodeados, y estaba siendo versionado en git. Corresponde al riesgo RGC-03 del plan de gestion de configuracion (Probabilidad: Baja, Impacto: Critico).

**Credenciales expuestas:**
```
URL de bases de datos
Usuario
Contrasena
JWT token secret
```

- **Riesgo:** Cualquier persona con acceso al repositorio tiene acceso completo a la base de datos y puede firmar tokens JWT validos para cualquier usuario.
- **Acciones tomadas:** `application-local.properties` agregado a `.gitignore`.
- **Acciones pendientes:**
    1. Ejecutar `git rm --cached src/main/resources/application-local.properties` para removerlo del indice de git
    2. Rotar password de base de datos en Railway
    3. Generar nuevo JWT secret para produccion distinto al de desarrollo
    4. Verificar que el archivo no quede en historial con `git log --all --full-history -- application-local.properties`

---

#### H-02 - JWT secret hardcodeado en perfil dev

- **Caso de prueba:** CP-MNT14-05
- **Requisito:** RF-05, RN-04
- **Archivo:** `src/main/resources/application-dev.properties`
- **Descripcion:** El archivo de configuracion dev contiene el JWT secret hardcodeado. Aunque es un valor de prueba, reutilizar el mismo secret entre entornos dev y produccion es una mala practica y el archivo estaba siendo versionado.

- **Riesgo:** Si el secret de dev coincide con el de produccion, un atacante con acceso al repo puede forjar tokens validos en produccion.
- **Acciones tomadas:** `application-dev.properties` agregado a `.gitignore`.
- **Acciones pendientes:**
    1. Ejecutar `git rm --cached src/main/resources/application-dev.properties`
    2. Usar un JWT secret diferente para dev que no coincida con el de produccion
    3. El secret de produccion debe gestionarse exclusivamente como variable de entorno en Railway

---

#### H-03 - Validacion insuficiente en campo `codigo` de Materia

- **Caso de prueba:** CP-MNT14-03, CP-MNT14-04
- **Requisito:** RF-04
- **Archivo:** `src/main/java/com/uco/tutorspace_api/domain/dto/CrearMateriaRequest.java`
- **Descripcion:** El campo `codigo` solo valida `@NotBlank` y `@Size(max=20)`. ZAP logro insertar en la base de datos payloads de SQL Injection, XSS, command injection y template injection.
- **Evidencia:** Base de datos contiene en tabla `materias` IDs del 10 al 50 con valores como `'`, `AND 1=1 --`, `CAT /ETC/PASSWD`, `SLEEP 15`, `<`, `<!--`, entre otros.
- **Riesgo:** Datos maliciosos en BD pueden generar XSS en frontends que rendericen estos datos sin escape. SQL Injection real prevenido por JPA parametrizado.
- **Alineacion con politica GCS-01:** Segun la politica de nomenclatura de codigos de materias (GCS-01 seccion 5.2), los codigos solo admiten letras mayusculas y numeros, formato `[PREFIJO][NUMERO]`, ejemplos: `PROG1`, `BD101`, `IS201`, `CAL1`. No se permiten caracteres especiales.
- **Correccion:**
```java
@NotBlank(message = "El codigo es requerido")
@Size(min = 2, max = 20, message = "El codigo debe tener entre 2 y 20 caracteres")
@Pattern(regexp = "^[A-Z][A-Z0-9]{1,19}$",
         message = "El codigo solo puede contener letras mayusculas y numeros, sin caracteres especiales")
String codigo;
```

---

#### H-04 - Validacion insuficiente en campos de nombre y email en creacion de usuarios

- **Caso de prueba:** CP-MNT14-03, CP-MNT14-04
- **Requisito:** RF-04
- **Archivos:** `CrearTutorRequest.java`, `RegisterRequest.java`
- **Descripcion:** Los campos `nombre` y `email` en creacion de tutores (via admin) y registro de estudiantes no tienen validacion de contenido suficiente. ZAP logro insertar URLs, payloads de template injection y SQL Injection.
- **Evidencia:**
    - ID 11 en tabla usuarios: `nombre = "http://www.google.com/"`
    - ID 12: `email = "beretutor@uco.net.co'"` (SQL injection)
    - ID 13: `email = "zj{@4514*3585}zj"` (template injection)
- **Nota importante:** `RegisterRequest` tiene validacion de dominio `@uco.net.co` en `AuthService`, pero `CrearTutorRequest` usado por el admin no pasa por esa validacion de dominio.
- **Correccion para nombre:**
```java
@NotBlank(message = "El nombre es requerido")
@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
@Pattern(regexp = "^[a-zA-Z ]{2,100}$",
         message = "El nombre solo puede contener letras y espacios")
String nombre;
```
- **Correccion para email en CrearTutorRequest:**
```java
@NotBlank(message = "El email es requerido")
@Email(message = "El formato del email no es valido")
@Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
         message = "El email tiene un formato invalido")
String email;
```

---

### 5.2 Severidad Media

---

#### H-05 - Headers de seguridad HTTP no configurados explicitamente

- **Caso de prueba:** CP-MNT14-01
- **Requisito:** RF-03, RNF-03
- **Archivo:** `src/main/java/com/uco/tutorspace_api/config/SecurityConfig.java`
- **Descripcion:** No existe bloque `.headers()` explicito en la configuracion de Spring Security. Spring Security 7 aplica X-Content-Type-Options y X-Frame-Options por defecto, pero RF-03 exige CSP, HSTS y X-Content-Type-Options configurados explicitamente.

**Inventario actual de headers:**

| Header                    | Estado                    | Valor actual                  |
|---------------------------|---------------------------|-------------------------------|
| X-Content-Type-Options    | Presente (Spring default) | nosniff                       |
| X-Frame-Options           | Presente (Spring default) | DENY                          |
| Cache-Control             | Presente (Spring default) | no-cache, no-store, max-age=0 |
| Content-Security-Policy   | **Ausente**               | -                             |
| Strict-Transport-Security | **Ausente**               | -                             |
| Permissions-Policy        | **Ausente**               | -                             |

- **Nota sobre HSTS:** El proyecto usa HTTP en desarrollo. HSTS solo tiene efecto sobre conexiones HTTPS. Debe configurarse condicionalmente o solo en el perfil `prod` donde Railway provee HTTPS.
- **Correccion en SecurityConfig.java:**
```/java
.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
    .frameOptions(frame -> frame.deny())
    .contentTypeOptions(Customizer.withDefaults())
    .permissionsPolicy(pp -> pp
        .policy("camera=(), microphone=(), geolocation=()"))
)
```
- **Para HSTS** agregar en `application-prod.properties`:
```properties
server.servlet.session.cookie.secure=true
```
Y condicionalmente en SecurityConfig al detectar perfil prod.

---

#### H-06 - Sin validacion en campo `jornadaGeneral`

- **Requisito:** RF-04
- **Archivos:** `CrearTutorRequest.java`, `AdminController.java` (record `ActualizarJornadaRequest`)
- **Descripcion:** El campo `jornadaGeneral` no tiene ninguna anotacion de validacion. Acepta cualquier string arbitrario.
- **Evidencia:** BD contiene tutores con `jornadaGeneral = "MANAN"`, `"12"`, `"xyz"`.
- **Correccion:**
```java
@Pattern(regexp = "^(MANANA|TARDE|NOCHE)$",
         message = "La jornada debe ser MANANA, TARDE o NOCHE")
String jornadaGeneral;
```

---

#### H-07 - Sin validacion en campo `dia` de disponibilidad

- **Requisito:** RF-04
- **Archivo:** `src/main/java/com/uco/tutorspace_api/domain/dto/CrearDisponibilidadRequest.java`
- **Descripcion:** El campo `dia` solo tiene `@NotBlank`. Como consecuencia, la validacion de solapamiento en `DisponibilidadRepository.findSolapadas()` no funciona con dias invalidos porque compara strings exactos.
- **Evidencia:** BD contiene disponibilidades con `dia = "JUEVESSSS"`. La query de solapamiento nunca las detecto.
- **Correccion:**
```java
@NotBlank(message = "El dia es requerido")
@Pattern(regexp = "^(LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SABADO|DOMINGO)$",
         message = "El dia debe ser un dia de la semana valido en mayusculas")
String dia;
```

---

#### H-08 - Contenido de mensajes llega sin sanitizar a notificaciones

- **Caso de prueba:** CP-MNT14-04
- **Requisito:** RF-04
- **Archivo:** `src/main/java/com/uco/tutorspace_api/service/ChatService.java`
- **Descripcion:** `enviarMensaje()` sanitiza el contenido antes de guardarlo en `Mensaje`, pero envia la notificacion usando `request.contenido()` original sin sanitizar. La cadena que llega a `NotificacionService` y se guarda en `notificaciones` puede contener HTML/scripts.

**Codigo problematico:**
```/java
mensaje.setContenido(sanitizar(request.contenido()));  // correcto
// ...
notificacionService.enviarNotificacion(
    destinatario,
    TipoNotificacion.NUEVO_MENSAJE,
    emisor.getNombre() + ": " + request.contenido()  // sin sanitizar
);
```

- **Riesgo:** XSS potencial si el frontend renderiza notificaciones sin escape.
- **Correccion:**
```/java
String contenidoSanitizado = sanitizar(request.contenido());
mensaje.setContenido(contenidoSanitizado);
// ...
notificacionService.enviarNotificacion(
    destinatario,
    TipoNotificacion.NUEVO_MENSAJE,
    emisor.getNombre() + ": " + contenidoSanitizado
);
```

---

#### H-09 - Sesiones aceptan fechas pasadas

- **Requisito:** RF-04
- **Archivo:** `src/main/java/com/uco/tutorspace_api/domain/dto/CrearSesionRequest.java`
- **Descripcion:** El campo `fecha` solo valida `@NotNull`. Es posible crear sesiones con fechas en el pasado.
- **Correccion:**
```java
@NotNull(message = "La fecha es requerida")
@FutureOrPresent(message = "La fecha de la sesion no puede ser en el pasado")
LocalDate fecha;
```

---

#### H-10 - Validacion insuficiente en campo `nombre` de Materia

- **Requisito:** RF-04
- **Archivo:** `src/main/java/com/uco/tutorspace_api/domain/dto/CrearMateriaRequest.java`
- **Descripcion:** El campo `nombre` solo tiene `@NotBlank`. ZAP logro insertar URLs y scripts como nombre de materia.
- **Evidencia:** BD contiene `nombre = "http://www.google.com/"`.
- **Correccion:**
```java
@NotBlank(message = "El nombre es requerido")
@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
@Pattern(regexp = "^[a-zA-Z0-9 ]{2,100}$",
         message = "El nombre solo puede contener letras, numeros y espacios")
String nombre;
```

---

#### H-11 - IDs en requests sin validacion de valor positivo

- **Requisito:** RF-04
- **Archivos:** `AsignarMateriaRequest.java`, `IniciarChatRequest.java`, `CrearSesionRequest.java`
- **Descripcion:** Los campos de tipo ID (`materiaId`, `tutorId`, `estudianteId`, `disponibilidadId`) solo tienen `@NotNull`. No se valida que sean valores positivos, permitiendo enviar IDs negativos o cero.
- **Correccion:** Agregar `@Positive(message = "El ID debe ser un valor positivo")` en cada campo ID de los tres DTOs.

---

### 5.3 Severidad Baja

---

#### H-12 - Sin validacion de estado al activar/desactivar tutor

- **Requisito:** RF-04
- **Archivo:** `TutorService.java`
- **Descripcion:** Activar un tutor ya ACTIVO o desactivar uno ya INACTIVO retorna 200 sin mensaje. Comportamiento silencioso.
- **Plan de accion:** Validar estado actual en `activarTutor()` y `desactivarTutor()` y lanzar excepcion descriptiva.

---

#### H-13 - Sin mensaje al retirar materia no asignada

- **Requisito:** RF-04
- **Archivo:** `TutorService.java`
- **Descripcion:** `retirarMateria()` usa `removeIf` sin verificar si la materia estaba asignada. Retorna 200 sin efecto real.
- **Plan de accion:** Verificar existencia en lista antes del `removeIf` y lanzar excepcion descriptiva.

---

#### H-14 - Perfil activo hardcodeado en application.properties

- **Requisito:** RNF-01, GCS-02
- **Archivo:** `src/main/resources/application.properties`
- **Descripcion:** Durante las pruebas el archivo tenia `spring.profiles.active=dev` hardcodeado. El valor original correcto es `${SPRING_PROFILES_ACTIVE:local}`.
- **Riesgo:** Si se hace merge con `dev` hardcodeado, en cualquier entorno sin la variable de entorno se activaria H2 en lugar de PostgreSQL.
- **Plan de accion:** Revertir a `spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}` antes del merge a Develop. **Esta correccion debe hacerse ANTES de abrir el PR.**

---

## 6. Verificaciones Positivas

Controles verificados y aprobados. Todos los casos CP-MNT14-06 y CP-MNT14-07 incluidos:

| Control                       | Caso        | Resultado | Detalle                                                                 |
|-------------------------------|-------------|-----------|-------------------------------------------------------------------------|
| Autenticacion JWT stateless   | CP-MNT14-02 | Aprobado  | Tokens validados en cada request                                        |
| Expiracion de token           | CP-MNT14-02 | Aprobado  | Token vencido rechazado con 401                                         |
| Control de acceso ADMIN       | CP-MNT14-06 | Aprobado  | Token TUTOR/ESTUDIANTE en `/admin/**` retorna 403                       |
| Control de acceso TUTOR       | CP-MNT14-06 | Aprobado  | Token ESTUDIANTE en `/tutor/**` retorna 403                             |
| Control de acceso ESTUDIANTE  | CP-MNT14-06 | Aprobado  | Token ADMIN en `/estudiante/**` retorna 403                             |
| Password no expuesto en JSON  | CP-MNT14-07 | Aprobado  | `@JsonIgnore` en `Usuario.password` - verificado en todos los endpoints |
| Rate limiting `/auth/**`      | CP-MNT14-02 | Aprobado  | 429 despues de 20 intentos/minuto                                       |
| BCrypt strength 12            | CP-MNT14-02 | Aprobado  | Factor de costo 12 configurado                                          |
| SQL Injection via JPA         | CP-MNT14-03 | Aprobado  | Queries parametrizadas, payloads no ejecutados                          |
| Sanitizacion de mensajes chat | CP-MNT14-04 | Aprobado  | `sanitizar()` escapa `<`, `>`, `"`, `'` correctamente                   |
| CORS con lista blanca         | CP-MNT14-02 | Aprobado  | Solo dominios permitidos aceptados                                      |
| WebSocket con JWT             | CP-MNT14-02 | Aprobado  | Conexiones sin token rechazadas                                         |
| Sesion STATELESS              | CP-MNT14-02 | Aprobado  | No se crean sesiones HTTP                                               |
| CSRF deshabilitado            | CP-MNT14-02 | Aprobado  | Correcto para API REST con JWT                                          |

---

## 7. Tabla Consolidada de Hallazgos

| ID   | Descripcion                                            | Severidad | Caso GCS-04    | RF     | Estado                                                             |
|------|--------------------------------------------------------|-----------|----------------|--------|--------------------------------------------------------------------|
| H-01 | Credenciales reales en `application-local.properties`  | Alta      | CP-MNT14-05    | RF-05  | Gitignore aplicado. Pendiente git rm --cached y rotar credenciales |
| H-02 | JWT secret en `application-dev.properties`             | Alta      | CP-MNT14-05    | RF-05  | Gitignore aplicado. Pendiente git rm --cached y separar secrets    |
| H-03 | Validacion debil en `codigo` de Materia                | Alta      | CP-MNT14-03/04 | RF-04  | Pendiente correccion en DTO                                        |
| H-04 | Validacion debil en `nombre` y `email` de Usuario      | Alta      | CP-MNT14-03/04 | RF-04  | Pendiente correccion en DTOs                                       |
| H-05 | Headers HTTP faltantes (CSP, HSTS, Permissions-Policy) | Media     | CP-MNT14-01    | RF-03  | Pendiente configuracion en SecurityConfig                          |
| H-06 | Sin validacion en `jornadaGeneral`                     | Media     | -              | RF-04  | Pendiente correccion en DTO                                        |
| H-07 | Sin validacion en `dia` de disponibilidad              | Media     | -              | RF-04  | Pendiente correccion en DTO                                        |
| H-08 | Notificacion usa contenido sin sanitizar               | Media     | CP-MNT14-04    | RF-04  | Pendiente correccion en ChatService                                |
| H-09 | Sesiones aceptan fechas pasadas                        | Media     | -              | RF-04  | Pendiente correccion en DTO                                        |
| H-10 | Validacion debil en `nombre` de Materia                | Media     | CP-MNT14-03/04 | RF-04  | Pendiente correccion en DTO                                        |
| H-11 | IDs sin `@Positive` en requests                        | Media     | -              | RF-04  | Pendiente correccion en DTOs                                       |
| H-12 | Activar/desactivar tutor sin validacion de estado      | Baja      | -              | RF-04  | Pendiente correccion en Service                                    |
| H-13 | Retirar materia no asignada sin mensaje                | Baja      | -              | RF-04  | Pendiente correccion en Service                                    |
| H-14 | Perfil `dev` hardcodeado en `application.properties`   | Baja      | -              | RNF-01 | Pendiente revertir ANTES del merge                                 |

---

## 8. Plan de Accion por Archivo

### `CrearMateriaRequest.java`
- `nombre`: agregar `@Size(min=2, max=100)` y `@Pattern(regexp = "^[a-zA-Z0-9 ]{2,100}$")`
- `codigo`: agregar `@Size(min=2, max=20)` y `@Pattern(regexp = "^[A-Z][A-Z0-9]{1,19}$")`

### `CrearTutorRequest.java`
- `nombre`: agregar `@Pattern(regexp = "^[a-zA-Z ]{2,100}$")`
- `email`: agregar `@Pattern` de refuerzo
- `jornadaGeneral`: agregar `@Pattern(regexp = "^(MANANA|TARDE|NOCHE)$")`
- `password`: agregar `@Size(max=100)`

### `RegisterRequest.java`
- `nombre`: agregar `@Pattern(regexp = "^[a-zA-Z ]{2,100}$")`
- `password`: agregar `@Size(max=100)`

### `CrearDisponibilidadRequest.java`
- `dia`: agregar `@Pattern(regexp = "^(LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SABADO|DOMINGO)$")`

### `CrearSesionRequest.java`
- `fecha`: agregar `@FutureOrPresent`
- `estudianteId`, `disponibilidadId`: agregar `@Positive`

### `AsignarMateriaRequest.java`
- `materiaId`: agregar `@Positive`

### `IniciarChatRequest.java`
- `tutorId`: agregar `@Positive`

### `AdminController.java` - record `ActualizarJornadaRequest`
- `jornadaGeneral`: agregar `@Pattern(regexp = "^(MANANA|TARDE|NOCHE)$")`

### `ChatService.java`
- Extraer `sanitizar(request.contenido())` a variable local y usarla en mensaje y notificacion

### `TutorService.java`
- `activarTutor()`: validar que estado actual no sea ACTIVO antes de activar
- `desactivarTutor()`: validar que estado actual no sea INACTIVO antes de desactivar
- `retirarMateria()`: verificar que la materia este en la lista antes del `removeIf`

### `SecurityConfig.java`
- Agregar bloque `.headers()` con CSP, Permissions-Policy, X-Frame-Options y X-Content-Type-Options explicitos
- HSTS: configurar solo en perfil prod

### `application.properties`
- Revertir a `spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}` antes del merge

### Git
- `git rm --cached src/main/resources/application-local.properties`
- `git rm --cached src/main/resources/application-dev.properties`
- Rotar credenciales de Railway
- Generar JWT secret diferente para dev y prod

---

## 9. Verificacion de Criterios de Salida GCS-03

| Criterio                                                     | Estado                                                                          |
|--------------------------------------------------------------|---------------------------------------------------------------------------------|
| 100% casos de prueba prioridad Alta ejecutados y aprobados   | **Pendiente** - CP-MNT14-02 requiere scan final post-correcciones               |
| 0 defectos Alta o Critica abiertos sin plan de accion        | **En progreso** - H-01 a H-04 tienen plan documentado, pendiente implementacion |
| Defectos Media con plan de accion documentado                | **Aprobado** - H-05 a H-11 documentados con correccion especifica               |
| Analisis OWASP ZAP final sin vulnerabilidades Alta o Critica | **Pendiente** - se ejecuta despues de aplicar todas las correcciones            |

### Scan de verificacion final

Una vez implementadas todas las correcciones de H-01 a H-14, se ejecutara un scan de verificacion con OWASP ZAP 2.17.0 siguiendo el mismo procedimiento de este informe. El objetivo es confirmar 0 vulnerabilidades Alta o Critica, requisito de cierre del sprint segun GCS-03 seccion 8.

---

## 10. Trazabilidad GCS-01

| Artefacto              | Referencia                                               |
|------------------------|----------------------------------------------------------|
| Rama Git               | fix/mnt-14-mejoras-seguridad                             |
| Commits                | fix(mnt-14): descripcion del cambio                      |
| Pull Request           | [MNT-14] Mejoras de seguridad - OWASP ZAP y correcciones |
| Documento              | INFORME-MNT-14                                           |
| Casos de prueba        | CP-MNT14-01 a CP-MNT14-07 (GCS-04)                       |
| Componente impactado   | SecurityConfig (Alto), application.properties (Medio)    |
| Riesgo GCS-01 mitigado | RGC-03 - Secrets en repositorio                          |

---

## 11. Notas Tecnicas

**Sobre SQL Injection:** JPA con queries parametrizadas previene SQL Injection real en consultas. Los payloads insertados por ZAP quedaron como datos literales en BD, no fueron ejecutados como comandos SQL. El riesgo es de segundo orden: XSS si el frontend renderiza esos datos sin escape. Mitigado parcialmente por sanitizacion en ChatService y completamente al aplicar H-03 y H-04.

**Sobre el dominio de emails en data.sql:** `AuthService` valida que el email de registro termine en `@uco.net.co`, pero el seed tiene usuarios con dominio `@uco.edu.co`. Estos usuarios existen solo en el entorno de prueba local y no comprometen la logica de produccion. El seed (`data.sql`) esta en `.gitignore` y no se versiona.

**Sobre el perfil de pruebas ZAP:** GCS-03 describe un perfil llamado `zap` con H2 en memoria. Se uso el perfil `dev` con H2 file-based, que es funcionalmente equivalente para los objetivos del scan. La diferencia (memoria vs archivo) no afecta los resultados de seguridad.

**Sobre HSTS en desarrollo:** HSTS (Strict-Transport-Security) solo tiene efecto en conexiones HTTPS. El backend en desarrollo usa HTTP plano, por lo que el header no aplica en ese contexto. En produccion, Railway provee HTTPS automaticamente y el header debe configurarse en el perfil `prod`.

---

*Documento: INFORME-MNT-14 - Mejoras de Seguridad TutorSpace*
*Generado en el contexto de MNT-14 - Sprint 1 Junio 2026*
*Fuentes: OWASP ZAP 2.17.0 + analisis estatico + pruebas manuales*
*Trazabilidad: GCS-01, GCS-02, GCS-03, GCS-04*
*Ultima actualizacion: 6 de junio de 2026*