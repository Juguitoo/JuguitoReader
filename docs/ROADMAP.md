# Roadmap — JuguitoReader

Visión por versión. Estado operativo de tareas: [BACKLOG.md](BACKLOG.md). Bugs abiertos: [KNOWN_ISSUES.md](KNOWN_ISSUES.md). Histórico cerrado: [archive/](archive/).

**Visión:** lector EPUB + gestor de lecturas (digitales y físicos). Backend/suscripción pospuesto hasta app local sólida.

---

## v1.2.x — Pulido pre-producción *(código 1.2.1 listo)*

Línea **v1.2.x** hasta producción en Play Store. Nube de pago (TAR-29 / v2) después; backup **manual** local como puente (TAR-59 hecho).

### Estrategia de publicación

1. **v1.2.0** — Audit + fixes del pase **cerrados** en [archive/v1.2.0.md](archive/v1.2.0.md) → **pruebas cerradas**.
2. **v1.2.1** — Pulido store + backup manual. Código listo (`versionName` 1.2.1 / `versionCode` 6). Ver [archive/v1.2.1.md](archive/v1.2.1.md).
3. **Producción** — Checklist hecho (TAR-59, privacy en Console, keystore/R8/CI). Publicar 1.2.1; tag `v1.2.1` al subir.

### v1.2.1 — Store polish + Backup manual *(hecho)*

About in-app **hecho** (TAR-57). Changelog / novedades **hecho** (TAR-58). Detail como página de lectura **hecho** (UX-020). Backup manual **hecho** (TAR-59). Ver [archive/v1.2.1.md](archive/v1.2.1.md).

Licencia de producto: privativa / source-available (portfolio); ya no GPLv3.

Sin Auto Backup de Google (ARCH-002 hecho en v1.2.0); sync de pago sigue en TAR-29.

## v1.3.0 — Biblioteca y estadísticas

Estadísticas (TAR-19/20), higiene de sesiones (TAR-60 **hecho** — tope de delta de palabras; TAR-61 borrar día), carpetas como archivadores (TAR-47), filtros/ordenación (TAR-49/50), look de portadas (TAR-62), marcadores, tipografías, modo horizontal, índice en barra inferior, favoritos, drawer (TAR-51–56, UX-027). Registry: no perder comentario y poder deseleccionar campos (UX-026).

**Diseño:** desacoplar stats del WebView (TAR-19/TAR-25) de cara a motor nativo. TAR-60 no revierte la posición ni tira la sentada: en `creditWordsRead` descarta deltas de scroll > ~1000 palabras a zoom 100% (escala con `textZoom`); el tiempo cuenta. TAR-61 borra la fila diaria (`book_id` + `date`), no una sentada. Modo estantería visual queda bajo TAR-54 (Home), no como epic aparte.

---

## v1.3.1 — Tema oscuro

Pase de Classic Dark + Neon (TAR-63) **después** del visual de biblioteca (TAR-62), para no retocar contraste dos veces.

---

## v1.4.0 — EPUB3

Compatibilidad básica/avanzada (TAR-31/32). Incluye fixes diferidos READER-008 / READER-009 (ver KNOWN_ISSUES).

---

## v2.0.0 — Cloud (pospuesto)

TAR-29 — Supabase/Dropbox, local-first. Scaffold sync de v1.x eliminado (ARCH-001); reimplementar cuando el producto local esté maduro.

---

## v3.0.0 — Motor nativo

TAR-30 — Visor EPUB propio (EPUB → Compose). Scaffold Gradle `:epub-engine` (librería; **no** enlazada a `:app`). El trabajo del motor va ahí; el merge al lector de producción es cuando haya paridad con WebView.

---

## Pre-publicación Play Store

- **Closed testing:** hecha (v1.2.0).
- **Producción:** lista. Privacy policy ya en Console; TAR-59 hecho. Queda publicar el AAB 1.2.1 y el tag.
- Keystore (REL-001 hecho); R8 (REL-002 hecho); CI (REL-003 hecho); política migraciones (DATA-006, [DATABASE.md](DATABASE.md)).

Detalle en [BACKLOG.md](BACKLOG.md).

---

## Versiones completadas

| Versión | Resumen | Archive |
|---------|---------|---------|
| v1.0.0 / v1.0.2 | Base app + hotfix scroll | [archive/v1.0.0.md](archive/v1.0.0.md) |
| v1.1.0 | UX, undo, gestor | [archive/v1.1.0.md](archive/v1.1.0.md) |
| v1.2.0 | Features 1.2 + estabilización audit | [archive/v1.2.0.md](archive/v1.2.0.md) |
| v1.2.1 | Store polish + backup manual | [archive/v1.2.1.md](archive/v1.2.1.md) |

---

## Mantenimiento

Editar al planificar versiones o cerrar fases. Ver [MAINTENANCE.md](MAINTENANCE.md).
