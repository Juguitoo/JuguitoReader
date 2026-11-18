# TAR-73 — Mejorar menús emergentes de biblioteca, inicio...

- estado: hecho
- version: v1.3.1
- tipo: enhance

## Problema

Los menús de Inicio, Biblioteca, Registro y la ficha del libro son el desplegable plano de Material 3. Algunos se alargan mucho, sobre todo el del libro en la biblioteca, que lista todos los estados uno bajo otro. Se ven básicos y poco alineados con el resto de la app.

## Decisión

Se rehacen esos menús: misma intención, menos lista interminable y un aspecto propio, no el bloque por defecto. El cambio de estado del libro se agrupa para que no ocupe todo el menú. No se añaden acciones nuevas. Si al reorganizar alguna sobra, se decide en el plan.

Quedan fuera los selectores de filtros y los diálogos de confirmación.

## Durante el desarrollo

Apretar el desplegable de Material 3 no bajaba la altura. Cada opción mide como mínimo 48 dp y el texto del menú ya entra a 14 sp, así que cambiar la tipografía no encogía el bloque. Los menús cortos de acciones de un libro pasaron a otra forma. Los de la barra, que llevan texto, se rehicieron con el mismo contenedor propio.

En Inicio, leer, detalles y eliminar son una fila de iconos. Cada icono conserva el nombre para el lector de pantalla y eliminar va en color de error. El menú sale bajo el ancla, alineado al final, y se desplaza si no cabe en la pantalla. Así no tapa el título en las lecturas activas ni se recorta en el libro de la izquierda del podio.

En la biblioteca el menú del libro es una hoja inferior. El desplegable apilaba detalles y los cuatro estados, y era el que se alargaba. La hoja enseña el título y el autor, para saber de qué libro es, y detalles y eliminar como iconos. El título cede el ancho a esos iconos y se corta si no cabe. El estado queda en su propia fila.

Esa fila es el mismo componente en la hoja y en el diario de la ficha. Antes era un grupo de chips con el nombre entero y, en español, saltaba de línea en los dos sitios. Ahora cada estado cerrado es su icono y solo el seleccionado se abre con el nombre completo. El orden no cambia al pulsar: pendiente, leyendo, finalizado y abandonado. La animación de abrir y cerrar se dejó como está: se entiende, aunque el fundido y el ensanchado no duran lo mismo.

Eliminar desde la biblioteca es la excepción a no añadir acciones. El borrado ya existía en Inicio y en la ficha. Aquí se usa el deshacer de Inicio, no el diálogo: la hoja se cierra, el libro sale de la lista y un aviso corto permite restaurarlo con sus lecturas, el progreso y los marcadores. Si el aviso se va, entonces se borran la portada, el EPUB y la caché. La ficha sigue pidiendo confirmación y borra los ficheros en el momento, porque al confirmar se abandona la pantalla. Biblioteca tiene su propia ventana de deshacer y no comparte la de Inicio.

Los menús de la barra de Biblioteca y de Registro, y el de la ficha, son un bloque con icono y texto, la misma superficie y el mismo borde. No son una fila de solo iconos: importar, el gestor, la vista, editar y eliminar se leen por el texto. El selector de estado dentro del registro, los filtros y los selectores de Ajustes siguen siendo desplegables. Los menús de marcadores del lector no entran en este cambio.

## Resolución

Los menús de libro dejan de ser el desplegable de Material 3. En Inicio las acciones son una fila de iconos. En la biblioteca, una hoja muestra el título, el estado en una sola línea y el borrado con deshacer. En la ficha el borrado sigue pidiendo confirmación. Los menús de las barras de Biblioteca, Registro y la ficha usan el mismo bloque propio.

Commits: `5818147`
