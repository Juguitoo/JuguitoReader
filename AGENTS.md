# Guía para agentes IA — JuguitoReader

Este archivo orienta a asistentes (Cursor, etc.) que trabajen en el repositorio.

## Contexto del proyecto

- **Qué es:** lector EPUB + gestor de lecturas (digitales y físicos) para Android.
- **Estado:** pre-release, pruebas cerradas, desarrollo activo.
- **Autor:** Hugo — prefiere orientación y revisión; implementa él salvo modo WORKER/AGENT explícito.
- **Idioma:** comunicación en español; código e identificadores en inglés.

## Modos de colaboración

Indicar al inicio del mensaje:

| Modo | Comportamiento |
|------|----------------|
| **SUPERVISOR** | Análisis, plan, pasos; Hugo implementa |
| **PAIR** | Paso a paso; snippets locales solo |
| **WORKER** | Implementa alcance acordado |
| **AGENT** | Autonomía temporal con luz verde explícita |

**Reglas generales:**
- No implementar cambios de código sin confirmación, salvo WORKER/AGENT.
- Commits y PRs: proponer mensaje y esperar OK.
- Cambios quirúrgicos; no reestructurar sin acordarlo.
- Dependencias nuevas: proponer y esperar aprobación.

## Arquitectura (resumen)

```
UI (Compose + ViewModel) → UseCase → Repository (interface) → RepositoryImpl → DAO / DataStore
```

- **27 use cases** en `domain/usecase/`
- **11 ViewModels** con patrón `UiState` / `Event` / `UiEffect`
- **Room v10** — ver [docs/DATABASE.md](docs/DATABASE.md)
- **Sin backend** — sync cloud eliminado del roadmap cercano; app 100 % local

## Documentos obligatorios antes de tocar ciertas áreas

| Área | Leer primero |
|------|--------------|
| Room / DAOs / relaciones | [docs/DATABASE.md](docs/DATABASE.md), [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) |
| Nueva pantalla / ViewModel | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| Git / releases | [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md) |
| Prioridades y tareas | [docs/ROADMAP.md](docs/ROADMAP.md), [docs/BACKLOG.md](docs/BACKLOG.md) — fuente única en repo |
| Mantener docs al día | [docs/MAINTENANCE.md](docs/MAINTENANCE.md) |

## Convenciones de código

- Seguir patrones existentes en el archivo/paquete que se edita.
- Use cases devuelven `Result<T>`; errores de dominio con `JuguitoException`.
- ViewModels: `StateFlow<UiState>`, eventos en funciones `onEvent`, efectos en `Channel<UiEffect>`.
- Tests: MockK + Turbine para Flows; nombrar `` `descripción del caso` ``.

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

No reintroducir estos anti-patrones — detalle en [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md):

1. **`@Insert(REPLACE)` en updates** — rompe FK CASCADE (carpetas, géneros, libros). (Resueltas instancias anteriores DATA-001)
2. **Cross-refs solo con INSERT IGNORE** — no elimina relaciones al editar. (Resuelto instancias anteriores DATA-002)
3. **Resolución de carpetas/géneros por nombre** — falla tras renombrar entidades. (Resuelto instancias anteriores DATA-003)

## Producto: libros físicos

`isPhysical = true` es feature **permanente**. La app no es solo lector; es gestor de lecturas completadas o en curso aunque el libro no esté en la app.

## Cursor rules

Reglas persistentes en `.cursor/rules/`:

- `project-context.mdc` — siempre activa
- `architecture.mdc` — archivos Kotlin
- `room-data.mdc` — capa data / Room
- `testing.mdc` — archivos de test
