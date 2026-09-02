# Errores y riesgos conocidos — JuguitoReader

Registro vivo de bugs, riesgos y anti-patrones. **Consultar antes de modificar Room, reader, importación de archivos o relaciones M:N.**

---

## Índice por severidad


| Severidad    | IDs                                                                                  |
| ------------ | ------------------------------------------------------------------------------------ |
| **Crítico**  |                                                                                      |
| **Alto**     |                                                                                      |
| **Medio**    | DATA-006, READER-011…014, FILE-005, UX-001, UX-002, PERF-001, ARCH-002     |
| **Diferido** | READER-008, READER-009 → v1.4.0 (TAR-31)                                     |
| **Mejora**   | ARCH-001, REL-001, REL-002, UX-003, I18N-001                                         |
| **Resuelto** | READER-010, READER-005, READER-006, READER-007, DATA-008, DATA-004, DATA-007, FILE-004, READER-004, SEC-003, SEC-002, SEC-001, FILE-003, FILE-001, FILE-002, DATA-003, READER-002, READER-001, DATA-001, READER-003, DATA-002 |


---



## Alto

*(Sin issues abiertos en esta severidad.)*

---



### DATA-006 · Migraciones 1→5 inexistentes

Solo 6→10. En dev aceptable (destructive OK). Antes de open testing: definir política.

---



### READER-005 · Brillo no se restaura al salir

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |

`ReaderControls` modifica `window.attributes.screenBrightness` sin restore al salir del lector.

**Fix:** `ReaderScreen` restaura `BRIGHTNESS_OVERRIDE_NONE` en `DisposableEffect.onDispose` junto con el cleanup de ventana.

---

### READER-006 · System bars ocultas permanentemente

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |

`ReaderScreen` ocultaba barras al leer; no las restauraba al salir.

**Fix:** `show(systemBars())` en el mismo `onDispose` de `ReaderScreen`.

---

### READER-007 · WebView no destruido

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |

Falta `stopLoading()`, quitar JS bridge, `destroy()` en `AndroidView.onRelease`.

**Fix:** `EpubWebView` implementa `onRelease` con `stopLoading()`, `removeJavascriptInterface` y `destroy()`.

**Relacionado:** READER-012 (proceso de render).

---



### READER-008 · TOC con #fragment

| Campo        | Valor                                      |
| ------------ | ------------------------------------------ |
| **Estado**   | **Diferido (v1.4.0)**                      |
| **Relacionado** | TAR-31, READER-009                      |

`chapter.xhtml#section2` no matchea bien con spine por nombre final → navegación TOC ambigua.

**Nota:** limitación conocida del lector EPUB2 actual. No bloquea v1.2.0; se abordará con compatibilidad EPUB3 / navegación del índice (v1.4.0).

---

### READER-009 · EPUB3 nav incompleto

| Campo        | Valor                                      |
| ------------ | ------------------------------------------ |
| **Estado**   | **Diferido (v1.4.0)**                      |
| **Relacionado** | TAR-31, TAR-32                          |

NCX parseado; falta soporte completo HTML Navigation Document (`properties="nav"`).

**Nota:** EPUB3 parcial — muchos libros EPUB3 siguen leyéndose vía NCX o índice autogenerado desde spine. Soporte nav completo previsto en v1.4.0 (TAR-31).

---



### READER-010 · Errores unzip silenciados


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |


`extractFullContent` tragaba excepciones de parse con `printStackTrace()`; fallos de unzip podían dejar extracción incompleta; `openInputStream` null en `ensureExtracted` dejaba caché vacía y bloqueaba reintentos.

**Fix:** refactor en `ensureExtracted`, `parseContainerOpfPath`, `parseOpf` y `parseNcx`; fail-fast en container/OPF/spine vacío; NCX opcional con fallback por spine; cleanup de caché si falla la extracción; `openInputStream` null → `IOException`.

**Nota:** FILE-005 sigue abierto — mismo patrón `openInputStream` null en `FileUtils` al copiar a internal storage, no en el lector.

**Test:** `EpubParserExtractTest` (androidTest), `EpubParserSecurityTest`, `ParseEpubUseCaseTest`.

---



### FILE-005 · openInputStream null devuelve path

`FileUtils`: si `openInputStream` es null, no copia pero retorna `absolutePath`.

---



### UX-001 · Loading global al importar

`HomeViewModel.importBook()` → Loading en toda la pantalla.

---



### UX-002 · Stuck en Loading tras error

AddBookViewModel, HomeViewModel, LibraryViewModel: no todos los paths de error resetean loading.

