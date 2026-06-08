# MNT-11 — Reporte de Uso de la Plataforma por Rol

| Campo | Detalle |
|---|---|
| **ID** | MNT-11 |
| **Tipo** | Perfectivo |
| **Desarrollador** | Carmona Ramírez Jorge Andrés |
| **Repos** | `TutorSpace_API` (backend) · `TutorSpace_APP` (frontend) |
| **Rama** | `feature/mnt-11-reporte-uso-rol` (en ambos repos) |
| **Rama base** | `Develop` (API) / `develop` (APP) |
| **Sprint** | Sprint 1 — Junio 2026 |

---

## 1. Objetivo

Generar estadísticas de uso de la plataforma diferenciadas por rol (**Estudiante, Tutor, Administrador**),
permitiendo conocer patrones de utilización: usuarios activos, sesiones creadas, mensajes enviados y
actividad por período de tiempo, con un gráfico de actividad semanal.

## 2. Trazabilidad de requisitos

| Req. | Descripción | Cómo se cumple |
|---|---|---|
| RF-01 | Endpoint `GET /admin/reportes/uso` con métricas por rol y período | `AdminController.reporteUso()` → `ReporteUsoService` |
| RF-02 | Métricas: usuarios activos, sesiones creadas, mensajes enviados, por rol | `UsoPorRolResponse` (tabla) + tarjetas resumen |
| RF-03 | Gráfico de actividad semanal o mensual por rol | `GraficoActividadSemanal.tsx` (SVG, serie por rol) |
| RF-04 | Filtrar por rango de fechas | Parámetros `fechaInicio` / `fechaFin` (front y back) |
| RF-05 | Si no existe tabla de logs de actividad, debe crearse | **Ver decisión de diseño (§4)** |
| RNF-01 | Solo el rol ADMIN puede acceder | `SecurityConfig`: `/admin/**` → `hasRole('ADMIN')` |
| RNF-02 | Respuesta en menos de 3 s | Consultas proyectadas (no entidades completas) + agregación O(n) en memoria |
| RNF-03 | El registro de actividad no impacta el rendimiento | Solo lectura sobre tablas existentes; cero escrituras nuevas |
| RN-02 | Usuario activo = al menos una acción en el período | Conjunto de IDs distintos con sesión o mensaje en el rango |
| RN-03 | Los logs de actividad son de solo lectura | El reporte nunca escribe; solo consulta |

## 3. Arquitectura de la solución

```
[Admin] ──HTTP GET /admin/reportes/uso?fechaInicio&fechaFin──►
   Frontend (React)                         Backend (Spring Boot)
   ├─ ReporteUsoPorRol.tsx                   ├─ AdminController
   │   ├─ tarjetas resumen                   │    └─ reporteUso()
   │   ├─ GraficoActividadSemanal.tsx        ├─ ReporteUsoService  (agregación)
   │   └─ exportarReportePdf()  ──► PDF      ├─ SesionRepository.findActividadSesiones()
   └─ reportesApi.getUsoPorRol()             └─ MensajeRepository.findActividadMensajes()
                                                     │
                                                     ▼
                                             PostgreSQL: sesiones, mensajes, usuarios
```

## 4. Decisión de diseño — RF-05 (tabla de logs de actividad)

RF-05 plantea crear una tabla de logs de actividad si no existe. Se evaluaron dos caminos:

1. **Interceptor/AOP global con logging asíncrono** hacia una nueva tabla `log_actividad`.
2. **Reconstruir la actividad (solo lectura) desde las tablas de dominio existentes** (`sesiones`, `mensajes`, `usuarios`).

**Se eligió la opción 2** por las siguientes razones, alineadas con los requisitos y con la naturaleza
colaborativa del repositorio:

- **RNF-03 (no impactar el rendimiento):** no se añade ninguna escritura ni interceptor en el camino de
  ejecución de los demás módulos. El reporte es 100 % de solo lectura.
- **Datos reales desde el primer día:** una tabla de logs nueva arrancaría vacía; reconstruir desde el
  dominio entrega métricas históricas reales de inmediato (necesario para la demo y el PDF).
- **No desestabiliza el trabajo de otros desarrolladores:** la implementación es puramente aditiva
  (nuevos DTOs, nuevo servicio, nuevo endpoint, nuevas consultas), sin modificar entidades ni flujos
  compartidos.

> Si en una iteración futura se requiere auditar acciones que hoy no dejan rastro en el dominio
> (p. ej. inicios de sesión o navegación), se recomienda introducir `log_actividad` con escritura
> asíncrona (`@Async`) y consumirla desde este mismo servicio sin cambiar el contrato del endpoint.

