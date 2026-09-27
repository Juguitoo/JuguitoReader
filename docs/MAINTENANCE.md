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
| Línea de tiempo (lo que lee md-flow) | [VERSIONS.md](VERSIONS.md) |
| Resumen de lo cerrado por versión | [archive/](archive/) (`vX.Y.Z.md`) |
| Plan de implementación mientras se trabaja | `.artifacts/plans/` (no se sube a git) |

---

## Qué actualizar y cuándo

| Evento | Archivos a tocar |
|--------|------------------|
| Bug encontrado | Línea en `BACKLOG.md` + ficha `tasks/{ID}.md` con el problema. `KNOWN_ISSUES.md` no se toca |
| Bug resuelto | Quitar el checkbox del backlog; en la ficha, `estado: hecho` y Resolución; fila en `archive/vX.Y.Z.md`; commit con ID |
| Nueva feature / tarea | Línea en `BACKLOG.md`. Ficha solo si hay algo que contar. `ROADMAP.md` si cambia el milestone |
| Tarea en curso | Mover el checkbox completo debajo de `## En curso` |
| Tarea completada | Quitar el checkbox del backlog; ficha a `estado: hecho` si existe; fila en `archive/vX.Y.Z.md`; `ROADMAP.md` si cierra fase |
| Cierre de versión | Completar `archive/vX.Y.Z.md`; pasar la línea en `VERSIONS.md` a Publicadas; roadmap `estado: publicada`. No borrar las tareas abiertas de esa versión: cambiarles la versión o cerrarlas antes |
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

## Formato

md-flow lee estos archivos al pie de la letra. Si cambia el encabezado o una columna, deja de ver la tarea. Las reglas de abajo salen de cómo lee y escribe, no de una convención aparte.

### BACKLOG.md

Tres secciones, con ese título: `## En curso`, `## Pendiente`, `## Hecho`. Dentro de Pendiente, un grupo `### vX.Y.Z` por versión. Una tarea es un checkbox y sus subviñetas. El id va al principio de la línea. El orden de las líneas dentro del mismo grupo es el orden de la lista.

`## Hecho` puede existir vacío. Ahí no se deja ninguna tarea. Cerrar es quitar el checkbox entero y añadir la fila al archive. `[x]` no es un paso intermedio: si se deja escrito, md-flow la trata como hecha y la esconde de la lista abierta, pero no crea la fila del archive.

Al pasar a en curso se mueve el checkbox completo, con `version`, `tipo` y `ref`, justo debajo de `## En curso`. No lleva `### vX.Y.Z`. Ese grupo solo existe bajo `## Pendiente`. La versión sigue siendo la subviñeta `version:`.

md-flow no deja de leer después de las tres secciones. Ignora el texto, las tablas y los bloques de código que no sean un checkbox. `## Commits con IDs` y `## Archive` no son secciones de estado, así que un checkbox puesto debajo de ellas sigue contando como hecho, porque la sección activa sigue siendo `## Hecho`. No van tareas ahí.

```markdown
## En curso

- [ ] UX-026 Título corto
  - version: v1.2.3
  - tipo: fix
  - ref: tasks/UX-026.md

## Pendiente

### v1.2.3

- [ ] TAR-50 Título corto
  - version: v1.2.3
  - tipo: feat
```

### tasks/{ID}.md

La lista y la versión que se ven en la web salen de la línea del backlog, no del prólogo de la ficha. En el prólogo, md-flow solo escribe y actualiza `estado` (`pendiente`, `en curso` o `hecho`). `version` y `tipo` ahí se conservan si ya están, pero no alimentan la web. `severidad` no se lee.

Después, estos títulos: `## Problema`, `## Decisión`, `## Durante el desarrollo`, `## Resolución`. Se pueden añadir otras `##` al final. La pantalla solo edita esas cuatro. `## Decisión` no hace falta para leer el archivo: si falta, la sección sale vacía. Un párrafo entre el prólogo y `## Problema` no se muestra y no rompe el resto; se queda en el prólogo. La primera vez que se guarda la ficha desde la web, se reescriben las cuatro secciones y ese párrafo sigue delante, en el prólogo.

Al cerrar la tarea, el texto de Resolución pasa al comentario del archive.

### VERSIONS.md

