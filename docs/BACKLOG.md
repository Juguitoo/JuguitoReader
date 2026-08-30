# Backlog — JuguitoReader

Backlog del proyecto (fuente única). Incluye issues del audit + features planificadas. IDs estables para commits: `fix: DATA-001 ...`

**Leyenda:** `[ ]` pendiente · `[~]` en progreso · `[x]` hecho

---

## v1.2.0 — Estabilización (cierre)

> **Scope acordado:** resolver todos los issues abiertos en [KNOWN_ISSUES.md](KNOWN_ISSUES.md) antes de cerrar v1.2.0.

### P0 — Críticos


| ID         | Tarea                                                                 | Estado | Ref          |
| ---------- | --------------------------------------------------------------------- | ------ | ------------ |
| DATA-001   | Separar INSERT / UPDATE en DAOs (Book, Folder, Genre)                 | `[x]`  | KNOWN_ISSUES |
| DATA-002   | Sync cross-refs en transacción (UpdateBookUseCase)                    | `[x]`  | KNOWN_ISSUES |
| READER-002 | PDF aceptado en picker pero no soportado — quitar                     | `[x]`  | KNOWN_ISSUES |
| READER-003 | UpdateReadingProgressUseCase usa saveBook(REPLACE) → borra relaciones | `[x]`  | KNOWN_ISSUES |




### P1 — Altos (audit)


| ID         | Tarea                                                           | Estado | Ref          |
| ---------- | --------------------------------------------------------------- | ------ | ------------ |
| DATA-003   | Resolver relaciones por ID, no por nombre                       | `[x]`  | KNOWN_ISSUES |
| FILE-001   | Mezcla content:// URI vs path en filesystem                     | `[x]`  | KNOWN_ISSUES |
| FILE-002   | BookDetail guarda URI en vez de path interno al reemplazar EPUB | `[x]`  | KNOWN_ISSUES |
| FILE-003   | ParseEpubUseCase / extractFullContent sin Dispatchers.IO        | `[x]`  | KNOWN_ISSUES |
| SEC-001    | WebView + file:// + allowFileAccess — usar WebViewAssetLoader   | `[x]`  | KNOWN_ISSUES |
| SEC-002    | Zip bomb — limitar tamaño/descompresión en EpubParser           | `[x]`  | KNOWN_ISSUES |
| SEC-003    | Path traversal tras unzip (canonicalPath)                       | `[~]`  | KNOWN_ISSUES |
| READER-004 | Crash al reemplazar EPUB con menos capítulos (index OOB)        | `[ ]`  | KNOWN_ISSUES |
| FILE-004   | Archivos internos huérfanos (EPUB/cover sin confirmar)          | `[ ]`  | KNOWN_ISSUES |
| DATA-007   | Undo delete no restaura daily_reading (CASCADE)                 | `[ ]`  | KNOWN_ISSUES |
| DATA-008   | AddBook/UpdateBook sin transacción atómica                      | `[ ]`  | KNOWN_ISSUES |




### P2 — Medios (audit)


| ID         | Tarea                                                        | Estado | Ref          |
| ---------- | ------------------------------------------------------------ | ------ | ------------ |
| DATA-004   | Cleanup reading_progress al borrar libro                     | `[ ]`  | KNOWN_ISSUES |
| READER-005 | Restaurar brillo al salir del lector                         | `[ ]`  | KNOWN_ISSUES |
| READER-006 | Restaurar system bars al salir del lector                    | `[ ]`  | KNOWN_ISSUES |
| READER-007 | Destruir WebView explícitamente (onRelease)                  | `[ ]`  | KNOWN_ISSUES |
| READER-008 | TOC con #fragment falla                                      | `[ ]`  | KNOWN_ISSUES |
| READER-009 | EPUB3 nav document incompleto                                | `[ ]`  | KNOWN_ISSUES |
| READER-010 | Errores unzip silenciados en EpubParser                      | `[ ]`  | KNOWN_ISSUES |
| FILE-005   | openInputStream null pero devuelve path válido               | `[ ]`  | KNOWN_ISSUES |
| UX-001     | Loading global al importar (Home)                            | `[ ]`  | KNOWN_ISSUES |
| UX-002     | Pantallas stuck en Loading tras error                        | `[ ]`  | KNOWN_ISSUES |
| PERF-001   | Demasiadas escrituras BD durante scroll                      | `[ ]`  | KNOWN_ISSUES |
| READER-011 | Conteo palabras duplica al retroceder scroll                 | `[ ]`  | KNOWN_ISSUES |
| READER-012 | WebView sin onRenderProcessGone (crash si muere el renderer) | `[ ]`  | KNOWN_ISSUES |
| READER-013 | FOUC / flicker de HTML al cambiar de capítulo                | `[ ]`  | KNOWN_ISSUES |
| ARCH-002   | Revisar allowBackup vs política local-only                   | `[ ]`  | KNOWN_ISSUES |