---



### PERF-001 · Escrituras excesivas en scroll

Cada update de progreso → Room + `.first()` en preferences. Throttle; guardar al pausar/cambiar capítulo.

---



### READER-011 · Conteo palabras duplica al retroceder

Scroll abajo → arriba → abajo puede recontar palabras ya leídas.

---



### READER-012 · WebView sin onRenderProcessGone

| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |

Lint en `EpubWebView`: el HTML corre en un proceso de render aparte. Si muere (OOM, crash de Chromium) y no hay `onRenderProcessGone`, Android mata la app.

**Fix:** implementar el callback, devolver `true`, **no** reutilizar ese `WebView` (quitar del árbol / recrear `AndroidView` o estado Error). Un `return true` vacío evita el crash pero deja el visor muerto.

**Relacionado:** READER-007 (ciclo de vida del WebView). Detectado al cerrar SEC-001; no forma parte de AssetLoader.

**Archivo:** `EpubWebView.kt`

---



### READER-013 · FOUC al cambiar de capítulo

| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |

Al pasar de capítulo se ve un instante el HTML del EPUB (sin tema/padding del lector) y luego el CSS inyectado en `onPageFinished`. Más visible tras SEC-001 (el origen sintético pinta el documento antes de `evaluateJavascript`).

**Fix (orientativo):** inyectar CSS en `onPageStarted`, u ocultar el WebView hasta `onPageFinished`; alinear `setBackgroundColor` con el tema al cambiar de capítulo.

**Archivo:** `EpubWebView.kt`

---

### READER-014 · Temporizador de sesión perdido durante Loading

| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |

`ReaderScreen` envía `OnStartReading` al recibir `Lifecycle.Event.ON_RESUME`, pero `ReaderViewModel.onEvent` descarta todos los eventos mientras `_internalState` no sea `Success`. Si el EPUB sigue parseándose, el temporizador no comienza y la sesión no se registra hasta que ocurre otro pause/resume.

**Fix orientativo:** conservar si el reader está resumed aunque siga cargando e iniciar el temporizador al publicar `Success`; hacer `OnStartReading` idempotente y detenerlo correctamente en `ON_PAUSE`.

**Archivo:** `ReaderViewModel.kt`, `ReaderScreen.kt`

---



### ARCH-002 · Auto Backup vs local-only

`allowBackup="true"`, rules vacías → Android puede backup de DB y filesDir.

---



## Mejoras (pre-release)



### ARCH-001 · Scaffold sync sin usar

Deps Supabase/Ktor, `SyncStatus`, `syncPending*()` TODO. **Eliminar en v1.2.0.**

---



### REL-001 · Release firmado con debug

`app/build.gradle.kts:29`

---



### REL-002 · Release sin R8

`isMinifyEnabled = false`

---



### UX-003 · Portrait lock

`AndroidManifest.xml` — coherente con TAR-53 (horizontal) en v1.3.

---



### I18N-001 · Strings hardcodeadas

`EpubParser.kt`, `FileUtils.kt` — mover a strings.xml.

---



## Resuelto

### DATA-007 · Undo delete no restaura daily_reading

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |

`daily_reading` tiene FK CASCADE a `books`. Al borrar desde Home, el historial se perdía y el undo solo reinsertaba el libro vía `AddBookUseCase`.

**Fix:** snapshot de `daily_reading` y `reading_progress` antes del delete (`DeletedBookSnapshot`), restore transaccional en `BookRepository.restoreDeletedBook` vía `RestoreDeletedBookUseCase`.

**Test:** `RestoreDeletedBookUseCaseTest`, `BookRepositoryImplTest.restoreDeletedBook`, `HomeViewModelTest` (snapshot + undo).

---

### DATA-008 · Operaciones libro + carpetas + géneros no atómicas

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `fe1e2ee`             |

`AddBookUseCase` / `UpdateBookUseCase`: insert/update del libro y sync de cross-refs en llamadas separadas. Fallo intermedio → BD inconsistente (libro sin carpetas/géneros o relaciones desincronizadas).

**Fix:** `insertBookWithCrossRefs` y `updateBookWithCrossRefs` en `BookRepository` (`withTransaction`). Los use cases resuelven IDs en dominio y delegan en una sola operación. `restoreDeletedBook` y `updateBookWithNewEpub` reutilizan los mismos métodos.

**Test:** `AddBookUseCaseTest`, `UpdateBookUseCaseTest`, `BookRepositoryImplTest`.

