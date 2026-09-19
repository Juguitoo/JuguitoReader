# Arquitectura — JuguitoReader

## Visión general

JuguitoReader sigue **Clean Architecture pragmática** con **MVVM** en la capa de presentación. Es una app **local-first**: todos los datos viven en Room y DataStore; no hay backend en el roadmap cercano.

Gradle: `:app` es el producto. `:epub-engine` es librería Android del visor nativo (TAR-30 / v3.0); **no** está en las `dependencies` de `:app`. El lector publicado sigue siendo WebView.

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer                                               │
│  Compose Screens · ViewModels · UiState/Event/Effect    │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│  Domain Layer                                           │
│  Models · Repository interfaces · Use Cases · Enums     │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│  Data Layer                                             │
│  RepositoryImpl · DAOs · Entities · Mappers             │
└──────────────────────────┬──────────────────────────────┘
                           │
                    Room / DataStore / FileSystem
```

## Capas

### UI (`ui/`)

Organizada por feature:

| Paquete | Responsabilidad |
|---------|-----------------|
| `ui/home` | Dashboard, importación rápida, libros recientes |
| `ui/library` | Biblioteca con filtros y carpetas |
| `ui/registry` | Edición masiva de metadatos |
| `ui/reader` | Lector EPUB (WebView) |
| `ui/book` | Detalle (página de lectura + ficha) y alta de libros |
| `ui/folder` | Crear/editar carpetas |
| `ui/genre` | Diálogo de géneros |
| `ui/management` | Gestión de carpetas y géneros |
| `ui/settings` | Ajustes de app y lector |
| `ui/settings/backup` | Export/import local (ZIP); segundo ViewModel en Settings |
| `ui/about` | Acerca de (estática; sin ViewModel) |
| `ui/changelog` | Novedades de versiones |
| `ui/navigation` | NavHost, drawer, rutas |
| `ui/common` | Componentes, `UiText`, interfaces compartidas |

#### Patrón por pantalla

Cada feature expone:

```kotlin
// Estado sellado
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(...) : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}

// Acciones del usuario
sealed interface HomeEvent { ... }

// Efectos de un solo uso (snackbar, navegación)
// Channel<UiEffect> en el ViewModel
```

El composable recibe `uiState` y `onEvent`; observa `effect` con `ObserveAsEvents`.

### Domain (`domain/`)

- **`model/`** — Entidades de negocio: `Book`, `Folder`, `Genre`, `ReadingProgress`, `DailyReading`, `EpubContent`.
- **`repository/`** — Interfaces que define el dominio; sin dependencias Android.
- **`usecase/`** — Un caso de uso por operación de negocio (~29). Validan y orquestan repositorios.
- **`enums/`** — `BookStatus`, `Language`, etc.
- **`exception/`** — `JuguitoException` con `@StringRes`.

Los use cases devuelven `Result<Unit>` o `Result<T>`. La UI traduce errores con `asUiText()`.

### Data (`data/`)

- **`local/entity/`** — Entidades Room.
- **`local/dao/`** — Acceso a datos.
- **`mapper/`** — `Entity ↔ Domain`.
- **`repository/`** — Implementaciones de interfaces de dominio.

### DI (`di/`)

Hilt con dos módulos principales:

- `DatabaseModule` — Room, DAOs `@Singleton`.
- `RepositoryModule` — `@Binds` de interfaces a implementaciones.

ViewModels: `@HiltViewModel`. Use cases: `@Inject constructor` (sin módulo dedicado).

## Flujos principales

### Importar EPUB

```
HomeScreen → ImportBookFromUriUseCase
  → GetBookFromEpubUseCase (EpubParser: metadata)
  → AddBookUseCase (validación, carpetas/géneros, cross-refs)
  → BookRepository → BookDAO
```

El EPUB se copia a almacenamiento interno vía `FileUtils`.

### Leer libro

```
ReaderScreen → ReaderViewModel
  → GetBookByIdUseCase
  → ParseEpubUseCase (contenido + nav)
  → GetReadingProgressByIdUseCase
  → EpubWebView (HTML local + JS bridge)
  → UpdateReadingProgressUseCase / AddDailyReadingUseCase
```

El reader combina settings de DataStore (tema, zoom, brillo) con el estado del libro en un `combine`.

### Libro físico (sin EPUB)

```
AddBookScreen → AddBookUseCase(isPhysical = true, localFilePath = null)
```

Aparece en Registry y Library pero no en listas de lectura de Home (filtra `!isPhysical && localFilePath != null`). Si hay libros pero ninguno es digital con EPUB, Home sigue en `Success` (no `Empty`) y la UI explica que Inicio no los muestra (UX-018). La UI de Add/Detail oculta el picker de EPUB si `isPhysical`. Al guardar un físico se fuerza `localFilePath = null` y se borra el fichero previo (si lo había). Un digital **puede** no tener EPUB todavía; Library abre detalle en vez del lector (UX-003).

### Backup local (TAR-59)

```
Export
  SettingsScreen → CreateDocument (application/zip)
  → BackupViewModel → ExportBackupUseCase
  → snapshot Room + EPUB/covers + settings.json → ZIP SAF
  → snackbar OK

