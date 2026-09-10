# Roadmap — JuguitoReader

Visión por versión. Estado operativo de tareas: [BACKLOG.md](BACKLOG.md). Bugs abiertos: [KNOWN_ISSUES.md](KNOWN_ISSUES.md). Histórico cerrado: [archive/](archive/).

**Visión:** lector EPUB + gestor de lecturas (digitales y físicos). Backend/suscripción pospuesto hasta app local sólida.

---

## v1.2.x — Pulido pre-producción *(en curso)*

Línea **v1.2.x** hasta producción en Play Store. Nube de pago (TAR-29 / v2) después; backup **manual** local como puente.

### Estrategia de publicación

1. **v1.2.0** — Audit + fixes del pase **cerrados** en [archive/v1.2.0.md](archive/v1.2.0.md) → **pruebas cerradas**.
2. **v1.2.1 / v1.2.2** — Pulido store (TAR-57 y TAR-58 hechos) + backup manual (TAR-59). Bugs sin versión cuando toque.
3. **Producción** — Solo cuando esté TAR-59 y el cierre 1.2.x.

### v1.2.1 — Store polish

About in-app **hecho** (TAR-57). Changelog / novedades **hecho** (TAR-58). Detail como página de lectura **hecho** (UX-020). Ver [archive/v1.2.1.md](archive/v1.2.1.md).

Licencia de producto: privativa / source-available (portfolio); ya no GPLv3.

### v1.2.2 — Backup manual *(gate producción)*

Export/import local (TAR-59). Sin Auto Backup de Google (ARCH-002 hecho en v1.2.0); sync de pago sigue en TAR-29.

---

## v1.3.0 — Biblioteca y estadísticas

Estadísticas (TAR-19/20), carpetas como archivadores (TAR-47), filtros/ordenación (TAR-49/50), marcadores, tipografías, modo horizontal, favoritos, drawer (TAR-51–56).

**Diseño:** desacoplar stats del WebView (TAR-19/TAR-25) de cara a motor nativo.

---

## v1.4.0 — EPUB3

Compatibilidad básica/avanzada (TAR-31/32). Incluye fixes diferidos READER-008 / READER-009 (ver KNOWN_ISSUES).

---

## v2.0.0 — Cloud (pospuesto)

TAR-29 — Supabase/Dropbox, local-first. Scaffold sync de v1.x eliminado (ARCH-001); reimplementar cuando el producto local esté maduro.

---

## v3.0.0 — Motor nativo

TAR-30 — Visor EPUB propio (EPUB → Compose).

---

## Pre-publicación Play Store

- **Closed testing:** tras v1.2.0; seguir con 1.2.1 / 1.2.2.
- **Producción:** tras TAR-59 + checklist release.
- Keystore (REL-001 hecho); R8 (REL-002 hecho); CI (REL-003 hecho); política migraciones (DATA-006); privacy policy.

Detalle en [BACKLOG.md](BACKLOG.md).

---

## Versiones completadas

| Versión | Resumen | Archive |
|---------|---------|---------|
| v1.0.0 / v1.0.2 | Base app + hotfix scroll | [archive/v1.0.0.md](archive/v1.0.0.md) |
| v1.1.0 | UX, undo, gestor | [archive/v1.1.0.md](archive/v1.1.0.md) |
| v1.2.0 | Features 1.2 + estabilización audit | [archive/v1.2.0.md](archive/v1.2.0.md) |

---

## Mantenimiento

Editar al planificar versiones o cerrar fases. Ver [MAINTENANCE.md](MAINTENANCE.md).
