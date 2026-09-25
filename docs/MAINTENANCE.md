# Mantenimiento de documentación — JuguitoReader

Esta documentación es **parte del proyecto**, no un snapshot. Debe actualizarse cuando cambie arquitectura, git, bugs o roadmap.

---

## Principio

> Si un dato en docs/ deja de ser cierto, **editar o eliminar** el dato — no acumular notas obsoletas.

---

## Mapa de fuentes

| Qué | Dónde |
|-----|-------|
| Trabajo abierto (lista + versión) | [BACKLOG.md](BACKLOG.md) |
| Detalle de una tarea (problema, notas, resolución) | [tasks/](tasks/) (`{ID}.md`) |
| Anti-patrones que no hay que reintroducir | [KNOWN_ISSUES.md](KNOWN_ISSUES.md) |
| Visión por versión | [ROADMAP.md](ROADMAP.md) |
| Resumen de lo cerrado por versión | [archive/](archive/) (`vX.Y.Z.md`) |
| Plan de implementación mientras se trabaja | `.artifacts/plans/` (no se sube a git) |

**Notion no se usa.** Todo vive en el repo.

---

## Qué actualizar y cuándo

| Evento | Archivos a tocar |
|--------|------------------|
| Bug encontrado | Línea en `BACKLOG.md` + ficha `tasks/{ID}.md` con el problema. `KNOWN_ISSUES.md` no se toca |
| Bug resuelto | Quitar la línea del backlog; en la ficha, `estado: hecho` y Resolución; fila en `archive/vX.Y.Z.md`; commit con ID |
| Nueva feature / tarea | Línea en `BACKLOG.md`. Ficha solo si hay algo que contar. `ROADMAP.md` si cambia el milestone |
| Tarea completada | Quitar la línea del backlog; ficha a `estado: hecho` si existe; fila en `archive/vX.Y.Z.md`; `ROADMAP.md` si cierra fase |
| Cierre de versión | Completar `archive/vX.Y.Z.md`; limpiar el backlog de esa versión; marcar fase en ROADMAP |
| Cambio arquitectura | `ARCHITECTURE.md` + rule `.cursor/rules/architecture.mdc` si aplica |
| Cambio Room / schema | `DATABASE.md` + `room-data.mdc` |
| Cambio ramas git | `GIT_WORKFLOW.md` — solo estado actual |
| Cambio CI / hooks | `.github/workflows/…`, `GIT_WORKFLOW.md` |
| Decisión producto | `ROADMAP.md`, `AGENTS.md` si aplica |

---

## Reglas por tipo de doc

### Vivos (cambian seguido)

- `BACKLOG.md` — solo lo abierto; orden por versión próxima → lejana
- `tasks/{ID}.md` — ficha viva de esa tarea. Escribirla como algo que puede leerse en el repo público
- `KNOWN_ISSUES.md` — solo anti-patrones
- `GIT_WORKFLOW.md` — tabla de ramas actual

### Estables

- `ARCHITECTURE.md`, `DATABASE.md`, `README.md`

### Históricos

- `docs/archive/vX.Y.Z.md` — no borrar; corregir solo hechos erróneos

---

## Añadir / cerrar tareas

### Nueva tarea

1. ID: `TAR-xxx` (feat) o prefijos (`DATA-`, `READER-`, `UX-`, …).
2. Línea en `BACKLOG.md`, en la versión que toque, con `version`, `tipo` y `ref`.
3. Si hace falta contar el problema, la decisión o cómo se resolvió: `tasks/{ID}.md`. Un bug lo necesita. Una feature, solo si hay algo que no cabe en la línea.
4. Si es milestone nuevo → nota en `ROADMAP.md`.
5. El checklist de implementación va a `.artifacts/plans/`, que no se sube. En la ficha pública cabe el síntoma, como antes en `KNOWN_ISSUES`. No cabe el detalle de cómo explotar un agujero que siga abierto.

### Cerrar tarea

1. Quitar la línea del backlog.
2. Si hay ficha, `estado: hecho` y sección Resolución.
3. Añadir fila al `archive/vX.Y.Z.md` de la versión donde se cerró.
4. Si era la última de una fase, actualizar `ROADMAP.md`.

---

## Checklist post-release

- [ ] Issues de la versión en `archive/vX.Y.Z.md`, con la ficha en `estado: hecho`
- [ ] BACKLOG sin líneas de esa versión
- [ ] ROADMAP: fase completada + enlace archive
- [ ] `archive/README.md` lista la versión
- [ ] GIT_WORKFLOW: ramas actualizadas
- [ ] Tag git (`vX.Y.Z`)
- [ ] Changelog in-app: `ChangelogUiCatalog` + `string-array` es/en de esa versión

---

## Quién mantiene

- **Hugo:** producto, prioridades, cierre de versiones.
- **IA (WORKER/AGENT):** actualizar docs al implementar o cuando Hugo pida revisión.
- Al abrir sesión SUPERVISOR: leer `BACKLOG.md` y, si la tarea es un bug, su ficha en `tasks/`. `KNOWN_ISSUES.md` solo para no reintroducir un anti-patrón. Archive si hace falta el histórico.