Import (sustituye; no merge)
  SettingsScreen → JuguitoDialog destructivo → OpenDocument
  → BackupViewModel → ImportBackupUseCase
  → validar manifest/dbVersion, relocate paths, swap DB/files
  → UiEffect.RestartApp → ProcessAppRestarter (kill proceso)
  → cold start (Hilt/Room/migraciones)
```

El archivo es un ZIP (`JuguitoReader-backup-YYYYMMDD.zip`) con `manifest.json`, `settings.json`, `database/juguito_db` y `files/{basename}`. Auto Backup de Google sigue apagado (`allowBackup=false`). El import **reinicia el proceso**; no se reabre Room en la misma sesión.

`SettingsViewModel` no orquesta backup: `BackupViewModel` convive en la misma pantalla.

### Borrado con undo

```
HomeViewModel → DeleteBookUseCase (BD inmediato)
  → pendingUndoBookId (StateFlow) → Snackbar en LaunchedEffect
  → nuevo delete: confirmPending del anterior (ficheros) + sustituye pending
  → confirm: borra archivos (cover, EPUB, cache/reader); undo: RestoreDeletedBookUseCase
```

El undo de delete es estado (ventana viva), no `UiEffect` one-shot. Ver `common/ActionUndoManager.kt`.

## Navegación

Centralizada en `ui/navigation/JuguitoApp.kt`:

- Navigation Compose con rutas string.
- Drawer modal para secciones principales.
- Argumentos vía `SavedStateHandle` en ViewModels (`bookId`, `folderId`).

| Ruta | Pantalla |
|------|----------|
| `home` | HomeScreen |
| `library` | LibraryScreen |
| `registry` | RegistryScreen |
| `management` | ManagementScreen |
| `add_book` | AddBookScreen |
| `book_detail/{bookId}` | BookDetailScreen |
| `reader/{bookId}` | ReaderScreen |
| `add_folder` / `edit_folder/{folderId}` | FolderScreen |
| `settings` | SettingsScreen |
| `about` | AboutScreen |
| `changelog` | ChangelogScreen (desde About; diálogo Novedades overlay en `JuguitoApp`) |

## Decisiones de diseño

### WebView como lector EPUB

**Pros:** renderiza HTML/CSS de EPUBs sin dependencia externa pesada; control total del JS bridge (scroll, capítulos, WPM).

**Contras:** rendimiento y batería vs. renderer nativo; accesibilidad limitada. Contenido EPUB se sirve con `WebViewAssetLoader` (origen HTTPS sintético, sin `file://` ni `allowFileAccess`). El JS del capítulo comparte origen con `JavascriptInterface` (`AndroidBridge`); el ViewModel acota palabras acreditadas (SEC-004). Aislar stats del WebView queda para TAR-19 / motor nativo (TAR-30). `onRenderProcessGone` recrea el WebView (READER-012).

**Alternativas descartadas por ahora:** Readium, FolioReader.

### Módulo `:epub-engine` (TAR-30)

Librería (`com.android.library`, namespace `com.juguito.epubengine`). Scaffold vacío a propósito: se puede desarrollar el renderer sin tocar el APK.

- `:app` no depende de este módulo hasta que el motor sustituya `EpubWebView` con paridad de lo ya publicado (posición, tema, stats, etc.).
- El módulo no importa `com.juguito.juguitoreader.*`.
- `EpubParser` / `ParseEpubUseCase` siguen en `:app` (v1.4 EPUB3 los va a cambiar). El motor consumirá ese contrato más adelante; no se duplica el parser aquí.
- Compose se añade al módulo cuando haya UI que pintar, no en el scaffold.
- CI: `./gradlew test` ya incluye `:epub-engine:test`.

No es el inicio de una modularización `:core` / `:feature-*`.

### Filtros en cliente (`BookCriteria`)

`List<Book>.applyCriteria()` filtra y ordena en memoria. Aceptable para biblioteca personal (< miles de libros). Si crece, mover filtros a queries SQL.

### Domain enums en entities Room

`BookStatus` se usa directamente en entities. Pragmático en monolito; complicaría modularización Gradle futura.

### Modelos domain como `class` + `copy()` manual

`Book`, `Folder`, `Genre` no son `data class`. Histórico del proyecto; candidato a refactor cuando se toque integridad de datos.

## Testing

| Capa | Cobertura |
|------|-----------|
| Use cases | Alta (~29 tests) |
| Repositories | 7/7 impls |
| ViewModels | 12/13 |
| DAOs | Instrumentados |
| Compose screens | Tests con estado fake (sin Hilt E2E) |

Stack: MockK, Turbine, Truth, Coroutines Test.

## Extensiones futuras (sin implementar)

- Backend / sync cloud — pospuesto hasta app sólida en local.
- Type-safe navigation.
- Modularización Gradle (`:core`, `:feature-*`). `:epub-engine` es solo el visor nativo, no ese split.