### P3 — Mejoras / limpieza v1.2


| ID        | Tarea                                                                      | Estado | Ref          |
| --------- | -------------------------------------------------------------------------- | ------ | ------------ |
| CLEAN-001 | Eliminar scaffold sync (SyncStatus, deps Supabase, TODOs)                  | `[ ]`  | KNOWN_ISSUES |
| CLEAN-002 | SortOption.displayName → strings.xml                                       | `[ ]`  |              |
| DATA-009  | Separar INSERT/UPDATE en ReadingProgressDAO (higiene; REPLACE no rompe FK) | `[ ]`  | DATABASE.md  |
| I18N-001  | Strings hardcodeadas en EpubParser, FileUtils                              | `[ ]`  | KNOWN_ISSUES |
| GIT-001   | Renombrar rama v1.2.0 → feature/* o merge dev                              | `[ ]`  | GIT_WORKFLOW |
| DOC-001   | Documentación base (README, docs/, rules)                                  | `[x]`  |              |




### Resueltos en v1.2.0


| ID         | Tarea                                                               | Commit / nota                              |
| ---------- | ------------------------------------------------------------------- | ------------------------------------------ |
| SEC-002    | Límites anti zip bomb en EpubParser (unzip + cover)                 | `11e1d4d`, `14078f0`                       |
| SEC-001    | WebView EPUB vía WebViewAssetLoader (sin file:// / allowFileAccess) | `903dede` (+ webkit `6822276`)             |
| FILE-003   | Parse/import EPUB I/O en Dispatchers.IO (lector + Home/Library)     | `7f4cf25`, `1bbe4fb`                       |
| FILE-002   | BookDetail guarda URI en vez de path interno al reemplazar EPUB     | `8cf2a86`, `32e8d83`                       |
| FILE-001   | Mezcla content:// URI vs path en filesystem                         | `906b659`, `32e8d83`                       |
| DATA-003   | Resolver relaciones por ID, no por nombre                           | `901ad24`, `d26e95c`                       |
| READER-002 | PDF aceptado en picker pero no soportado — quitar                   | `6ac4c4a`                                  |
| DATA-002   | Sync cross-refs en transacción (UpdateBookUseCase)                  | `06fe93b`                                  |
| DATA-001   | Separar INSERT / UPDATE (Book, Folder, Genre)                       | `3b6823c`, `ed6a26d`, `41257b4`, `28a8981` |
| READER-003 | Progress update usaba saveBook(REPLACE)                             | `41257b4` (vía DATA-001)                   |
| READER-001 | `windows.scrollY` → `window.scrollY`                                | `737d185`                                  |


---



## Pendientes por versión



### v1.3.0


| ID     | Tarea                                       | Tipo |
| ------ | ------------------------------------------- | ---- |
| TAR-19 | Recopilación de estadísticas                | Feat |
| TAR-20 | Sección estadísticas                        | Feat |
| TAR-47 | Reestructuración carpetas (archivadores)    | Feat |
| TAR-49 | Controles biblioteca (filtros + ordenación) | Feat |
| TAR-50 | Filtro por autor                            | Feat |
| TAR-51 | Marcadores en libros                        | Feat |
| TAR-52 | Tipografías en lector                       | Feat |
| TAR-53 | Modo horizontal                             | Feat |
| TAR-54 | Sistema favoritos + estantería Home         | Feat |
| TAR-56 | Colapsar sección gestión en drawer          | Feat |




### v1.4.0


| ID     | Tarea                         | Tipo |
| ------ | ----------------------------- | ---- |
| TAR-31 | Compatibilidad básica EPUB3   | Feat |
| TAR-32 | Compatibilidad avanzada EPUB3 | Feat |




### v2.0.0 / v3.0.0


| ID     | Tarea                     | Tipo |
| ------ | ------------------------- | ---- |
| TAR-29 | Conexión Supabase/Dropbox | Feat |
| TAR-30 | Motor visor EPUB propio   | Feat |


---



## P1 — Pre-release Play Store


| ID       | Tarea                                                         | Estado |
| -------- | ------------------------------------------------------------- | ------ |
| REL-001  | Keystore release (no debug)                                   | `[ ]`  |
| REL-002  | Habilitar R8 / ProGuard                                       | `[ ]`  |
| REL-003  | CI: `./gradlew test` en push (GitHub Actions) + hook opcional | `[x]`  |
| DATA-006 | Migraciones DB 1→5 o política destructive en dev              | `[ ]`  |


---



## Done — histórico

v1.0.0 – v1.1.0 – v1.2.0 features (clic para expandir)


| ID     | Tarea                       | Versión |
| ------ | --------------------------- | ------- |
| TAR-6  | Configurar proyecto         | v1.0.0  |
| TAR-7  | Inicializar BD              | v1.0.0  |
| TAR-8  | Capa repositorio            | v1.0.0  |
| TAR-9  | Capa ViewModel              | v1.0.0  |
| TAR-10 | Interfaz principal          | v1.0.0  |
| TAR-11 | Formulario libros           | v1.0.0  |
| TAR-13 | Configurar Supabase         | v1.0.0  |
| TAR-14 | Casos de uso                | v1.0.0  |
| TAR-15 | Detalle libro               | v1.0.0  |
| TAR-16 | Biblioteca                  | v1.0.0  |
| TAR-17 | Registro                    | v1.0.0  |
| TAR-18 | Lector EPUB                 | v1.0.0  |
| TAR-21 | Gestión carpetas y géneros  | v1.0.0  |
| TAR-36 | Bug tamaño letra y scroll   | v1.0.2  |
| TAR-22 | Pantalla ajustes            | v1.1.0  |
| TAR-23 | Diálogos + snackbars        | v1.1.0  |
| TAR-24 | Deshacer                    | v1.1.0  |
| TAR-26 | Importar desde biblioteca   | v1.1.0  |
| TAR-33 | Home vacío mejorado         | v1.1.0  |
| TAR-34 | Persistencia ajustes lector | v1.1.0  |
| TAR-37 | Carpetas desde formulario   | v1.1.0  |
| TAR-38 | Botones crear en gestor     | v1.1.0  |
| TAR-39 | Creador géneros             | v1.1.0  |
| TAR-40 | Iconos gestor               | v1.1.0  |
| TAR-42 | Formulario carpeta          | v1.1.0  |
| TAR-43 | Gestor + conteo             | v1.1.0  |
| TAR-25 | Velocidad lectura           | v1.2.0  |
| TAR-27 | Sesiones lectura            | v1.2.0  |
| TAR-28 | i18n                        | v1.2.0  |
| TAR-44 | Automatizaciones            | v1.2.0  |
| TAR-45 | Sugerencia estado           | v1.2.0  |
| TAR-46 | % en portadas               | v1.2.0  |
| TAR-55 | Tema neon                   | v1.2.0  |


---



## Commits con IDs

```
fix: DATA-001 use Update instead of Replace in FolderDAO
fix: READER-003 avoid saveBook on progress update
feat: TAR-49 library filter controls
docs: update KNOWN_ISSUES READER-001 resolved
```

