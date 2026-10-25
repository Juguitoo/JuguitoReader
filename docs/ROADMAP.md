# Roadmap — JuguitoReader

**Visión:** Libros digitales y físicos. Una app local. Importa EPUBs, léelos en la app y registra también lo que lees en papel — fechas, nota, comentario, estado y estadísticas de sesión — sin backend.

md-flow lee cada `## vX.Y.Z — Título`, con raya `—`. No vale `## vX.Y.x` ni `###`. `estado` es `prevista`, `en curso` o `publicada`, y coloca el punto. El párrafo de debajo es el resumen al pulsar. La visión es solo la línea `**Visión:**`.

## v1.0.0 — Base de la app

- estado: publicada

Primera release: Clean Architecture, Room, Home, biblioteca, registro, lector EPUB, carpetas y géneros. El hotfix v1.0.2 corrigió el scroll.

## v1.1.0 — UX, undo y gestor

- estado: publicada

Ajustes, diálogos, deshacer, importar desde la biblioteca y gestor de carpetas y géneros.

## v1.2.0 — Estabilización

- estado: publicada

Auditoría y features de la línea 1.2, orientadas a pruebas cerradas en Play. READER-008 y READER-009 quedaron diferidos a la 1.4.0.

## v1.2.1 — Store polish y backup manual

- estado: publicada

About en la app (TAR-57), changelog (TAR-58), la ficha como página de lectura (UX-020) y backup manual (TAR-59). Licencia privativa / source-available. Sin Auto Backup de Google.

## v1.2.2 — Sesiones y barra del lector

- estado: publicada

Tope de palabras por gesto (TAR-60), borrar un día de lectura (TAR-61) e índice en la barra inferior con el progreso a todo el ancho (UX-027). Código en `versionName` 1.2.2 / `versionCode` 7, listo para pruebas cerradas. Keystore, R8 y CI de tests ya están hechos.

## v1.2.3 — Pulido de uso

- estado: publicada

Guías de primer uso (TAR-64) e informe de problemas por correo (TAR-65). Pendiente, sin cambio de schema: no perder el comentario del registro (UX-026), colapsar Gestión en el menú (TAR-56) y filtro por autor (TAR-50). `versionName` sigue en 1.2.2. Lanzamiento a Producción.

## v1.3.0 — Biblioteca y estadísticas

- estado: en curso

Estadísticas (TAR-19 y TAR-20: el tiempo sale de `daily_reading` y un físico terminado cuenta por estado y fecha de fin), carpetas como archivadores (TAR-47), filtros y ordenación (TAR-49), portadas (TAR-62) y favoritos (TAR-54). El Home pasa a libro en curso arriba y favoritos como vitrina (TAR-66). Entran la barra inferior (TAR-69), el enlace de donación (TAR-68) y el pase de Classic Dark y Neon (TAR-63). El visual de biblioteca ya está cerrado, así que el contraste se revisa una sola vez, con las pantallas nuevas incluidas.

## v1.3.1 — Lector y lista TBR

- estado: prevista

Marcadores (TAR-51), tipografías del lector (TAR-52) y modo horizontal (TAR-53). También una lista TBR, distinta de pendientes (TAR-67).

## v1.4.0 — EPUB3

- estado: prevista

Compatibilidad básica y avanzada (TAR-31/32), con los fixes diferidos READER-008 y READER-009.

## v2.0.0 — Cloud

- estado: prevista

TAR-29: Supabase o Dropbox, local-first. El scaffold de sync de la 1.x se eliminó (ARCH-001). Se reimplementa cuando el producto local esté maduro.

## v3.0.0 — Motor nativo

- estado: prevista

TAR-30: visor EPUB propio, de EPUB a Compose. El módulo `:epub-engine` existe y no está enlazado a `:app`. Entra en el lector de producción cuando haya paridad con el WebView.
