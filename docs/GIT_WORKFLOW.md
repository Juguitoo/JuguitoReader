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
- `--no-verify` en push salvo emergencia (salta tests locales del hook)

## CI (GitHub Actions)

Workflow: [.github/workflows/test.yml](../.github/workflows/test.yml)

| Trigger | Qué ejecuta |
|---------|-------------|
| Cada `push` | `./gradlew test` |
| Cada `pull_request` | `./gradlew test` |

Ver resultados en GitHub → **Actions** → *Test*. Badge en [README.md](../README.md).

Solo unit tests. Los instrumentados (`connectedAndroidTest`) requieren emulador y no están en CI por ahora.

## Hook pre-push (local, opcional)

Complemento al CI: feedback antes de subir al remoto.

```powershell
# Desde la raíz del repo (una vez por clone)
.\scripts\install-git-hooks.ps1
```

Copia `scripts/hooks/pre-push` → `.git/hooks/pre-push`. Antes de cada `git push` ejecuta `./gradlew test`.

| Comando | Efecto |
|---------|--------|
| Push normal | Corre tests; aborta si fallan |
| `git push --no-verify` | Salta el hook (usar solo en emergencia) |

Los hooks **no se versionan** en `.git/hooks/`; hay que reinstalar tras un clone nuevo.

---

| Rama | Rol |
|------|-----|
| `main` | Estable |
| `dev` | Integración |
| `v1.2.0` | Feature branch v1.2 — **pendiente merge/rename** (GIT-001) |

Ver [ROADMAP.md](ROADMAP.md) · Mantener esta tabla al día: [MAINTENANCE.md](MAINTENANCE.md)