## 5. Contrato del endpoint

`GET /admin/reportes/uso`

**Query params** (opcionales): `fechaInicio`, `fechaFin` (formato ISO `yyyy-MM-dd`).

**Respuesta `200 OK` — `ReporteUsoResponse`:**

```json
{
  "estudiantesActivos": 42,
  "tutoresActivos": 9,
  "adminsActivos": 1,
  "totalSesionesCreadas": 87,
  "totalMensajesEnviados": 540,
  "metricasPorRol": [
    { "rol": "ESTUDIANTE", "usuariosActivos": 42, "sesiones": 87, "mensajesEnviados": 310 },
    { "rol": "TUTOR",      "usuariosActivos": 9,  "sesiones": 87, "mensajesEnviados": 225 },
    { "rol": "ADMIN",      "usuariosActivos": 1,  "sesiones": 0,  "mensajesEnviados": 5 }
  ],
  "actividadSemanal": [
    { "semana": "26/05", "inicioSemana": "2026-05-26", "estudiante": 12, "tutor": 8, "admin": 1 }
  ]
}
```

### Definición de las métricas

- **Usuarios activos por rol:** IDs distintos de usuarios de ese rol con **al menos una acción**
  (una sesión o un mensaje) dentro del período (RN-02).
- **Sesiones:** sesiones en las que el rol participa (el estudiante la solicita, el tutor la atiende).
  Para ADMIN es 0 por definición del dominio.
- **Mensajes enviados:** mensajes cuyo emisor tiene ese rol.
- **Actividad semanal:** acciones agregadas por semana (lunes que la inicia), una serie por rol.
  Se limita a las **12 semanas más recientes** del conjunto resultante.

## 6. Archivos del cambio

### Backend — `TutorSpace_API`
| Archivo | Tipo |
|---|---|
| `domain/dto/ReporteUsoResponse.java` | nuevo |
| `domain/dto/UsoPorRolResponse.java` | nuevo |
| `domain/dto/ActividadSemanalResponse.java` | nuevo |
| `service/ReporteUsoService.java` | nuevo |
| `repositories/SesionRepository.java` | + `findActividadSesiones()` |
| `repositories/MensajeRepository.java` | + `findActividadMensajes()` |
| `controllers/AdminController.java` | + endpoint `GET /admin/reportes/uso` |

### Frontend — `TutorSpace_APP`
| Archivo | Tipo |
|---|---|
| `pages/admin/ReporteUsoPorRol.tsx` | nuevo |
| `components/reportes/GraficoActividadSemanal.tsx` | nuevo |
| `api/reportesApi.ts` | + `getUsoPorRol()` |
| `types/index.ts` | + tipos `ReporteUsoResponse`, `UsoPorRolResponse`, `ActividadSemanalResponse` |
| `router/AppRouter.tsx` | + ruta `/admin/reportes/uso` |
| `components/layout/Sidebar.tsx` | + enlace "Uso por Rol" en sección Reportes |

## 7. Consistencia visual (reutilización)

Se reutilizan los mismos componentes y utilidades de los reportes existentes para conservar tipografía,
colores y comportamiento:

- **Color primario** `#2563eb`, escala `slate` para textos — idéntico al reporte de Desempeño.
- **`exportarReportePdf`**: misma utilidad de exportación a PDF (encabezado azul + "TutorSpace",
  tipografía Helvetica/Arial, pie con paginación) que los demás reportes.
- **`DashboardLayout`, `Card`, `Button`, `Input`**: mismos componentes compartidos.
- Gráfico en **SVG** propio (sin dependencias nuevas), con colores: Estudiantes `#2563eb`,
  Tutores `#16a34a`, Administradores `#f59e0b`.

## 8. Verificación realizada

- **Backend:** `mvnw compile` ✅ · `mvnw test` → **125 tests, 0 fallos, 0 errores** ✅
- **Frontend:** `tsc -b && vite build` ✅ · `eslint` de los archivos del cambio ✅
- **PDF:** se exporta con la misma utilidad probada por los reportes existentes.

## 9. Diagramas (draw.io)

En `Documentacion/MNT-11/diagramas/`:

- `CU-M11-casos-de-uso.drawio` — Caso de uso CU-M11.
- `MNT-11-secuencia.drawio` — Diagrama de secuencia del flujo de consulta y exportación.
- `MNT-11-arquitectura.drawio` — Diagrama de componentes/arquitectura.
