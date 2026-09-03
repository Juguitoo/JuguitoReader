# Errores y riesgos conocidos — JuguitoReader

Registro vivo de bugs, riesgos y anti-patrones. **Consultar antes de modificar Room, reader, importación de archivos o relaciones M:N.**

---

## Índice por severidad


| Severidad    | IDs                                                                                                                                                                                                                                     |
| ------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Crítico**  |                                                                                                                                                                                                                                         |
| **Alto**     |                                                                                                                                                                                                                                         |
| **Medio**    |                                                                                                                                                                                                                               |
| **Diferido** | READER-008, READER-009 → v1.4.0 (TAR-31)                                                                                                                                                                                                |
| **Mejora**   | REL-001, REL-002, UX-003                                                                                                                                                                                                                |
| **Resuelto** | ARCH-001, ARCH-002, READER-011, READER-012, READER-013, READER-014, READER-015, PERF-001, UX-001, UX-002, FILE-005, READER-010, READER-005, READER-006, READER-007, DATA-008, DATA-004, DATA-007, FILE-004, READER-004, SEC-003, SEC-002, SEC-001, FILE-003, FILE-001, FILE-002, DATA-003, READER-002, READER-001, DATA-001, READER-003, DATA-002 |




## Crítico

*(Sin issues abiertos en esta severidad.)*

---

## Alto

*(Sin issues abiertos en esta severidad.)*

---

## Medio

### READER-008 · TOC con #fragment


| Campo           | Valor                 |
| --------------- | --------------------- |
| **Estado**      | **Diferido (v1.4.0)** |
| **Relacionado** | TAR-31, READER-009    |


`chapter.xhtml#section2` no matchea bien con spine por nombre final → navegación TOC ambigua.

**Nota:** limitación conocida del lector EPUB2 actual. No bloquea v1.2.0; se abordará con compatibilidad EPUB3 / navegación del índice (v1.4.0).

---



### READER-009 · EPUB3 nav incompleto


| Campo           | Valor                 |
| --------------- | --------------------- |
| **Estado**      | **Diferido (v1.4.0)** |
| **Relacionado** | TAR-31, TAR-32        |


NCX parseado; falta soporte completo HTML Navigation Document (`properties="nav"`).

**Nota:** EPUB3 parcial — muchos libros EPUB3 siguen leyéndose vía NCX o índice autogenerado desde spine. Soporte nav completo previsto en v1.4.0 (TAR-31).

---

## Mejoras (pre-release)



### REL-001 · Release firmado con debug

`app/build.gradle.kts:29`

---



### REL-002 · Release sin R8

`isMinifyEnabled = false`

---



### UX-003 · Portrait lock

`AndroidManifest.xml` — coherente con TAR-53 (horizontal) en v1.3.

---



## Resuelto



### ARCH-001 · Scaffold sync sin usar


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `c4aaa81`             |
| **Ticket** | CLEAN-001             |


Deps de Supabase (`auth-kt`, `postgrest-kt`) y Ktor, enum `SyncStatus`, columna `sync_status` en Room y stubs `syncPending*()` / `getUnsynced*()` eran scaffold de TAR-13, nunca conectado.

**Fix:** eliminación del enum, converters, campos de dominio/entidad, queries y TODOs; deps Gradle fuera del catálogo. Migración Room 11→12 reconstruye `books`, `folders`, `genres` y `reading_progress` sin `sync_status`. La nube de pago sigue en TAR-29 (v2), a reimplementar desde cero.

**Archivos:** entities, mappers, DAOs, repositorios, `JuguitoReaderDatabase`, `libs.versions.toml`

**Test:** `SyncStatusRemovalMigrationTest` (11→12 preserva filas e índices UNIQUE).

**Relacionado:** TAR-29 (cloud, pospuesto).

---



### ARCH-002 · Auto Backup vs local-only


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `853a3a4`             |


`allowBackup="true"` con rules vacías permitía Auto Backup / D2D de DB y `filesDir`, en conflicto con política local-only y con el valor futuro de backup manual (TAR-59) / nube de pago (TAR-29).

