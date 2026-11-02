# TAR-75 — Registro de sesiones de lecturas para libros físicos y digitales

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

Aunque exista el cronómetro del físico (TAR-74), no hay forma de anotar una lectura después de haberla hecho. Tampoco se puede corregir el día que el lector ya guardó, ni apuntar un día leído fuera de la app.

## Decisión

Desde la ficha del libro se edita el día de lectura, en físico y en digital. Esa pantalla tiene que ganar la edición de sesiones. El progreso por páginas del físico va en la misma ficha y es TAR-78: esta tarea no escribe esas columnas. Sigue siendo una fila de `daily_reading` por libro y fecha. No hay una segunda sesión el mismo día.

Ese día se puede completar de dos maneras. Si el lector ya escribió la fila, se corrige o se completa el tiempo. Si el día no existe, se crea: una lectura fuera de la app, o un físico que no pasó por el cronómetro.

Guardar puede sumar un tramo al tiempo del día o dejar el total corregido. No borra el porcentaje ni la velocidad que el lector hubiera guardado.

El cronómetro en vivo es TAR-74. Esta tarea no cambia el estado del libro ni abre varias sesiones en la misma fecha.

## Durante el desarrollo

## Resolución