**Residual:** la resolución de carpetas/géneros (posibles inserts) sigue fuera de la transacción del libro; Room y filesystem no comparten transacción (limitación arquitectural).

---

### DATA-004 · reading_progress huérfano al borrar libro

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |

`reading_progress` no tenía FK a `books`. Al borrar un libro, el progreso quedaba huérfano en BD.

**Fix:** migración 10→11 con FK `ON DELETE CASCADE` en `reading_progress`; undo de Home incluye progreso en `DeletedBookSnapshot` (extensión de DATA-007).

**Test:** `BookDAOTest.deleteBookById_cascades_reading_progress`, `ReadingProgressMigrationTest`, tests unitarios de restore.

---

### READER-004 · Crash EPUB con menos capítulos

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `fbf09aa`, `44061a0`, `96b565d` |

Al reemplazar un EPUB, `reading_progress`, `daily_reading` y la extracción en caché seguían perteneciendo al archivo anterior. Un `lastChapterIndex` superior al nuevo `spine` provocaba `IndexOutOfBoundsException`.

**Fix:** operación Room transaccional para actualizar el libro y sus relaciones mientras elimina progreso y sesiones; cleanup posterior de `cache/reader/{bookId}` y del EPUB anterior. `ReaderViewModel` también reconcilia índice y total de capítulos para datos legacy. Home mantiene visibles los libros `READING` aunque todavía no exista progreso para el EPUB nuevo.

**Test:** cobertura unitaria de selección de la operación, cleanup, reconciliación y filtro de Home; test instrumentado de commit y rollback de la transacción Room.

**Residual:** Room y filesystem no comparten transacción (limitación arquitectural). Huérfanos previos a v1.2.0 no se barreron del disco.

---

### SEC-003 · Path traversal tras unzip

| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `9f9ef29`, `1fb4e4c`  |

Las rutas declaradas por `container.xml`, el manifest OPF y el NCX se resolvían sin comprobar que permanecieran dentro del directorio extraído.

**Fix:** resolución mediante `canonicalFile`; rechazo de rutas absolutas, traversal y colisiones de prefijo; validación de referencias del OPF/NCX sin perder query o fragment; propagación fail-closed con `SecurityException`.

**Test:** `EpubParserSecurityTest` cubre rutas directas, anidadas, normalización interna, escapes, rutas absolutas, siblings con prefijo común y referencias con query/fragment.

---

### SEC-002 · Posible zip bomb


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `11e1d4d`, `14078f0` |


`EpubParser.unzip` tenía Zip Slip parcial pero sin tope de bytes descomprimidos ni de entradas.

**Fix:** límites (512 MiB total / 128 MiB por entry / 10 000 entries; cover 20 MiB); contar bytes escritos vía `copyBounded`; abort + `deleteRecursively` del destino; `unzip(InputStream, …)` testeable; `ParseEpubUseCase` → `error_unzip`. Completado en READER-010: parse fail-fast y cleanup de caché en `ensureExtracted`.

**Test:** `EpubParserSecurityTest`, `ParseEpubUseCaseTest`, `EpubParserExtractTest`.

---



### SEC-001 · WebView inseguro para contenido EPUB


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `903dede`, `6822276` |


Carga `file://` + `allowFileAccess`. EPUB = HTML de terceros.

**Fix:** `WebViewAssetLoader` + `InternalStoragePathHandler` sobre `cacheDir/reader/{id}`; URL `https://appassets.androidplatform.net/epub/…`; `allowFileAccess` / `allowContentAccess` en false; `shouldOverrideUrlLoading` bloquea otros hosts.

**Residual:** `javaScriptEnabled` + `AndroidBridge` (necesario para scroll/WPM). Renderer: READER-012.

**Test:** `EpubChapterUrlTest`.

---



### FILE-003 · Parse EPUB puede bloquear UI


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `7f4cf25`, `1bbe4fb` |


`ParseEpubUseCase` y `GetBookFromEpubUseCase` ejecutaban unzip/parse/copy en el hilo Main → ANR o tirones al abrir lector o importar desde Home/Library.

**Fix:** `withContext(Dispatchers.IO)` en ambos use cases; `GetBookFromEpubUseCase` pasa a `suspend`. ViewModels de Add/Detail dejan de envolver redundante.

**Test:** `ParseEpubUseCaseTest`, `GetBookFromEpubUseCaseTest`, `ImportBookFromUriUseCaseTest`, `BookDetailViewModelTest`.

---



### FILE-004 · Archivos internos huérfanos


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `f575581`, `a302657`, `3bb1edf`, `092c7b1`, `e76f776` |


