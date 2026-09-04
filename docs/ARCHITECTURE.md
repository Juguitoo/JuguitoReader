# Arquitectura — JuguitoReader

## Visión general

JuguitoReader sigue **Clean Architecture pragmática** con **MVVM** en la capa de presentación. Es una app **local-first**: todos los datos viven en Room y DataStore; no hay backend en el roadmap cercano.

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
| `ui/book` | Detalle y alta de libros |
| `ui/folder` | Crear/editar carpetas |
| `ui/genre` | Diálogo de géneros |
| `ui/management` | Gestión de carpetas y géneros |
| `ui/settings` | Ajustes de app y lector |
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
- **`usecase/`** — Un caso de uso por operación de negocio (~27). Validan y orquestan repositorios.
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

### Borrado con undo

```
HomeViewModel → DeleteBookUseCase (BD inmediato)
  → pendingUndoBookId (StateFlow) → Snackbar en LaunchedEffect
  → nuevo delete: confirmPending del anterior (ficheros) + sustituye pending
  → confirm: borra archivos (cover, EPUB); undo: RestoreDeletedBookUseCase
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

## Decisiones de diseño

### WebView como lector EPUB

**Pros:** renderiza HTML/CSS de EPUBs sin dependencia externa pesada; control total del JS bridge (scroll, capítulos, WPM).

**Contras:** rendimiento y batería vs. renderer nativo; accesibilidad limitada. Contenido EPUB se sirve con `WebViewAssetLoader` (origen HTTPS sintético, sin `file://` ni `allowFileAccess`). Residual: JS + `JavascriptInterface` (`AndroidBridge`); el proceso de render no se gestiona aún (READER-012).

**Alternativas descartadas por ahora:** Readium, FolioReader.

### Filtros en cliente (`BookCriteria`)

`List<Book>.applyCriteria()` filtra y ordena en memoria. Aceptable para biblioteca personal (< miles de libros). Si crece, mover filtros a queries SQL.

### Domain enums en entities Room

`BookStatus` se usa directamente en entities. Pragmático en monolito; complicaría modularización Gradle futura.

### Modelos domain como `class` + `copy()` manual

`Book`, `Folder`, `Genre` no son `data class`. Histórico del proyecto; candidato a refactor cuando se toque integridad de datos.

## Testing

| Capa | Cobertura |
|------|-----------|
| Use cases | Alta (~27 tests) |
| Repositories | 6/6 impls |
| ViewModels | 10/11 |
| DAOs | Instrumentados |
| Compose screens | Tests con estado fake (sin Hilt E2E) |

Stack: MockK, Turbine, Truth, Coroutines Test.

## Extensiones futuras (sin implementar)

- Backend / sync cloud — pospuesto hasta app sólida en local.
- CI en GitHub Actions.
- Type-safe navigation.
- Modularización Gradle (`:core`, `:feature-*`).