Una línea bajo `## En curso`, `## Previstas` o `## Publicadas`:

```markdown
- v1.2.3 | Título | archive/v1.2.3.md
```

La ruta del archive es relativa a `docs/`. Conviene una sola versión en curso.

Una línea sin ruta es válida mientras no se cierre ninguna tarea de esa versión. Para cerrar una, la línea tiene que llevar `archive/vX.Y.Z.md`. Si la ruta está y el archivo no existe, md-flow lo crea en ese momento, vacío, con la tabla estándar. Si la versión se crea desde la web, la ruta y el archivo vacío nacen a la vez. A mano, el archivo puede esperar hasta el primer cierre. La ruta, no.

### ROADMAP.md

La visión es una sola línea, en la misma línea. El encabezado de cada versión lleva raya `—`. `estado` es `prevista`, `en curso` o `publicada`.

```markdown
**Visión:** El objetivo, en una frase.

## v1.2.3 — Título

- estado: en curso

El párrafo es el resumen al pulsar el punto.
```

### archive/vX.Y.Z.md

Todas las tablas de tareas usan las mismas columnas. Los hashes van solo en Commits, entre comillas invertidas y separados por espacio. El comentario es prosa. Si había severidad, va al principio del comentario, no en otra columna.

`tipo` no tiene lista cerrada: se copia el texto tal cual. En el backlog va en minúscula (`feat`, `fix`, `enhance`), que es la forma de las tareas nuevas. Al cerrar desde la web, esa misma cadena pasa a la celda Tipo. Las mayúsculas (`Feat`, `Fix`, `Chore`) son filas viejas. Una celda vacía también es válida en esas filas. No reescribas las viejas solo para unificarlas, y no inventes una grafía distinta en el archive.

```markdown
| ID | Tarea | Tipo | Commits | Comentario / resolución |
|----|-------|------|---------|-------------------------|
| TAR-1 | Título corto | feat | `ca39572` | Qué se hizo, en una frase. |
```

Publicar una versión mueve su línea a `## Publicadas` y pone el roadmap en `estado: publicada`. No borra las tareas abiertas que sigan con esa versión.

## Añadir / cerrar tareas

### Nueva tarea

1. ID: `TAR-xxx` (feat) o prefijos (`DATA-`, `READER-`, `UX-`, …).
2. Línea en `BACKLOG.md`, en la versión que toque, con `version`, `tipo` y `ref`. `tipo` en minúscula.
3. Si hace falta contar el problema, la decisión o cómo se resolvió: `tasks/{ID}.md`. Un bug lo necesita. Una feature, solo si hay algo que no cabe en la línea.
4. Si es milestone nuevo → nota en `ROADMAP.md`.
5. El checklist de implementación va a `.artifacts/plans/`, que no se sube. En la ficha pública cabe el síntoma, como antes en `KNOWN_ISSUES`. No cabe el detalle de cómo explotar un agujero que siga abierto.

### Pasar a en curso

Mover el checkbox completo (`version`, `tipo`, `ref`) justo debajo de `## En curso`. Sin grupo `### vX.Y.Z`.

### Cerrar tarea

1. Quitar el checkbox entero del backlog. No marcarlo `[x]`.
2. Si hay ficha, `estado: hecho` y sección Resolución.
3. La línea de esa versión en `VERSIONS.md` tiene que llevar `archive/vX.Y.Z.md`. Si el archivo aún no existe, se puede crear a mano o dejar que md-flow lo cree vacío con la tabla estándar.
4. Añadir una fila a `archive/vX.Y.Z.md`, en la tabla con estas columnas y ninguna otra: `ID`, `Tarea`, `Tipo`, `Commits`, `Comentario / resolución`. `Tipo` es la misma cadena del backlog. Los hashes van solo en Commits, separados por espacio. El comentario es el texto, sin hashes mezclados. Si la tarea venía de una tabla de issues, la severidad se escribe al principio del comentario.
5. Si era la última de una fase, actualizar `ROADMAP.md`.

---

## Checklist post-release

- [ ] Issues de la versión en `archive/vX.Y.Z.md`, con la ficha en `estado: hecho`
- [ ] Ninguna tarea abierta sigue en esa versión. Si queda alguna, cámbiale la versión o ciérrala; no la borres al publicar
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
