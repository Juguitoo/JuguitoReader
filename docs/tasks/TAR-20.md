# TAR-20 — Sección estadísticas

- estado: hecho
- version: v1.3.0
- tipo: feat

## Problema

No hay una pantalla de estadísticas. En Inicio hay tres cuentas (biblioteca, leyendo, terminados) calculadas en el propio Home. La velocidad está prevista y no se muestra. El tiempo de lectura vive en las sesiones de cada libro y no se ve junto.

## Decisión

Cuarto destino de la barra: Inicio, Biblioteca, Registro, Estadísticas. La pantalla observa los libros y `daily_reading` y agrega en dominio. No hay tabla nueva.

El selector ofrece semana, mes y año, y abre en el mes. La racha actual llega hasta hoy, o hasta ayer si hoy no hay sesión, aunque se mire otro periodo. El récord es la racha más larga de todas las sesiones. El tiempo, la velocidad, los terminados del periodo y el género de ese periodo sí siguen el filtro. El tiempo se compara con los mismos días ya transcurridos del periodo anterior.

Arriba van la racha, el tiempo leído, los terminados y la velocidad media. Las páginas por hora salen de esa velocidad con una página estándar de 250 palabras. La gráfica de minutos lleva escala. El calendario muestra el mes del periodo: en semanal, el mes en el que empieza la semana; en un año ya cerrado, diciembre; en el año en curso, el mes de hoy. Debajo, la fila de terminados del periodo. Más abajo, el género más leído de todos los terminados, el género de ese periodo y la nota media de los terminados que tienen nota.

Un físico entra en las cuentas y en los terminados del periodo. Su tiempo queda en cero hasta que exista una sesión en `daily_reading`. Home no se reescribe en esta tarea.

## Durante el desarrollo

El selector no incluye el histórico. El momento del día no se guarda, así que no hay «momento favorito», y la velocidad no se repite debajo de las tarjetas. Las tarjetas usan el fondo de la pantalla y un borde con el color primario del tema.

## Resolución

Pantalla de estadísticas como cuarto destino, con semana, mes y año. Arriba, la racha hasta hoy y su récord, el tiempo leído frente al periodo anterior, los terminados y la velocidad media, con páginas por hora a 250 palabras por página. Gráfica de minutos con escala, calendario del mes del periodo y fila de terminados. Abajo, el género de siempre, el del periodo y la nota media. Sin tabla nueva: agrega libros y sesiones. El tiempo de un físico cuenta cuando hay una sesión.
