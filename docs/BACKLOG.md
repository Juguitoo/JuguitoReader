# Backlog — JuguitoReader

Fuente única de **trabajo activo**. Histórico cerrado en [archive/](archive/). Visión por versión en [ROADMAP.md](ROADMAP.md).

**Leyenda:** `[ ]` pendiente · `[~]` en progreso · `[x]` hecho (al cerrar → mover a archive y quitar de aquí)

Orden: versión más próxima → más lejana. Sin versión (`—`) = aún no asignada.

| Versión | ID | Tarea | Tipo | Estado | Ref |
|---------|-----|-------|------|--------|-----|
| — | READER-016 | No descartar sesión/WPM en pausas ≤60s | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-011 | Drawer: `launchSingleTop` / `popUpTo` | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-012 | Poder limpiar fechas inicio/fin | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-013 | DatePicker: medianoche UTC del día local | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-020 | Cancel edit Detail no debe tirar drafts del tab Registry | Fix | `[ ]` | KNOWN_ISSUES |
| — | READER-019 | Capítulos no scrolleables: reportar 100 % | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-023 | Ocultar overscroll en primer/último capítulo | Fix | `[ ]` | KNOWN_ISSUES |
| — | UX-024 | Barra de controles con scroll intra-capítulo | Fix | `[ ]` | KNOWN_ISSUES |
| — | SEC-004 | Acotar `AndroidBridge` (no fiar WPM a JS del EPUB) | Fix | `[ ]` | KNOWN_ISSUES |
| — | READER-017 | Ignorar eventos JS del capítulo anterior | Fix | `[ ]` | KNOWN_ISSUES |
| v1.2.1 | TAR-57 | Pantalla About (licencia, creador, créditos p. ej. icono) | Feat | `[ ]` | ROADMAP |
| v1.2.1 | TAR-58 | Changelog / novedades in-app por versión | Feat | `[ ]` | ROADMAP |
| v1.2.2 | TAR-59 | Export / import manual de datos (backup local; gate producción Play) | Feat | `[ ]` | ROADMAP |
| Pre-release | DATA-006 | Migraciones DB 1→5 o política destructive en dev | Chore | `[ ]` | DATABASE |
| v1.3.0 | TAR-19 | Recopilación de estadísticas | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-20 | Sección estadísticas | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-47 | Reestructuración carpetas (archivadores) | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-49 | Controles biblioteca (filtros + ordenación) | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-50 | Filtro por autor | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-51 | Marcadores en libros | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-52 | Tipografías en lector | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-53 | Modo horizontal | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-54 | Sistema favoritos + estantería Home | Feat | `[ ]` | ROADMAP |
| v1.3.0 | TAR-56 | Colapsar sección gestión en drawer | Feat | `[ ]` | ROADMAP |
| v1.4.0 | TAR-31 | Compatibilidad básica EPUB3 | Feat | `[ ]` | ROADMAP |
| v1.4.0 | TAR-32 | Compatibilidad avanzada EPUB3 | Feat | `[ ]` | ROADMAP |
| v1.4.0 | READER-008 | TOC con `#fragment` falla | Fix | `[ ]` | KNOWN_ISSUES |
| v1.4.0 | READER-009 | EPUB3 nav document incompleto | Fix | `[ ]` | KNOWN_ISSUES |
| v2.0.0 | TAR-29 | Conexión Supabase/Dropbox (local-first) | Feat | `[ ]` | ROADMAP |
| v3.0.0 | TAR-30 | Motor visor EPUB propio | Feat | `[ ]` | ROADMAP |

## Commits con IDs

```
fix: DATA-001 use Update instead of Replace in FolderDAO
fix: READER-008 TOC fragment navigation
feat: TAR-49 library filter controls
docs: move closed v1.2.0 work to archive
```

## Archive

| Versión | Archivo |
|---------|---------|
| v1.0.0 / v1.0.2 | [archive/v1.0.0.md](archive/v1.0.0.md) |
| v1.1.0 | [archive/v1.1.0.md](archive/v1.1.0.md) |
| v1.2.0 | [archive/v1.2.0.md](archive/v1.2.0.md) |
