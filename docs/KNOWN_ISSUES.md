# Errores y riesgos conocidos — JuguitoReader

Registro **vivo** de bugs, riesgos y anti-patrones abiertos. Solo bugs/errores/riesgos (no chores de release ni features). Histórico resuelto: [archive/](archive/).

Consultar antes de tocar Room, reader, importación o relaciones M:N.

---

## Issues abiertos

Una sola tabla. Orden: sin versión (`—`) → diferidos. Si el comentario se queda corto, detalle bajo la tabla e indicarlo con un (1) o el número que le siga al último detalle.


| ID         | Título                                                                  | Severidad | Estado   | Target | Comentario                                                                                                                                     | Relacionado        |
| ---------- | ----------------------------------------------------------------------- | --------- | -------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| UX-020     | Cancelar edit en Detail tira drafts del tab Registry                    | Medio     | Abierto  | —      | El tab Registry edita sin `isEditMode`; Cancel restaura status/rating/fechas/comentario del `book`.                                            |                    |
| UX-023     | Overscroll en primer/último capítulo                                    | Bajo      | Abierto  | —      | El affordance se pinta; `OnNext`/`OnPrevious` no-op fuera de rango.                                                                            |                    |
| UX-024     | Barra de controles ignora scroll del capítulo                           | Bajo      | Abierto  | —      | `(chapterIndex+1)/spine.size` → “completo” al empezar el último.                                                                               | READER-019         |
| SEC-004    | EPUB malicioso puede abusar de `AndroidBridge`                          | Medio     | Abierto  | —      | Residual SEC-001: JS del libro puede inflar WPM/progreso y cambiar de capítulo.                                                                | SEC-001            |
| READER-017 | JS stale tras cambio de capítulo                                        | Alto      | Abierto  | —      | El debounce 500 ms del capítulo anterior puede llegar con el estado ya a scroll 0 / palabras 0. (2)                                            |                    |
| READER-008 | TOC con `#fragment`                                                     | Medio     | Diferido | v1.4.0 | `chapter.xhtml#section2` no matchea bien con spine por nombre final → TOC ambigua.                                                             | TAR-31, READER-009 |
| READER-009 | EPUB3 nav incompleto                                                    | Medio     | Diferido | v1.4.0 | NCX parseado; falta soporte completo del HTML Navigation Document (`properties="nav"`).                                                        | TAR-31, TAR-32     |


### Detalle

**(2) READER-017.** Ejemplo: scroll cerca del final → overscroll al siguiente capítulo en menos de 500 ms → el timeout del HTML viejo manda `reportScrollPosition(1.0)` al capítulo nuevo (ya reseteado).

---

## Anti-patrones (no reintroducir)

Instancias corregidas en v1.2.0 — detalle en [archive/v1.2.0.md](archive/v1.2.0.md). Reglas vivas también en [DATABASE.md](DATABASE.md).


| ID       | Anti-patrón                             | Por qué importa                                        |
| -------- | --------------------------------------- | ------------------------------------------------------ |
| DATA-001 | `@Insert(REPLACE)` para updates         | SQLite DELETE+INSERT → CASCADE borra junction tables   |
| DATA-002 | Cross-refs solo con INSERT IGNORE       | No elimina relaciones quitadas en UI                   |
| DATA-003 | Resolver carpeta/género solo por nombre | Falla tras renombrar; usar `id`                        |
| DATA-011 | Insertar por nombre si el id ya no existe | Resucita carpetas/géneros borrados; omitir la relación |
| DATA-004 | `reading_progress` sin FK a books       | Huérfanos al borrar libro (CASCADE desde migración 11) |


---

## Cómo mantener

Ver [MAINTENANCE.md](MAINTENANCE.md).

1. Bug nuevo → fila aquí + fila en [BACKLOG.md](BACKLOG.md) (misma ID).
2. Bug resuelto → quitar fila; añadir a `archive/vX.Y.Z.md`; commit con ID.
3. Chores/features (REL-*, TAR-*) → solo BACKLOG / ROADMAP, no aquí.

