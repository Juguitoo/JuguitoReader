# TAR-78 — Progreso opcional de un libro físico

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

Un físico guarda título, autor, estado, fechas y nota. No hay sitio para el número de páginas, los capítulos o la página por la que se va. Quien quiera ese detalle no puede, y quien no quiera no debería tener que rellenarlo.

El porcentaje que muestran el inicio y la biblioteca sale de `reading_progress`: índice de capítulo y scroll dentro del capítulo. En un físico esa fila no existe. La biblioteca oculta el porcentaje. El libro en curso del inicio, si no hay fila, enseña 0 %.

## Decisión

El progreso de papel es opcional y vive en `reading_progress`, la misma fila 1:1 del libro. No se crea otra tabla y no se parte `books`. El físico sigue siendo un libro con `is_physical`. Una tabla aparte también podría ocultar el `if` detrás de un tipo común. Se deja una sola porque el progreso ya es una fila por libro, y el DAO y el deshacer siguen teniendo un solo sitio.

Se añaden columnas nulables: página actual, páginas totales, capítulo actual y capítulos totales. El índice de capítulo, el scroll y el total de capítulos del EPUB no cambian de significado. Una fila ya guardada de un EPUB se queda igual, con las columnas de papel vacías. Si el físico no rellena nada, puede no tener fila, como un EPUB que aún no se ha abierto.

En dominio, `ReadingProgress` tiene dos formas y el porcentaje se pide a la base. El EPUB lo calcula como ahora, con el capítulo y el scroll. El físico lo calcula con la página actual entre las páginas totales, y solo cuando las dos están. Si falta una, no hay porcentaje: no se pinta un 0 %. Inicio, biblioteca y ficha leen ese porcentaje. No eligen un caso de uso según `is_physical`.

El lector construye la forma del EPUB, porque esa pantalla solo abre un EPUB. El formulario del físico construye la otra. La única traducción de columnas a una u otra forma está en el mapper, y usa `is_physical` del libro. No se añade una segunda columna de tipo: podría contradecir a la del libro. Room no instancia subclases. La entidad lleva todas las columnas y el mapper arma el tipo.

Hay dos sitios para escribirlo. En la ficha del libro se editan página, páginas totales y capítulos, junto a las sesiones de TAR-75. Al parar el cronómetro (TAR-74), un diálogo breve pregunta solo por la página actual. Se puede omitir. Si responde, se guarda esa página y nada más: los totales no se piden ahí.

Guardar pasa por el mismo caso de uso que ya actualiza `reading_progress`. Si el porcentaje llega a 100 y la opción de terminar al acabar está activa, el libro puede pasar a terminado, igual que un EPUB.

El tiempo del cronómetro y el registro del día siguen en `daily_reading`. Ese porcentaje es la foto de la sesión, no el avance de páginas.

## Durante el desarrollo

## Resolución
