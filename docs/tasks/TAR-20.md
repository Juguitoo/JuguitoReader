# TAR-20 — Sección estadísticas

- estado: en curso
- version: v1.3.0
- tipo: feat

## Problema

No hay una pantalla de estadísticas. En Inicio hay tres cuentas (biblioteca, leyendo, terminados) calculadas en el propio Home. La velocidad está prevista y no se muestra. El tiempo de lectura vive en las sesiones de cada libro y no se ve junto.

## Decisión

Cuarto destino de la barra: Inicio, Biblioteca, Registro, Estadísticas. La pantalla observa los libros y `daily_reading` y agrega en dominio. No hay tabla nueva.

Lo histórico no depende del periodo: terminados, racha y su récord, nota media de los terminados que tienen nota, y el género más leído (porcentaje de terminados que lo llevan). El periodo (semana de lunes a domingo, mes, año o todo; por defecto el mes) trae los libros terminados en esas fechas, para enseñarlos en una fila de portadas. El número de terminados es el tamaño de esa lista. También filtra tiempo, velocidad, días con lectura, barras de minutos y el género de ese periodo. El tiempo de ahora se compara con los mismos días ya transcurridos del periodo anterior. En «todo» no hay comparación. La racha llega hasta hoy, o hasta ayer si hoy aún no hay sesión.

Un físico entra en las cuentas y en los terminados del periodo. Su tiempo queda en cero hasta que TAR-19 registre la sesión. Home no se reescribe en esta tarea.

## Durante el desarrollo

## Resolución
