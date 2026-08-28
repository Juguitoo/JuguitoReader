# Mantenimiento de documentación — JuguitoReader

Esta documentación es **parte del proyecto**, no un snapshot. Debe actualizarse cuando cambie arquitectura, git, bugs o roadmap.

---

## Principio

> Si un dato en docs/ deja de ser cierto, **editar o eliminar** el dato — no acumular notas obsoletas.

Ejemplo: la nota sobre renombrar `v1.2.0` desaparece cuando la rama se reorganice, no queda para siempre en GIT_WORKFLOW.

---

## Qué actualizar y cuándo

| Evento | Archivos a tocar |
|--------|------------------|
| Bug encontrado | `KNOWN_ISSUES.md` + checkbox en `BACKLOG.md` |
| Bug resuelto | `KNOWN_ISSUES.md` → Resuelto; `BACKLOG.md` `[x]`; commit con ID |
| Nueva feature / tarea | `BACKLOG.md` + `ROADMAP.md` si cambia versión |
| Tarea completada | `BACKLOG.md` → Done; quitar de pendientes; actualizar `ROADMAP.md` si aplica |
| Cambio arquitectura | `ARCHITECTURE.md` + rule `.cursor/rules/architecture.mdc` si aplica |
| Cambio Room / schema | `DATABASE.md` + `room-data.mdc` |
| Cambio ramas git | `GIT_WORKFLOW.md` — **solo estado actual**, sin historial de ramas muertas |
| Nueva versión release | `ROADMAP.md`, `GIT_WORKFLOW.md` (si aplica), bump en README si relevante |
| Decisión producto (p. ej. sync) | `ROADMAP.md`, `AGENTS.md`, `KNOWN_ISSUES.md` |

---

## Reglas por tipo de doc

### Documentos **vivos** (cambian seguido)

- `BACKLOG.md` — checkboxes, prioridades
- `KNOWN_ISSUES.md` — estados Abierto/Resuelto
- `GIT_WORKFLOW.md` — sección "Estado actual del repo" (tabla de ramas)

**Obsoleto → borrar o actualizar.** No dejar avisos temporales más de una versión.

### Documentos **estables** (cambian poco)

- `ARCHITECTURE.md` — actualizar solo si cambia patrón o capa
- `DATABASE.md` — actualizar en migraciones
- `README.md` — stack, quick start

### Documentos **históricos** (no borrar, marcar resuelto)

- Entradas en `KNOWN_ISSUES.md` sección **Resuelto**
- Tareas en `BACKLOG.md` → Done

---

## Git workflow doc — patrón recomendado

En `GIT_WORKFLOW.md`, usar:

```markdown
## Estado actual del repo

| Rama | Rol |
|------|-----|
| `main` | Estable |
| `dev` | Integración |
| `feature/xxx` | Trabajo activo |
```

Cuando `v1.2.0` se mergee y renombre: **sustituir la fila**, no añadir "ya no usar v1.2.0".

---

## Quién mantiene

- **Hugo:** decisiones producto, prioridades, cierre de versiones.
- **IA (WORKER/AGENT):** actualizar docs al implementar fixes o cuando Hugo pida revisión de docs.
- Al abrir sesión SUPERVISOR: leer `BACKLOG.md` + `KNOWN_ISSUES.md` antes de proponer trabajo.

---

## Gestión de tareas (fuente única: este repo)

**Notion ya no se usa.** Tareas, roadmap e issues viven solo en:

| Qué | Dónde |
|-----|-------|
| Prioridades y checkboxes | `BACKLOG.md` |
| Fases por versión | `ROADMAP.md` |
| Bugs y anti-patrones | `KNOWN_ISSUES.md` |

### Añadir una tarea nueva

1. Asignar ID: `TAR-xxx` (features) o reutilizar prefijos existentes (`DATA-`, `READER-`, etc.).
2. Entrada en `BACKLOG.md` en la sección de la versión correspondiente.
3. Si es milestone nuevo, añadir fila en `ROADMAP.md`.
4. Commits opcionales con ID: `feat: TAR-57 descripción`.

### Cerrar una tarea

1. Marcar `[x]` en `BACKLOG.md`.
2. Mover a sección **Done** (no borrar).
3. Si era la última de una fase, marcar fase completada en `ROADMAP.md`.

---

## Checklist post-release

- [ ] Issues de la versión → Resuelto en KNOWN_ISSUES
- [ ] BACKLOG Done actualizado
- [ ] ROADMAP: fase marcada completada
- [ ] GIT_WORKFLOW: tabla ramas actualizada
- [ ] Tag git creado (`vX.Y.Z`)