EPUB y cover se copiaban a `filesDir` en picker/import antes de confirmar en Room. Cancelar, sustituir o fallar el save dejaba ficheros huérfanos.

**Fix (commit on save):** draft con `content://` o staging en `cacheDir`; `promotePendingFiles` solo al guardar. AddBook y BookDetail hacen rollback de lo promocionado si falla Room; BookDetail borra assets sustituidos y `cache/reader/{id}` al cambiar EPUB (complementa READER-004). Import one-shot (`ImportBookFromUriUseCase`) copia y hace rollback si `addBook` falla. Helpers `deleteStagingAsset` / `deleteFileFromInternalStorage`.

**Test:** `FileUtilsTest`, `GetBookFromEpubUseCaseTest`, `ImportBookFromUriUseCaseTest`, `AddBookViewModelTest`, `BookDetailViewModelTest`.

**Residual:** huérfanos acumulados en instalaciones previas (sin barrido histórico); Room y filesystem no comparten transacción (limitación arquitectural).

---



### FILE-001 · Mezcla content:// y path filesystem


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `906b659`, `32e8d83`  |


Algunas pantallas guardan URI del picker; `EpubParser` usa `File(path)` → falla.

**Relacionado:** `GetBookFromEpubUseCase` fallback `uri.toString()` si copy falla.

---



### FILE-002 · Reemplazar EPUB guarda path incorrecto


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `8cf2a86`, `32e8d83`  |


`BookDetailViewModel`: `getBookFromEpubUseCase` copia a interno pero draft guarda `event.localFilePath` (URI) en vez de path interno.

**Archivo:** `BookDetailViewModel.kt` ~159-168

---

### DATA-003 · Relaciones resueltas por nombre


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `901ad24`, `d26e95c`  |


`getFolderByName` / `getGenreByName` en Add/UpdateBook. Tras renombrar entidad, libro en memoria con nombre viejo → duplicados o enlaces rotos.

**Fix:** usar `id` cuando `id != 0`.

---



### READER-002 · PDF aceptado pero no soportado


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `6ac4c4a`             |


Pickers aceptan `application/pdf` pero no hay motor PDF (`PdfRenderer`, pdfium…). Todo pasa por `EpubParser` → fallo o comportamiento incorrecto.

**Fix:** quitar PDF de mime types hasta implementarlo.

**Archivos:** `HomeScreen.kt`, `LibraryScreen.kt`, `AddBookScreen.kt`, `BookDetailScreen.kt`.

---



### DATA-002 · Cross-refs no se sincronizan al editar libro


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `06fe93b`             |


`UpdateBookUseCase` solo INSERT IGNORE. No elimina relaciones quitadas en UI.

**Fix:** transacción DELETE + INSERT en junction tables.

---



### DATA-001 · `REPLACE` en updates rompe relaciones FK


| Campo      | Valor                           |
| ---------- | ------------------------------- |
| **Estado** | **Resuelto (v1.2.0)**           |
| **Commit** | `3b6823c`, `ed6a26d`, `41257b4` |


`@Insert(REPLACE)` = DELETE + INSERT → CASCADE destruía `book_folders` / `book_genres`. Separado en `@Insert` / `@Update` en Book, Folder y Genre.

Cierra también **READER-003** (progreso usaba `saveBook` → REPLACE).

---



### READER-003 · Actualizar progreso borra metadatos del libro


| Campo      | Valor                    |
| ---------- | ------------------------ |
| **Estado** | **Resuelto (v1.2.0)**    |
| **Commit** | `41257b4` (vía DATA-001) |


`UpdateReadingProgressUseCase` llamaba `saveBook` (REPLACE). Ahora usa `updateBook`.

---



### READER-001 · `windows.scrollY` typo en JS


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `737d185`             |


Usaba `windows.scrollY` → ReferenceError, progreso/tiempo/palabras no se reportaban. Corregido a `window.scrollY`.

---



## Cómo mantener este archivo

Ver [MAINTENANCE.md](MAINTENANCE.md). Al resolver un issue:

1. Cambiar **Estado** → `Resuelto (vX.Y.Z)` + commit.
2. Mover ID a sección **Resuelto** (no borrar).
3. Actualizar [BACKLOG.md](BACKLOG.md) checkbox.



## Plantilla nueva entrada

```markdown
### ID · Título

| Campo | Valor |
|-------|-------|
| **Estado** | Abierto |
| **Target** | vX.Y.Z |
| **Área** | paquete |

Descripción...

**Fix:** ...

**Test:** ...
```

