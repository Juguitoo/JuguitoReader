# Errores y riesgos conocidos — JuguitoReader

Registro vivo de bugs, riesgos y anti-patrones. **Consultar antes de modificar Room, reader, importación de archivos o relaciones M:N.**

---

## Índice por severidad


| Severidad    | IDs                                                                                  |
| ------------ | ------------------------------------------------------------------------------------ |
| **Crítico**  |                                                                                      |
| **Alto**     | DATA-007, DATA-008, FILE-004, SEC-002, SEC-003, READER-004                             |
| **Medio**    | DATA-004, DATA-006, READER-005…013, FILE-005, UX-001, UX-002, PERF-001, ARCH-002     |
| **Mejora**   | ARCH-001, REL-001, REL-002, UX-003, I18N-001                                         |
| **Resuelto** | SEC-001, FILE-003, FILE-001, FILE-002, DATA-003, READER-002, READER-001, DATA-001, READER-003, DATA-002 |


---



## Alto



### DATA-007 · Undo delete no restaura daily_reading


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`daily_reading` tiene FK CASCADE a `books`. Delete borra historial; `addBookUseCase` en undo no lo recupera.

**Fix:** soft delete, o backup de sesiones antes de delete, o undo antes de confirm CASCADE.

---



### DATA-008 · Operaciones libro + carpetas + géneros no atómicas


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


AddBookUseCase / UpdateBookUseCase: múltiples writes sin `@Transaction`. Fallo intermedio → BD inconsistente.

---



### FILE-004 · Archivos internos huérfanos


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


EPUB/cover se copian antes de confirmar save. Cancelación o error → archivos abandonados en storage.

---



### SEC-002 · Posible zip bomb


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`EpubParser.unzip`: hay protección Zip Slip parcial pero sin límite de tamaño descomprimido, entradas o bytes por entry.

---



### SEC-003 · Path traversal tras unzip


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


Rutas de container.xml / OPF / manifest → `File(dir, href)` sin verificar `canonicalPath` dentro del directorio destino.

---



### READER-004 · Crash EPUB con menos capítulos


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`lastChapterIndex` persistido; al cargar EPUB nuevo más corto, acceso `spine[index]` sin `coerceIn` → index OOB.

**Archivo:** `ReaderViewModel.kt`

---



## Medio



### DATA-004 · reading_progress huérfano al borrar libro

Sin FK a `books`. DeleteBook no limpia progreso.

---



### DATA-006 · Migraciones 1→5 inexistentes

Solo 6→10. En dev aceptable (destructive OK). Antes de open testing: definir política.

---



### READER-005 · Brillo no se restaura al salir

`ReaderControls` modifica `window.attributes.screenBrightness` sin restore en `DisposableEffect.onDispose`.

---



### READER-006 · System bars ocultas permanentemente

`ReaderScreen` oculta barras; no restaura al salir.

---



### READER-007 · WebView no destruido

Falta `stopLoading()`, quitar JS bridge, `destroy()` en `AndroidView.onRelease`.

**Relacionado:** READER-012 (proceso de render).

---



### READER-008 · TOC con #fragment

`chapter.xhtml#section2` no matchea bien con spine por nombre final → navegación TOC ambigua.

---



### READER-009 · EPUB3 nav incompleto

NCX parseado; falta soporte completo HTML Navigation Document (`properties="nav"`).

---



### READER-010 · Errores unzip silenciados

`EpubParser.kt` ~285-287: `printStackTrace()` sin rethrow → extracción incompleta silenciosa.

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

### SEC-001 · WebView inseguro para contenido EPUB


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `903dede`, `6822276` |


Carga `file://` + `allowFileAccess`. EPUB = HTML de terceros.

**Fix:** `WebViewAssetLoader` + `InternalStoragePathHandler` sobre `cacheDir/reader/{id}`; URL `https://appassets.androidplatform.net/epub/…`; `allowFileAccess` / `allowContentAccess` en false; `shouldOverrideUrlLoading` bloquea otros hosts.

**Residual:** `javaScriptEnabled` + `AndroidBridge` (necesario para scroll/WPM). Zip bomb / path traversal en parser: SEC-002, SEC-003. Renderer: READER-012.

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

