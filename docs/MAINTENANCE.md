# Mantenimiento de documentación — JuguitoReader

Esta documentación es **parte del proyecto**, no un snapshot. Debe actualizarse cuando cambie arquitectura, git, bugs o roadmap.

---

## Principio

> Si un dato en docs/ deja de ser cierto, **editar o eliminar** el dato — no acumular notas obsoletas.

---

## Mapa de fuentes

| Qué | Dónde |
|-----|-------|
| Trabajo activo (tabla + versión) | [BACKLOG.md](BACKLOG.md) |
| Bugs / riesgos abiertos | [KNOWN_ISSUES.md](KNOWN_ISSUES.md) |
| Visión por versión | [ROADMAP.md](ROADMAP.md) |
| Trabajo cerrado | [archive/](archive/) (`vX.Y.Z.md`) |

**Notion no se usa.** Todo vive en el repo.

---

## Qué actualizar y cuándo

| Evento | Archivos a tocar |
|--------|------------------|
| Bug encontrado | Fila en `KNOWN_ISSUES.md` + fila en `BACKLOG.md` (misma ID, columna Versión) |
| Bug resuelto | Quitar de ambos vivos; fila en `archive/vX.Y.Z.md`; commit con ID |
| Nueva feature / tarea | `BACKLOG.md` (+ `ROADMAP.md` si cambia el milestone) |
| Tarea completada | Quitar de `BACKLOG.md`; añadir a `archive/vX.Y.Z.md`; `ROADMAP.md` si cierra fase |
| Cierre de versión | Completar `archive/vX.Y.Z.md`; limpiar vivos; marcar fase en ROADMAP |
| Cambio arquitectura | `ARCHITECTURE.md` + rule `.cursor/rules/architecture.mdc` si aplica |
| Cambio Room / schema | `DATABASE.md` + `room-data.mdc` |
| Cambio ramas git | `GIT_WORKFLOW.md` — solo estado actual |
| Cambio CI / hooks | `.github/workflows/…`, `GIT_WORKFLOW.md` |
| Decisión producto | `ROADMAP.md`, `AGENTS.md` si aplica |

---

## Reglas por tipo de doc

### Vivos (cambian seguido)

- `BACKLOG.md` — solo pendientes; orden por versión próxima → lejana
- `KNOWN_ISSUES.md` — solo bugs/riesgos abiertos o diferidos (+ anti-patrones cortos)
- `GIT_WORKFLOW.md` — tabla de ramas actual

### Estables

- `ARCHITECTURE.md`, `DATABASE.md`, `README.md`

### Históricos

- `docs/archive/vX.Y.Z.md` — no borrar; corregir solo hechos erróneos

---

## Añadir / cerrar tareas

### Nueva tarea

1. ID: `TAR-xxx` (feat) o prefijos (`DATA-`, `READER-`, …).
2. Fila en `BACKLOG.md` con columna **Versión**.
3. Si es bug → también `KNOWN_ISSUES.md`.
4. Si es milestone nuevo → nota en `ROADMAP.md`.

### Cerrar tarea

1. Quitar fila del BACKLOG vivo (y de KNOWN_ISSUES si era bug).
2. Añadir fila al `archive/vX.Y.Z.md` de la versión donde se cerró.
3. Si era la última de una fase, actualizar `ROADMAP.md`.

---

## Checklist post-release

- [ ] Issues de la versión en `archive/vX.Y.Z.md`
- [ ] BACKLOG / KNOWN_ISSUES sin filas de esa versión
- [ ] ROADMAP: fase completada + enlace archive
- [ ] `archive/README.md` lista la versión
- [ ] GIT_WORKFLOW: ramas actualizadas
- [ ] Tag git (`vX.Y.Z`)

---

## Quién mantiene

- **Hugo:** producto, prioridades, cierre de versiones.
- **IA (WORKER/AGENT):** actualizar docs al implementar o cuando Hugo pida revisión.
- Al abrir sesión SUPERVISOR: leer `BACKLOG.md` + `KNOWN_ISSUES.md` (y archive solo si hace falta contexto histórico).
