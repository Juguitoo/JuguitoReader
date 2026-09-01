# Roadmap — JuguitoReader

Roadmap del proyecto. Fuente única de verdad junto con [BACKLOG.md](BACKLOG.md). IDs `TAR-xxx` para features planificadas.

**Visión:** lector EPUB + gestor de lecturas (digitales y físicos). Backend/suscripción pospuesto hasta app local sólida.

---

## v1.2.x — Cierre de estabilización *(en curso)*

Funcionalidades de v1.2 **completadas** + corrección de bugs del audit (ver [KNOWN_ISSUES.md](KNOWN_ISSUES.md), scope v1.2.0 en [BACKLOG.md](BACKLOG.md)).

### Features Done

| ID | Tarea |
|----|-------|
| TAR-25 | Cálculo de velocidad de lectura (JS bridge → sesiones) |
| TAR-27 | Registro de sesiones de lectura por libro |
| TAR-28 | Idiomas (i18n) |
| TAR-44 | Automatizaciones Pendiente → Leyendo / 100% → Finalizado |
| TAR-45 | Sugerencia cambio de estado al abrir libro |
| TAR-46 | Porcentaje en portadas (libros leyendo) |
| TAR-55 | Ajuste tema neon (contrastes) |

### Trabajo pendiente v1.2.0

- [ ] Resolver issues P0/P1 del audit ([BACKLOG.md — v1.2.0 estabilización](BACKLOG.md#v120--estabilización-cierre))
- [ ] Eliminar scaffold sync (CLEAN-001)
- [ ] Reorganizar rama git `v1.2.0` → `feature/*` o merge a `dev` (GIT-001)

---

## v1.3.0 — Biblioteca y estadísticas

| ID | Tarea | Estado |
|----|-------|--------|
| TAR-19 | Recopilación de estadísticas (PPM, velocidad…) | Not started |
| TAR-20 | Sección estadísticas (libros/mes, media, páginas/día) | Not started |
| TAR-47 | Reestructuración carpetas (archivadores, no tags) | Not started |
| TAR-49 | Controles biblioteca (filtros + ordenación) | Not started |
| TAR-50 | Filtro por autor (registro + biblioteca) | Not started |
| TAR-51 | Marcadores / subrayado en libros | Not started |
| TAR-52 | Tipografías personalizables en lector | Not started |
| TAR-53 | Modo horizontal | Not started |
| TAR-54 | Sistema favoritos + estantería en Home | Not started |
| TAR-56 | Colapsar sección gestión en drawer | Not started |

**Notas de diseño:**
- TAR-19/TAR-25: desacoplar stats del WebView para futuro motor nativo.

---

## v1.4.0 — EPUB3

| ID | Tarea | Estado |
|----|-------|--------|
| TAR-31 | Compatibilidad básica EPUB3 (nav document, parsing OPF 3.x) | Not started |
| TAR-32 | Compatibilidad avanzada EPUB3 (Media Overlays, scripts, notas) | Not started |
| READER-009 | EPUB3 nav document incompleto | Diferido → TAR-31 |
| READER-008 | TOC con `#fragment` (navegación índice) | Diferido → TAR-31 |

Issues READER-008 y READER-009 permanecen en [KNOWN_ISSUES.md](KNOWN_ISSUES.md) como limitaciones conocidas; fuera del cierre de v1.2.0.

---

## v2.0.0 — Cloud (pospuesto)

| ID | Tarea | Estado |
|----|-------|--------|
| TAR-29 | Conexión Supabase/Dropbox, arquitectura local-first | Not started |

**Decisión actual:** eliminar scaffold sync de v1.x. Reimplementar cuando el producto local esté maduro.

---

## v3.0.0 — Motor nativo

| ID | Tarea | Estado |
|----|-------|--------|
| TAR-30 | Motor visor EPUB propio (EPUB → Compose nativo) | Not started |

---

## Versiones completadas

### v1.0.0

| ID | Tarea |
|----|-------|
| TAR-6 | Configurar proyecto |
| TAR-7 | Inicializar BD |
| TAR-8 | Capa repositorio |
| TAR-9 | Capa ViewModel |
| TAR-10 | Interfaz principal (Home) |
| TAR-11 | Formulario libros |
| TAR-13 | Configurar Supabase *(scaffold — eliminar en v1.2)* |
| TAR-14 | Casos de uso |
| TAR-15 | Detalle libro |
| TAR-16 | Biblioteca |
| TAR-17 | Registro |
| TAR-18 | Lector EPUB |
| TAR-21 | Gestión carpetas y géneros |

### v1.0.2

| ID | Tarea |
|----|-------|
| TAR-36 | Bug tamaño letra y scroll visor |

### v1.1.0

| ID | Tarea |
|----|-------|
| TAR-22 | Pantalla ajustes |
| TAR-23 | Diálogos + snackbars |
| TAR-24 | Funcionalidad deshacer |
| TAR-26 | Importar desde biblioteca |
| TAR-33 | Mejorar Home vacío |
| TAR-34 | Persistencia ajustes lector |
| TAR-37 | Crear carpetas desde formulario |
| TAR-38 | Botones crear carpeta/género en gestor |
| TAR-39 | Creador de géneros |
| TAR-40 | Iconos gestor |
| TAR-42 | Mejorar formulario carpeta |
| TAR-43 | Mejorar gestor + conteo libros |

---

## Pre-publicación Play Store *(sin versión asignada aún)*

- Keystore release + R8 (REL-001, REL-002)
- CI básico (REL-003)
- Política migraciones DB
- Privacy policy

Ver [BACKLOG.md — P1](BACKLOG.md#p1--pre-release).

---

## Mantenimiento

Editar este archivo al planificar versiones nuevas o cerrar fases. Ver [MAINTENANCE.md](MAINTENANCE.md).
