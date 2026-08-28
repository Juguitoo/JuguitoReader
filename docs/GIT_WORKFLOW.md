# Git workflow — JuguitoReader

## Ramas

```
main                 → releases estables (tracks de Play Console)
dev                  → integración continua
feature/*            → trabajo diario (feat, fix, enhance por versión)
release/vX.Y         → preparación de versión (QA, version bump)
hotfix/*             → correcciones urgentes desde main
```

### Convención de nombres

| Prefijo | Uso | Ejemplo |
|---------|-----|---------|
| `feature/` | Funcionalidad o bloque de versión en desarrollo | `feature/v1.2-sessions` |
| `fix/` | Corrección aislada | `fix/data-replace-fk-relations` |
| `release/` | Estabilización pre-release (QA) | `release/v1.2.0` |
| `hotfix/` | Fix urgente en producción | `hotfix/crash-on-import` |

### Transición v1.2.0 → gitflow

La rama `v1.2.0` es una **feature branch** (trabajo de v1.2, no release finalizada). Plan:

1. Cerrar estabilización (issues v1.2.0 en [BACKLOG.md](BACKLOG.md)).
2. Merge a `dev`.
3. Renombrar o eliminar `v1.2.0`.
4. Crear `release/v1.2.0` **solo** para QA final antes de `main`.

> Tras completar el paso 3, **actualizar la tabla "Estado actual"** abajo y eliminar referencias a `v1.2.0` en este doc. Ver [MAINTENANCE.md](MAINTENANCE.md).

## Flujo día a día

```
1. git checkout dev && git pull
2. git checkout -b feature/mi-cambio
3. ... commits ...
4. Merge / PR hacia dev
5. Cuando dev estable → release/vX.Y → QA → merge a main + tag
```

Como desarrollador solo, el PR es opcional pero recomendable como checkpoint.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/):

| Prefijo | Cuándo |
|---------|--------|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de bug |
| `refactor:` | Cambio interno sin alterar comportamiento |
| `enhance:` | Mejora UX/UI menor |
| `test:` | Solo tests |
| `docs:` | Documentación |
| `chore:` | Deps, config |

Incluir ID de backlog cuando aplique:

```
fix: DATA-001 use Update instead of Replace in FolderDAO
```

## Versiones

En `app/build.gradle.kts`:

- `versionName` — semver (`1.2.0`)
- `versionCode` — entero incremental para Play Console

### Release

1. `release/vX.Y` desde `dev`
2. Bump versiones
3. QA
4. Merge a `main` + tag `vX.Y.Z`
5. Merge `main` → `dev` si hubo hotfixes

## Tags

Formato: `v{versionName}` — p. ej. `v1.2.0`

## Qué no hacer

- Force push a `main`
- Commits directos a `main` (salvo hotfix documentado)
- `--no-verify` salvo petición explícita

## Estado actual del repo

| Rama | Rol |
|------|-----|
| `main` | Estable |
| `dev` | Integración |
| `v1.2.0` | Feature branch v1.2 — **pendiente merge/rename** (GIT-001) |

Ver [ROADMAP.md](ROADMAP.md) · Mantener esta tabla al día: [MAINTENANCE.md](MAINTENANCE.md)
