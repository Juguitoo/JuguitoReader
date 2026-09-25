# Anti-patrones — JuguitoReader

Reglas que no hay que reintroducir. Los bugs abiertos no se listan aquí: la línea está en [BACKLOG.md](BACKLOG.md) y el detalle en [tasks/](tasks/).

Consultar antes de tocar Room, el reader, la importación o las relaciones M:N. Histórico de cuando se corrigieron: [archive/v1.2.0.md](archive/v1.2.0.md). Reglas de datos también en [DATABASE.md](DATABASE.md).

## Anti-patrones

| ID       | Anti-patrón                               | Por qué importa                                        |
| -------- | ----------------------------------------- | ------------------------------------------------------ |
| DATA-001 | `@Insert(REPLACE)` para updates           | SQLite DELETE+INSERT → CASCADE borra junction tables   |
| DATA-002 | Cross-refs solo con INSERT IGNORE         | No elimina relaciones quitadas en UI                   |
| DATA-003 | Resolver carpeta/género solo por nombre   | Falla tras renombrar; usar `id`                        |
| DATA-011 | Insertar por nombre si el id ya no existe | Resucita carpetas/géneros borrados; omitir la relación |
| DATA-004 | `reading_progress` sin FK a books         | Huérfanos al borrar libro (CASCADE desde migración 11) |

## Cómo mantener

Ver [MAINTENANCE.md](MAINTENANCE.md).

1. Bug nuevo → línea en [BACKLOG.md](BACKLOG.md) y ficha `tasks/{ID}.md` con el problema. No se añade una fila aquí.
2. Bug resuelto → quitar la línea del backlog, escribir la resolución en la ficha (`estado: hecho`) y una fila en `archive/vX.Y.Z.md`.
3. Estas reglas se quedan aunque el caso original esté cerrado. Features y chores no entran en este archivo.
