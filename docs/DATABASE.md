# Base de datos — JuguitoReader

## Motor y configuración

- **Room** 2.8.4, base de datos `juguito_db`, **versión 12**.
- Schemas exportados en `app/schemas/com.juguito.juguitoreader.data.local.JuguitoReaderDatabase/`.
- Config KSP: `room.schemaLocation = $projectDir/schemas`.
- Política actual: `fallbackToDestructiveMigration(false)`.

### Política de migraciones (DATA-006)

Producción y pruebas cerradas usan la misma regla: **nunca** `fallbackToDestructiveMigration(true)`. Un mismatch de schema no puede borrar la biblioteca.

- Toda subida de `version` en `@Database` exige `MIGRATION_N_N+1` registrada, schema exportado en `app/schemas/`, y test de migración si el cambio toca datos.
- No hay puente 1→6. Instalaciones o backups con schema < 6 no son soportados.
- **Backup manual (TAR-59):** el ZIP lleva `dbVersion` en `manifest.json`. Restore con `dbVersion` menor que la app: Room aplica las migraciones al cold start. Restore con `dbVersion` mayor: rechazo. Restore igual: abre tal cual.
- Al bump de schema, el export sigue escribiendo el `dbVersion` actual (versión Room). Sin `MIGRATION` nueva, los backups de la versión anterior dejarán de abrir.

## Diagrama de tablas

```
books (1) ──────< book_folders >────── (N) folders
  │                                       
  ├──────< book_genres >────── (N) genres
  │
  ├────── (1) reading_progress  [FK CASCADE a books]
  │
  └──────< daily_reading        [FK CASCADE a books]
```



## Tablas


| Tabla              | Descripción                                                                      |
| ------------------ | -------------------------------------------------------------------------------- |
| `books`            | Metadatos del libro: título, autor, estado, fechas, rating, paths, `is_physical` |
| `folders`          | Carpetas de organización (`name` UNIQUE)                                         |
| `genres`           | Etiquetas/géneros (`name` UNIQUE)                                                |
| `book_folders`     | Junction M:N libro ↔ carpeta (FK CASCADE ambos lados)                            |
| `book_genres`      | Junction M:N libro ↔ género (FK CASCADE ambos lados)                             |
| `reading_progress` | Capítulo actual, scroll, `total_chapters`, `last_read_at`                        |
| `daily_reading`    | Sesiones diarias: tiempo, % alcanzado, WPM (`reading_speed`)                     |




## Type converters

`RoomConverters.kt` persiste enums como `String`:

- `BookStatus`



## Migraciones definidas

Solo existen migraciones **6 → 12**:


| Migración | Cambio                                                 |
| --------- | ------------------------------------------------------ |
| 6→7       | Rebuild `reading_progress` + columna `scroll_position` |
| 7→8       | Columna `total_chapters` en `reading_progress`         |
| 8→9       | Tabla `daily_reading`                                  |
| 9→10      | Columna `reading_speed` en `daily_reading`             |
| 10→11     | FK CASCADE en `reading_progress`; limpia huérfanos     |
| 11→12     | Elimina columna `sync_status` (scaffold cloud)         |


Definidas en `JuguitoReaderDatabase.kt`, registradas en `DatabaseModule.kt`.

## Reglas de integridad (obligatorias)

Resumen abajo. Anti-patrones vivos: [KNOWN_ISSUES.md](KNOWN_ISSUES.md). Detalle histórico: [archive/v1.2.0.md](archive/v1.2.0.md) (DATA-001..004).

### 1. Nunca `@Insert(REPLACE)` para updates

SQLite `INSERT OR REPLACE` = **DELETE + INSERT**. Con FK `ON DELETE CASCADE` en junction tables, **borra las relaciones**.

```kotlin
// ❌ Incorrecto para actualizar
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertFolder(folder: FolderEntity): Long

// ✅ Correcto
@Insert
suspend fun insertFolder(folder: FolderEntity): Long

@Update
suspend fun updateFolder(folder: FolderEntity)
```

Aplica a: `BookDAO`, `FolderDAO`, `GenreDAO`, `ReadingProgressDAO`.

### 2. Sincronizar cross-refs en transacción

Al actualizar relaciones M:N de un libro:

```kotlin
@Transaction
    suspend fun syncBookCrossRefs(bookId: Int, folderIds: List<Int>, genreIds: List<Int>) {
        deleteBookFolderCrossRefs(bookId)
        deleteBookGenreCrossRefs(bookId)

        if (folderIds.isNotEmpty()) insertBookFolderCrossRefs(folderIds.map { BookFolderCrossRef(bookId = bookId, folderId = it) })
        if (genreIds.isNotEmpty()) insertBookGenreCrossRefs(genreIds.map { BookGenreCrossRef(bookId = bookId, genreId = it) })
    }
```

**NO** usar solo `INSERT IGNORE`, esto solo añade relaciones; **nunca elimina** relaciones quitadas en UI. 

Patrón correcto implementado en `BookDAO.syncBookCrossRefs` y en operaciones transaccionales de `BookRepositoryImpl`: `insertBookWithCrossRefs`, `updateBookWithCrossRefs` (AddBook / UpdateBook / RestoreDeletedBook); `updateBookWithNewEpub` reutiliza `updateBookWithCrossRefs`.

### 3. Resolver relaciones por ID

En updates, usar `folder.id` / `genre.id` cuando `id != 0`. Si ese id ya no existe, **omitir la relación** — no hacer `insert` por nombre (resucitaría carpetas/géneros borrados). Resolver por `name` solo cuando `id == 0` (género nuevo en el formulario, import EPUB).

### 4. `reading_progress` y borrado de libro

`reading_progress` tiene FK CASCADE a `books` (schema v12). Al borrar un libro, el progreso se elimina en cascada. El undo desde Home restaura progreso vía `DeletedBookSnapshot` (DATA-007).

## Queries importantes

- `BookDAO.getAllBooks()` — `@Transaction` + `BookWithDetails` con `@Relation` y `@Junction`.
- `FolderDAO.getFoldersWithBookCount()` — subquery COUNT en junction.



## Cómo añadir una migración

1. Incrementar `version` en `@Database`.
2. Crear `MIGRATION_X_Y` en `JuguitoReaderDatabase.companion object`.
3. Registrar en `DatabaseModule.addMigrations(...)`.
4. Compilar — Room valida contra schema exportado.
5. Commit del nuevo JSON en `app/schemas/...`.



## Tests de DAO

Instrumentados en `app/src/androidTest/.../dao/`:

- `BookDAOTest`, `FolderDAOTest`, `GenreDAOTest`, `ReadingProgressDAOTest`
- `ReadingProgressMigrationTest` (10→11), `SyncStatusRemovalMigrationTest` (11→12)