**Fix:** `allowBackup="false"`, `fullBackupContent="false"`, y `data_extraction_rules.xml` con excludes totales en `cloud-backup` y `device-transfer`. Eliminado `backup_rules.xml` del template.

**Archivos:** `AndroidManifest.xml`, `res/xml/data_extraction_rules.xml`

**Relacionado:** TAR-59 (export/import manual — gate producción Play).

---



### READER-011 · Conteo palabras duplica al retroceder


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `fe50fe3`             |


**Criterio de producto (revisado):** las palabras solo alimentan el WPM de la sesión. Retroceder el scroll no debe sumar nada, pero **releer sí cuenta** (dentro del mismo capítulo o volviendo a él): es lectura real que consume tiempo de sesión. Descartar relecturas hundiría el WPM.

Con ese criterio, la acumulación por incrementos positivos de `OnReportWordsRead` ya era correcta y el síntoma descrito en el ticket no era un bug. Lo que sí fallaba: al guardar sesión desde el diálogo de sesiones, `saveCurrentReadingSession` reseteaba `accumulatedReadWords` pero la escritura siguiente usaba una copia previa del estado y revertía el reset, arrastrando esas palabras a la sesión siguiente y duplicando su aportación al WPM.

**Fix:** las escrituras posteriores a `saveCurrentReadingSession` usan `updateSuccessState` (lectura fresca del estado) para no revertir el reset. La lógica de acreditación se extrae a `creditWordsRead` con el criterio documentado: `delta.coerceAtLeast(0)` sobre la última posición reportada, reiniciada en `updateChapter`.

**Relacionado:** READER-015 (línea base al restaurar el scroll).

**Archivo:** `ReaderViewModel.kt`, `ReaderUiState.kt`

**Test:** `ReaderViewModelTest` (`READER-011 scrolling backwards adds no words but rereading forward does`, `rereading a chapter credits its words again`, `READER-011 saving a session resets accumulated words`).

---



### READER-015 · Palabras regaladas al abrir por un capítulo empezado


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `fe50fe3`             |


La línea base de palabras arrancaba en 0 al cargar el capítulo, pero el lector restaura el scroll a la posición guardada. El primer avance acreditaba de golpe todas las palabras por encima de ese punto sin haberlas leído: reanudar al 60 % de un capítulo regalaba ese 60 % al numerador del WPM. Afectaba a la apertura del libro (navegar entre capítulos restaura a scroll 0) y, por tanto, a la primera sesión de cada apertura.

**Fix:** el script inyectado reporta la posición restaurada con `AndroidBridge.reportInitialWordsRead(...)` justo después de `window.scrollTo`; el ViewModel la fija como `lastReportedChapterWords` sin acreditarla (`OnChapterWordsBaseline`). No se pierde el primer tramo leído de verdad, a diferencia de ignorar el primer reporte.

**Relacionado:** READER-011 (mismo contador).

**Archivo:** `EpubWebView.kt`, `ReaderViewModel.kt`, `ReaderEvent.kt`

**Test:** `ReaderViewModelTest` (`READER-015 restored scroll position is a baseline and credits no words`).

---



### READER-012 · WebView sin onRenderProcessGone


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `2136d05`             |


El HTML corre en un proceso de render aparte. Si moría (OOM, crash de Chromium) sin `onRenderProcessGone`, Android mataba la app.

**Fix:** `EpubWebView` implementa el callback: saca el `WebView` del árbol, lo destruye (`releaseWebView`, idempotente y compartido con `onRelease`), devuelve `true` y notifica `ReaderEvent.OnRenderProcessGone`. El ViewModel incrementa `webViewInstanceKey`; `ReaderContent` lo usa como `key(...)` para crear una instancia limpia. Tras `MAX_RENDERER_RECOVERIES` (2) pasa a `Error` en vez de reintentar en bucle.

**Relacionado:** READER-007 (ciclo de vida del WebView).

**Archivo:** `EpubWebView.kt`, `ReaderViewModel.kt`, `ReaderScreen.kt`

**Test:** `ReaderViewModelTest` (`READER-012 render process gone recreates the WebView and then fails with Error`).

---



### READER-013 · FOUC al cambiar de capítulo


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `b63d0c3`             |


