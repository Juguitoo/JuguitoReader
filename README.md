# JuguitoReader

Lector EPUB y gestor de lecturas personal para Android. Importa libros digitales, léelos en la app y registra también lecturas hechas fuera de ella (libros físicos): fechas, nota, comentario, estado y estadísticas.

> **Estado:** pre-release — desarrollo activo, pruebas cerradas. No publicada en Play Store.

## Funcionalidades

- **Biblioteca digital:** importar EPUBs, organizar por carpetas y géneros, filtrar y ordenar.
- **Lector integrado:** progreso por capítulo, temas, zoom, estadísticas de sesión (tiempo, WPM).
- **Gestor de lecturas:** libros físicos y digitales con estado, fechas, valoración y comentarios.
- **Registro:** vista tipo hoja de cálculo para editar metadatos rápidamente.
- **Estadísticas:** progreso diario y sesiones de lectura por libro.

## Stack

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin 2.4, JVM 11 |
| UI | Jetpack Compose, Material 3 |
| Arquitectura | Clean Architecture + MVVM |
| DI | Dagger Hilt |
| Persistencia | Room 2.8, DataStore Preferences |
| Async | Coroutines + Flow |
| Tests | JUnit 4, MockK, Turbine, Truth |

Ver [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) para detalle de capas y patrones.

## Requisitos

- Android Studio (AGP 9.3+)
- JDK 11
- minSdk 26 · targetSdk 37

## Quick start

```bash
git clone <repo-url>
cd JuguitoReader
# Abrir en Android Studio y sincronizar Gradle
```

Ejecutar tests unitarios:

```bash
./gradlew test
```

Tests instrumentados (requiere emulador o dispositivo):

```bash
./gradlew connectedAndroidTest
```

## Estructura del proyecto

```
app/src/main/java/com/juguito/juguitoreader/
├── ui/           # Pantallas Compose, ViewModels, UiState/Event/Effect
├── domain/       # Modelos, repositorios (interfaces), use cases
├── data/         # Room, mappers, implementaciones de repositorios
├── di/           # Módulos Hilt
├── common/       # Utilidades compartidas (p. ej. ActionUndoManager)
└── utils/        # EpubParser, FileUtils
```

Módulo único: `:app`.

## Documentación

| Documento | Contenido |
|-----------|-----------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Capas, flujos, decisiones de diseño |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema Room, migraciones, reglas de integridad |
| [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) | Errores graves conocidos y cómo evitarlos |
| [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md) | Ramas, commits, releases |
| [docs/MAINTENANCE.md](docs/MAINTENANCE.md) | Cuándo y cómo actualizar la documentación |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Fases y versiones |
| [docs/BACKLOG.md](docs/BACKLOG.md) | Tareas priorizadas |
| [AGENTS.md](AGENTS.md) | Guía para asistentes IA en este repo |

## Licencia

GNU General Public License v3.0 — ver [LICENSE](LICENSE).
