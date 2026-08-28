# Errores y riesgos conocidos — JuguitoReader

Registro vivo de bugs, riesgos y anti-patrones. **Consultar antes de modificar Room, reader, importación de archivos o relaciones M:N.**

---

## Índice por severidad


| Severidad    | IDs                                                                              |
| ------------ | -------------------------------------------------------------------------------- |
| **Crítico**  | READER-002                                                                       |
| **Alto**     | DATA-002, DATA-003, DATA-007, DATA-008, FILE-001…004, SEC-001…003, READER-004    |
| **Medio**    | DATA-004, DATA-006, READER-005…011, FILE-005, UX-001, UX-002, PERF-001, ARCH-002 |
| **Mejora**   | ARCH-001, REL-001, REL-002, UX-003, I18N-001                                     |
| **Resuelto** | READER-001, DATA-001, READER-003                                                 |


---



## Crítico



### READER-002 · PDF aceptado pero no soportado


| Campo      | Valor                              |
| ---------- | ---------------------------------- |
| **Estado** | Abierto                            |
| **Target** | v1.2.0                             |
| **Área**   | Home, Library, AddBook, BookDetail |


Pickers aceptan `application/pdf` pero no hay motor PDF (`PdfRenderer`, pdfium…). Todo pasa por `EpubParser` → fallo o comportamiento incorrecto.

**Fix:** quitar PDF de mime types hasta implementarlo, o añadir pipeline PDF explícito.

**Archivos:** `HomeScreen.kt`, `LibraryScreen.kt`, `AddBookScreen.kt`, `BookDetailScreen.kt`.

---



## Alto



### DATA-002 · Cross-refs no se sincronizan al editar libro


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`UpdateBookUseCase` solo INSERT IGNORE. No elimina relaciones quitadas en UI.

**Fix:** transacción DELETE + INSERT en junction tables.

---



### DATA-003 · Relaciones resueltas por nombre


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`getFolderByName` / `getGenreByName` en Add/UpdateBook. Tras renombrar entidad, libro en memoria con nombre viejo → duplicados o enlaces rotos.

**Fix:** usar `id` cuando `id != 0`.

---



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



### FILE-001 · Mezcla content:// y path filesystem


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


Algunas pantallas guardan URI del picker; `EpubParser` usa `File(path)` → falla.

**Relacionado:** `GetBookFromEpubUseCase` fallback `uri.toString()` si copy falla.

---



### FILE-002 · Reemplazar EPUB guarda path incorrecto


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`BookDetailViewModel`: `getBookFromEpubUseCase` copia a interno pero draft guarda `event.localFilePath` (URI) en vez de path interno.

**Archivo:** `BookDetailViewModel.kt` ~159-168

---



### FILE-003 · Parse EPUB puede bloquear UI


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`ParseEpubUseCase` llama `EpubParser.extractFullContent` sin `withContext(Dispatchers.IO)`.

---



### FILE-004 · Archivos internos huérfanos


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


EPUB/cover se copian antes de confirmar save. Cancelación o error → archivos abandonados en storage.

---



### SEC-001 · WebView inseguro para contenido EPUB


| Estado | Abierto · v1.2.0 |
| ------ | ---------------- |


`javaScriptEnabled`, `allowFileAccess`, carga `file://`. EPUB = contenido no confiable.

**Fix:** `WebViewAssetLoader` (Android docs).

**Archivo:** `EpubWebView.kt`

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



### DATA-001 · `REPLACE` en updates rompe relaciones FK


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
| **Commit** | `3b6823c`, `ed6a26d`, `41257b4` |


`@Insert(REPLACE)` = DELETE + INSERT → CASCADE destruía `book_folders` / `book_genres`. Separado en `@Insert` / `@Update` en Book, Folder y Genre.

Cierra también **READER-003** (progreso usaba `saveBook` → REPLACE).

---



### READER-003 · Actualizar progreso borra metadatos del libro


| Campo      | Valor                 |
| ---------- | --------------------- |
| **Estado** | **Resuelto (v1.2.0)** |
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

