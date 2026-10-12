# TAR-66 — Rediseño del Home

- estado: pendiente
- version: v1.3.0
- tipo: enhance

## Problema

El Home enseña favoritos, lectura en curso y pendientes con la misma estantería. Volver al libro que se está leyendo pesa igual que el resto, y favoritos no se distinguen de la cola de pendientes.

## Decisión

Arriba, un bloque «Ahora»: el libro leído más recientemente en grande, con el avance y Continuar. Los demás en curso van debajo, pequeños, dentro de ese mismo bloque.

Favoritos sigue en estantería de madera y es la única fila alta. El nombre se queda en Favoritos. Pendientes conserva la estantería, más baja. El orden es Ahora, Favoritos, Pendientes.

Un favorito físico abre la ficha. Importar sigue en la barra. La biblioteca y la ficha no entran en esta tarea.

## Durante el desarrollo

La decisión final que se tomó es seguir la siguiente estructura:
- Ultima lectura activa
- Fila con otras lecturas activas
- Podio para los libros favoritos
- Lista vertical con libros pendientes
A lo largo de la interfaz se han distribuido varios botones para ir a la biblioteca con filtros preseleccionados para ver todos los libros pendientes, favoritos y activos.
También durante el desarrollo se descubrió que la tipografía Lora que se estaba usando no se estaba extrayendo correctamente y se estaba utilizando una diferente. Y se cambió también la tipografía Inter por Plus Jakarta Sans.
Se optó por una interfaz más minimalista con los libros al mismo nivel que el fondo de la app sin tarjetas que los rodeen.

## Resolución

Los cambios se desarrollaron correctamente, queda pendiente la mejora de los filtros de la biblioteca. Para ver los detalles leer la sección previa.