Al pasar de capítulo se veía un instante el HTML del EPUB sin tema ni padding, y después el CSS inyectado en `onPageFinished`. Agravado por SEC-001: el origen sintético pinta el documento antes de `evaluateJavascript`.

**Fix:** el `WebView` arranca `INVISIBLE` y se oculta de nuevo en `onPageStarted` con el color de fondo del tema; se revela en el callback de `evaluateJavascript` de `onPageFinished`, ya con CSS y scroll restaurado (timeout de seguridad de 2 s por si la inyección no responde). Además, tema y scroll se leen del `WebViewHolder` (valores frescos) en vez de la captura de la primera composición, y `setBackgroundColor` se realinea solo cuando cambia el tema.

**Archivo:** `EpubWebView.kt`

---



### READER-014 · Temporizador de sesión perdido durante Loading


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `a89bce2`             |


`ReaderScreen` envía `OnStartReading` en `ON_RESUME`, pero `onEvent` descartaba todo mientras el estado no fuese `Success`. Si el EPUB seguía parseándose, el temporizador no arrancaba y la sesión no se registraba hasta otro pause/resume.

**Fix:** `OnStartReading` / `OnFinishReading` se procesan antes del guard de `Success`. `OnStartReading` marca el lector como resumed y `startSessionTimerIfPossible()` arranca el cronómetro (idempotente: no reinicia una sesión abierta); `loadData` lo vuelve a invocar al publicar `Success`. `OnFinishReading` limpia el flag, hace flush del progreso y cierra la sesión.

**Archivo:** `ReaderViewModel.kt`

**Test:** `ReaderViewModelTest` (`READER-014 session timer starts when loading finishes…`, `…does not start if reader was paused while loading`, `…repeated OnStartReading keeps the original session start`).

---



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


| Campo      | Valor                           |
| ---------- | ------------------------------- |
| **Estado** | **Resuelto (v1.2.0)**           |
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
| **Commit** | `11e1d4d`, `14078f0`  |


`EpubParser.unzip` tenía Zip Slip parcial pero sin tope de bytes descomprimidos ni de entradas.

**Fix:** límites (512 MiB total / 128 MiB por entry / 10 000 entries; cover 20 MiB); contar bytes escritos vía `copyBounded`; abort + `deleteRecursively` del destino; `unzip(InputStream, …)` testeable; `ParseEpubUseCase` → `error_unzip`. Completado en READER-010: parse fail-fast y cleanup de caché en `ensureExtracted`.

**Test:** `EpubParserSecurityTest`, `ParseEpubUseCaseTest`, `EpubParserExtractTest`.

---



### SEC-001 · WebView inseguro para contenido EPUB


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `903dede`, `6822276`  |


Carga `file://` + `allowFileAccess`. EPUB = HTML de terceros.

**Fix:** `WebViewAssetLoader` + `InternalStoragePathHandler` sobre `cacheDir/reader/{id}`; URL `https://appassets.androidplatform.net/epub/…`; `allowFileAccess` / `allowContentAccess` en false; `shouldOverrideUrlLoading` bloquea otros hosts.

**Residual:** `javaScriptEnabled` + `AndroidBridge` (necesario para scroll/WPM). Renderer: READER-012.

**Test:** `EpubChapterUrlTest`.

---



### FILE-003 · Parse EPUB puede bloquear UI


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `7f4cf25`, `1bbe4fb`  |


`ParseEpubUseCase` y `GetBookFromEpubUseCase` ejecutaban unzip/parse/copy en el hilo Main → ANR o tirones al abrir lector o importar desde Home/Library.

**Fix:** `withContext(Dispatchers.IO)` en ambos use cases; `GetBookFromEpubUseCase` pasa a `suspend`. ViewModels de Add/Detail dejan de envolver redundante.

**Test:** `ParseEpubUseCaseTest`, `GetBookFromEpubUseCaseTest`, `ImportBookFromUriUseCaseTest`, `BookDetailViewModelTest`.

---



### FILE-004 · Archivos internos huérfanos


