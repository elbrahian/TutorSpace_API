
-- Administrador

INSERT INTO usuarios
    (tipo, nombre, email, password, rol, estado, created_at)
VALUES (
        'ADMINISTRADOR',
        'Admin Test',
        'admin@uco.net.co',
        '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
        'ADMIN',
        'ACTIVO',
        CURRENT_TIMESTAMP);

INSERT INTO administradores (id)
SELECT id FROM usuarios WHERE email = 'admin@uco.net.co';

-- Tutores

INSERT INTO usuarios
(id, tipo, nombre, email, password, rol, estado, created_at)
VALUES
    (2,
     'TUTOR',
     'Juan Tutor',
     'tutor1@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'TUTOR',
     'ACTIVO',
     CURRENT_TIMESTAMP),

    (3,
     'TUTOR',
     'Maria Tutor',
     'tutor2@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'TUTOR',
     'ACTIVO',
     CURRENT_TIMESTAMP),

    (4,
     'TUTOR',
     'Pedro Tutor',
     'tutor3@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'TUTOR',
     'ACTIVO',
     CURRENT_TIMESTAMP);

INSERT INTO tutores(id,jornada_general)
VALUES
    (2,'MANANA'),
    (3,'TARDE'),
    (4,'NOCHE');

-- Estudiantes

INSERT INTO usuarios
(id, tipo, nombre, email, password, rol, estado, created_at)
VALUES
    (5,
     'ESTUDIANTE',
     'Mateo Estudiante',
     'est1@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'ESTUDIANTE',
     'ACTIVO',
     CURRENT_TIMESTAMP),

    (6,
     'ESTUDIANTE',
     'Carlos Estudiante',
     'est2@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'ESTUDIANTE',
     'ACTIVO',
     CURRENT_TIMESTAMP),

    (7,
     'ESTUDIANTE',
     'Ana Estudiante',
     'est3@uco.edu.co',
     '$2a$10$M1pIAtV9B7wwqjuL2m0Pu.eAeLkBSctwMrdVnXt/KNKrYOm95PZ9a',
     'ESTUDIANTE',
     'ACTIVO',
     CURRENT_TIMESTAMP);

INSERT INTO estudiantes(id)
VALUES
    (5),
    (6),
    (7);

-- Materias

INSERT INTO materias(id,nombre,codigo)
VALUES
    (1,'Matematicas','MAT101'),
    (2,'Programacion','PRO101'),
    (3,'Bases de Datos','BDD101'),
    (4,'Calculo','CAL101'),
    (5,'Fisica','FIS101');

-- Tutor-Materia

INSERT INTO tutor_materia(tutor_id,materia_id)
VALUES
    (2,1),
    (2,4),
    (3,2),
    (3,3),
    (4,5);

-- Chats

INSERT INTO chats(id,tutor_id,estudiante_id,fecha_creacion)
VALUES
    (1,2,5,CURRENT_TIMESTAMP),
    (2,2,6,CURRENT_TIMESTAMP),
    (3,3,7,CURRENT_TIMESTAMP);

-- Mensajes

INSERT INTO mensajes(id,chat_id,emisor_id,contenido,fecha)
VALUES
    (1,1,5,'Hola tutor',CURRENT_TIMESTAMP),

    (2,1,2,'Hola, en que puedo ayudarte?',CURRENT_TIMESTAMP),

    (3,1,5,'Necesito ayuda con calculo',CURRENT_TIMESTAMP),

    (4,2,6,'Consulta sobre programacion',CURRENT_TIMESTAMP),

    (5,2,2,'Claro, dime tu duda',CURRENT_TIMESTAMP),

    (6,3,7,'<script>alert(1)</script>',CURRENT_TIMESTAMP),

    (7,3,3,'Mensaje de prueba',CURRENT_TIMESTAMP),

    (8,3,7,' OR 1=1 --',CURRENT_TIMESTAMP),

    (9,3,3,'<img src=x onerror=alert(1)>',CURRENT_TIMESTAMP);

-- Disponibilidades

INSERT INTO disponibilidades
(id,tutor_id,dia,hora_inicio,hora_fin,estado,created_at)
VALUES

    (1,2,'LUNES','08:00:00','10:00:00','DISPONIBLE',CURRENT_TIMESTAMP),

    (2,2,'MIERCOLES','14:00:00','16:00:00','DISPONIBLE',CURRENT_TIMESTAMP),

    (3,3,'MARTES','09:00:00','11:00:00','DISPONIBLE',CURRENT_TIMESTAMP),

    (4,4,'VIERNES','15:00:00','18:00:00','DISPONIBLE',CURRENT_TIMESTAMP);

-- Sesiones

INSERT INTO sesiones
(id,tutor_id,estudiante_id,disponibilidad_id,
 fecha,hora_inicio,hora_fin,estado,created_at)
VALUES

    (1,2,5,1,
     DATE '2026-06-10',
     TIME '08:00:00',
     TIME '09:00:00',
     'PENDIENTE',
     CURRENT_TIMESTAMP),

    (2,3,7,3,
     DATE '2026-06-12',
     TIME '09:00:00',
     TIME '10:00:00',
     'APROBADA',
     CURRENT_TIMESTAMP);

-- Historial de Sesiones

INSERT INTO historial_sesiones
(id,sesion_id,estado_anterior,estado_nuevo,fecha_cambio)
VALUES

    (1,2,'PENDIENTE','APROBADA',CURRENT_TIMESTAMP);

-- Notificaciones

INSERT INTO notificaciones
(id,usuario_id,tipo,mensaje,fecha,leida)
VALUES

    (1,5,'NUEVO_MENSAJE',
     'Tienes un nuevo mensaje',
     CURRENT_TIMESTAMP,
     FALSE),

    (2,2,'SESION_CREADA',
     'Nueva sesion programada',
     CURRENT_TIMESTAMP,
     FALSE),

    (3,7,'CAMBIO_ESTADO',
     'Tu sesion fue aprobada',
     CURRENT_TIMESTAMP,
     TRUE);

ALTER TABLE usuarios ALTER COLUMN id RESTART WITH 10;
ALTER TABLE materias ALTER COLUMN id RESTART WITH 10;
ALTER TABLE disponibilidades ALTER COLUMN id RESTART WITH 10;
ALTER TABLE sesiones ALTER COLUMN id RESTART WITH 10;
ALTER TABLE historial_sesiones ALTER COLUMN id RESTART WITH 10;
ALTER TABLE notificaciones ALTER COLUMN id RESTART WITH 10;
ALTER TABLE chats ALTER COLUMN id RESTART WITH 10;
ALTER TABLE mensajes ALTER COLUMN id RESTART WITH 10;


