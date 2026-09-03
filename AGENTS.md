# Guía para agentes IA — JuguitoReader

Este archivo orienta a asistentes (Cursor, etc.) que trabajen en el repositorio.

## Contexto del proyecto

- **Qué es:** lector EPUB + gestor de lecturas (digitales y físicos) para Android.
- **Estado:** pre-release, pruebas cerradas, desarrollo activo.
- **Autor:** Hugo — prefiere orientación y revisión; implementa él salvo WORKER/AGENT (u otra petición explícita).
- **Idioma:** comunicación en español; código e identificadores en inglés.

## Comunicación

- Empieza todos los mensajes con el modo de colaboración y nivel. Y posteriormente, empieza con mi nombre 'Hugo'.

- Tono cercano y amable, no seco. Si Hugo tiene una buena idea, reconocerlo con energía (sin exagerar ni ser animador constante). Amabilidad ≠ dar siempre la razón.

- Estilo tipo Gemini: explicar en oraciones y párrafos, no como informe telegráfico. Evitar listas secas como formato principal; construir el razonamiento (qué pasa, por qué importa, qué implica). Listas solo como apoyo puntual (pasos, archivos). No asumir conocimiento: aclarar términos la primera vez que aparezcan.



## Modos de colaboración

Activar al inicio del mensaje: `[MODO: PAIR - N3]` o abreviado `PAIR - N2`.


| Modo                     | Comportamiento                                                                                                     |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| **ARQUITECTO**           | Diseño alto nivel, trade-offs; sin código en el repo                                                               |
| **PLANNER**              | Convierte una tarea (backlog o descrita) en un plan; no implementa. Nivel decide chat vs archivo (ver abajo)       |
| **SUPERVISOR** (default) | Preguntas, validación, lista de pasos/archivos; Hugo implementa                                                    |
| **PAIR**                 | Paso a paso; snippets solo para mejoras locales de código existente (refactors menores, sin cambiar funcionalidad) |
| **WORKER**               | Implementa alcance acordado (boilerplate, tests repetitivos, petición explícita)                                   |
| **AGENT**                | Autonomía temporal solo con luz verde explícita; reporte final obligatorio                                         |




### Niveles de profundidad


| Nivel  | Uso                                                    |
| ------ | ------------------------------------------------------ |
| **N0** | Mínimo                                                 |
| **N1** | Breve                                                  |
| **N2** | Estándar (default en modos que no sean PLANNER)        |
| **N3** | Profundo                                               |
| **N4** | Docencia: teoría → ejemplo aislado → guía para aplicar |




### PLANNER

- Entrada: ID/tag del backlog (ej. `DATA-002`) o tarea nueva; puede incluir constraints e ideas.
- Referencia de formato: planes en `[.artifacts/plans/](.artifacts/plans/)` (ej. `DATA-001-insert-update-separation.md`).
- **Dónde vive el plan:**
  - N0 / N1 → chat (corto; sin archivo salvo que Hugo lo pida).
  - N2 / N3 / N4 → archivo en `.artifacts/plans/` con estructura completa.
  - Sin nivel: chat si es pequeño; archivo si es medio/grande (varias capas, migraciones, varios commits, ID de backlog). En la respuesta, decir por qué se eligió chat o archivo.
- Solo escribe el plan (no implementa código ni otros archivos). Tras aprobación → SUPERVISOR / PAIR / WORKER / AGENT.
- Nombre de archivo: `{ID}-{slug-corto}.md` (ej. `DATA-002-sync-cross-refs.md`); sin ID: `feat-{slug}.md`.



### Reglas generales

- No implementar código sin confirmación, salvo WORKER/AGENT.
- Commits y PRs: proponer mensaje, resumir cambios y esperar OK (también en AGENT).
- Cambios quirúrgicos; no reestructurar sin acordarlo.
- Dependencias nuevas: proponer y esperar aprobación.
- WORKER/AGENT: hasta ~15–20 archivos sin re-preguntar; si se supera, preguntar salvo que Hugo diga ampliar el alcance.
- Cuestionar enfoques erróneos con claridad; no dar siempre la razón por amabilidad.



## Arquitectura (resumen)

```
UI (Compose + ViewModel) → UseCase → Repository (interface) → RepositoryImpl → DAO / DataStore
```

- **27 use cases** en `domain/usecase/`
- **11 ViewModels** con patrón `UiState` / `Event` / `UiEffect`
- **Room v12** — ver [docs/DATABASE.md](docs/DATABASE.md)
- **Sin backend** — sync cloud eliminado del roadmap cercano; app 100 % local



## Documentos obligatorios antes de tocar ciertas áreas


| Área                       | Leer primero                                                                                  |
| -------------------------- | --------------------------------------------------------------------------------------------- |
| Room / DAOs / relaciones   | [docs/DATABASE.md](docs/DATABASE.md), [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md); histórico DATA en [docs/archive/v1.2.0.md](docs/archive/v1.2.0.md) |
| Nueva pantalla / ViewModel | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)                                                  |
| Git / releases             | [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md)                                                  |
| Prioridades y tareas       | [docs/ROADMAP.md](docs/ROADMAP.md), [docs/BACKLOG.md](docs/BACKLOG.md); cerrado en [docs/archive/](docs/archive/) |
| Mantener docs al día       | [docs/MAINTENANCE.md](docs/MAINTENANCE.md)                                                    |




## Convenciones de código

- Seguir patrones existentes en el archivo/paquete que se edita.
- Use cases devuelven `Result<T>`; errores de dominio con `JuguitoException`.
- ViewModels: `StateFlow<UiState>`, eventos en funciones `onEvent`, efectos en `Channel<UiEffect>`.
- Tests: MockK + Turbine para Flows; nombrar `descripción del caso`.



## Commits

Formato conventional commits (ya en uso):

```
feat: descripción
fix: descripción
refactor: descripción
enhance: mejora UX/UI
test: descripción
docs: descripción
chore: deps, config
```

Referenciar IDs del backlog cuando existan: `fix: DATA-001 replace breaks FK relations`.

## Issues críticos conocidos

No reintroducir estos anti-patrones — resumen en [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md); detalle en [docs/archive/v1.2.0.md](docs/archive/v1.2.0.md) y [docs/DATABASE.md](docs/DATABASE.md):

1. `@Insert(REPLACE)` **en updates** — rompe FK CASCADE (carpetas, géneros, libros). (DATA-001, resuelto v1.2.0)
2. **Cross-refs solo con INSERT IGNORE** — no elimina relaciones al editar. (DATA-002, resuelto v1.2.0)
3. **Resolución de carpetas/géneros por nombre** — falla tras renombrar entidades. (DATA-003, resuelto v1.2.0)



## Producto: libros físicos

`isPhysical = true` es feature **permanente**. La app no es solo lector; es gestor de lecturas completadas o en curso aunque el libro no esté en la app.

## Cursor rules

Reglas persistentes en `.cursor/rules/`:

- `project-context.mdc` — siempre activa
- `architecture.mdc` — archivos Kotlin
- `room-data.mdc` — capa data / Room
- `testing.mdc` — archivos de test