| Campo      | Valor                                                 |
| ---------- | ----------------------------------------------------- |
| **Estado** | **Resuelto (v1.2.0)**                                 |
| **Commit** | `f575581`, `a302657`, `3bb1edf`, `092c7b1`, `e76f776` |


EPUB y cover se copiaban a `filesDir` en picker/import antes de confirmar en Room. Cancelar, sustituir o fallar el save dejaba ficheros huérfanos.

**Fix (commit on save):** draft con `content://` o staging en `cacheDir`; `promotePendingFiles` solo al guardar. AddBook y BookDetail hacen rollback de lo promocionado si falla Room; BookDetail borra assets sustituidos y `cache/reader/{id}` al cambiar EPUB (complementa READER-004). Import one-shot (`ImportBookFromUriUseCase`) copia y hace rollback si `addBook` falla. Helpers `deleteStagingAsset` / `deleteFileFromInternalStorage`.

**Test:** `FileUtilsTest`, `GetBookFromEpubUseCaseTest`, `ImportBookFromUriUseCaseTest`, `AddBookViewModelTest`, `BookDetailViewModelTest`.

**Residual:** huérfanos acumulados en instalaciones previas (sin barrido histórico); Room y filesystem no comparten transacción (limitación arquitectural).

---



### FILE-005 · openInputStream null devuelve path


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `deca14f` |


`FileUtils.saveImageToInternalStorage` / `saveEpubBookToInternalStorage`: si `openInputStream(uri)` era null, no copiaba pero retornaba `absolutePath` → path fantasma en BD.

**Fix:** `openInputStream(uri) ?: throw IOException(...)` antes de `FileOutputStream`; el `catch` existente propaga `JuguitoException` (`error_copy_cover` / `error_copy_epub`).

---



### READER-010 · Errores unzip silenciados


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `6d21d74` |


`extractFullContent` tragaba excepciones de parse con `printStackTrace()`; fallos de unzip podían dejar extracción incompleta; `openInputStream` null en `ensureExtracted` dejaba caché vacía y bloqueaba reintentos.

**Fix:** refactor en `ensureExtracted`, `parseContainerOpfPath`, `parseOpf` y `parseNcx`; fail-fast en container/OPF/spine vacío; NCX opcional con fallback por spine; filtrado de nav points vacíos; cleanup de caché si falla la extracción; `openInputStream` null → `IOException`.

**Test:** `EpubParserExtractTest` (androidTest), `EpubParserSecurityTest`, `ParseEpubUseCaseTest`.

---



### UX-001 · Loading global al importar


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `6c5f9f8` |


`HomeViewModel` / `LibraryViewModel.importBook()` ponían `UiState.Loading` → spinner a pantalla completa tapando contenido durante import one-shot.

**Fix:** flag `isImporting` aparte del `UiState`; overlay semitransparente sobre `Success`/`Empty`; botones de import deshabilitados o guardados con `!isImporting`.

**Archivos:** `HomeViewModel.kt`, `HomeScreen.kt`, `LibraryViewModel.kt`, `LibraryScreen.kt`.

---



### UX-002 · Stuck en Loading tras error


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `6c5f9f8` |


Tras fallo de import en Home/Library, el estado quedaba en `Loading` sin restaurar la pantalla. AddBook podía dejar `isLoading` activo en algunos paths.

**Fix:** `try/finally` en import de Home/Library; AddBook ya resetea `isLoading` en save/import (validaciones antes de activar loading; `finally` en save).

**Test:** `AddBookViewModelTest` (`OnImportEpub clears loading state on failure`, save rollback paths).

---



### PERF-001 · Escrituras excesivas en scroll


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `e815a1d` |


Cada `OnScrollPositionChanged` persistía al momento en Room (el debounce JS de 500 ms en WebView no bastaba).

**Fix:** debounce 2 s en `ReaderViewModel` (`scheduleProgressPersist`); UI en memoria al instante; flush en pause (`OnFinishReading`), atrás y cambio de capítulo; deduplicación por capítulo + scroll (`hasSamePersistedValues`).

**Archivo:** `ReaderViewModel.kt`

**Test:** `ReaderViewModelTest` (debounce, flush en pause/back, UI antes de persist).

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

