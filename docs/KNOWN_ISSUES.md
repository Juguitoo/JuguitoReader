# Errores y riesgos conocidos — JuguitoReader

Registro **vivo** de bugs, riesgos y anti-patrones abiertos. Solo bugs/errores/riesgos (no chores de release ni features). Histórico resuelto: [archive/](archive/).

Consultar antes de tocar Room, reader, importación o relaciones M:N.

---

## Issues abiertos

Una sola tabla. Orden: target v1.2.0 → sin versión (`—`) → diferidos. Si el comentario se queda corto, detalle bajo la tabla e indicarlo con un (1) o el número que le siga al último detalle.


| ID         | Título                                                                  | Severidad | Estado   | Target | Comentario                                                                                                                                     | Relacionado        |
| ---------- | ----------------------------------------------------------------------- | --------- | -------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| FILE-011   | `takePersistableUriPermission` sin catch                                | Alto      | Abierto  | v1.2.0 | Tras el picker se llama siempre. Algunos providers no dan grant persistente → crash. Tras copiar a `filesDir` el persistable no hace falta.    |                    |
| DATA-011   | Add/update dejan el draft expuesto (drawer) y pueden resucitar carpetas | Medio     | Abierto  | v1.2.0 | Bloquear el drawer (y otras rutas) mientras hay un add/update intermedio. Si un id del draft ya no existe, no insertar por nombre. (3)         | DATA-003, DATA-017 |
| UX-008     | Columna Inicio del Registry ordena por `createdAt`                      | Medio     | Abierto  | v1.2.0 | Ordenar por la fecha que se ve en la celda (`startDate`), no por alta en la app.                                                               |                    |
| UX-015     | Delete en Home como side-effect de composición                          | Medio     | Abierto  | v1.2.0 | `onEvent(OnDeleteBookClick)` corre en el cuerpo del Composable, no en `LaunchedEffect`. Puede dispararse dos veces.                            |                    |
| UX-017     | Registry `LazyColumn` sin `key`                                         | Medio     | Abierto  | v1.2.0 | `items(filteredBooks)` sin `key = { it.id }`. El comentario en edición puede pegarse a otra fila al ordenar.                                   |                    |
| FILE-015   | Save/import sin gate de doble tap                                       | Bajo      | Abierto  | v1.2.0 | Dos toques rápidos en Guardar pueden lanzar dos `saveBook()` en paralelo.                                                                      |                    |
| DATA-017   | Unique name carpeta/género: check-then-insert                           | Bajo      | Abierto  | v1.2.0 | El caso del libro queda cubierto por DATA-011. Residual: dos altas de carpeta a la vez.                                                        | DATA-011           |
| DATA-010   | `updateBookWithCrossRefs` sin transacción                               | Medio     | Abierto  | v1.2.0 | Insert sí es `withTransaction`; update no. Si falla el sync, el metadato ya está guardado.                                                     |                    |
| READER-016 | Sesión / WPM se descartan en pausas ≤60s                                | Alto      | Abierto  | —      | `ON_PAUSE` resetea timer y palabras aunque no persista. Rotación no aplica (portrait lock); sí al ir a recents o abrir el diálogo de sesiones. |                    |
| UX-011     | Drawer apila destinos duplicados                                        | Medio     | Abierto  | —      | Solo Home hace `popUpTo`. Library → Registry → Library hincha el back stack.                                                                   | DATA-011           |
| UX-012     | No hay forma de limpiar fechas inicio/fin                               | Medio     | Abierto  | —      | Date picker solo Confirm/Cancel. Registry + Detail.                                                                                            | UX-013             |
| UX-013     | DatePicker puede marcar el día incorrecto                               | Medio     | Abierto  | —      | `initialSelectedDateMillis = currentTimeMillis()` (instant), no medianoche UTC del día local.                                                  | UX-012             |
| UX-020     | Cancelar edit en Detail tira drafts del tab Registry                    | Medio     | Abierto  | —      | El tab Registry edita sin `isEditMode`; Cancel restaura status/rating/fechas/comentario del `book`.                                            |                    |
| READER-019 | Capítulos cortos no reportan progreso                                   | Medio     | Abierto  | —      | Sin scroll no hay `onscroll` → `scrollPosition` queda 0 aunque el capítulo quepa en pantalla.                                                  |                    |
| UX-023     | Overscroll en primer/último capítulo                                    | Bajo      | Abierto  | —      | El affordance se pinta; `OnNext`/`OnPrevious` no-op fuera de rango.                                                                            |                    |
| UX-024     | Barra de controles ignora scroll del capítulo                           | Bajo      | Abierto  | —      | `(chapterIndex+1)/spine.size` → “completo” al empezar el último.                                                                               | READER-019         |
| SEC-004    | EPUB malicioso puede abusar de `AndroidBridge`                          | Medio     | Abierto  | —      | Residual SEC-001: JS del libro puede inflar WPM/progreso y cambiar de capítulo.                                                                | SEC-001            |
| READER-017 | JS stale tras cambio de capítulo                                        | Alto      | Abierto  | —      | El debounce 500 ms del capítulo anterior puede llegar con el estado ya a scroll 0 / palabras 0. (2)                                            |                    |
| READER-008 | TOC con `#fragment`                                                     | Medio     | Diferido | v1.4.0 | `chapter.xhtml#section2` no matchea bien con spine por nombre final → TOC ambigua.                                                             | TAR-31, READER-009 |
| READER-009 | EPUB3 nav incompleto                                                    | Medio     | Diferido | v1.4.0 | NCX parseado; falta soporte completo del HTML Navigation Document (`properties="nav"`).                                                        | TAR-31, TAR-32     |


### Detalle

**(2) READER-017.** Ejemplo: scroll cerca del final → overscroll al siguiente capítulo en menos de 500 ms → el timeout del HTML viejo manda `reportScrollPosition(1.0)` al capítulo nuevo (ya reseteado).

**(3) DATA-011.** Decisión de producto: en pantallas add/update (libro, carpeta, etc.) no se abre el drawer ni se navega a otra vista dejando el formulario a medias. Complemento: si un id del draft ya no existe, omitir la relación, no `insert` por nombre. Con el drawer bloqueado, DATA-017 (resolve al guardar) deja de ser alcanzable.

---

## Anti-patrones (no reintroducir)

Instancias corregidas en v1.2.0 — detalle en [archive/v1.2.0.md](archive/v1.2.0.md). Reglas vivas también en [DATABASE.md](DATABASE.md).


| ID       | Anti-patrón                             | Por qué importa                                        |
| -------- | --------------------------------------- | ------------------------------------------------------ |
| DATA-001 | `@Insert(REPLACE)` para updates         | SQLite DELETE+INSERT → CASCADE borra junction tables   |
| DATA-002 | Cross-refs solo con INSERT IGNORE       | No elimina relaciones quitadas en UI                   |
| DATA-003 | Resolver carpeta/género solo por nombre | Falla tras renombrar; usar `id`                        |
| DATA-004 | `reading_progress` sin FK a books       | Huérfanos al borrar libro (CASCADE desde migración 11) |


---

## Cómo mantener

Ver [MAINTENANCE.md](MAINTENANCE.md).

1. Bug nuevo → fila aquí + fila en [BACKLOG.md](BACKLOG.md) (misma ID).
2. Bug resuelto → quitar fila; añadir a `archive/vX.Y.Z.md`; commit con ID.
3. Chores/features (REL-*, TAR-*) → solo BACKLOG / ROADMAP, no aquí.

