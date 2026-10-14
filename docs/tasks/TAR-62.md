# TAR-62 — Biblioteca: portadas rectangulares / look más libro

- estado: hecho
- version: v1.3.0
- tipo: enhance

## Problema

La biblioteca no da sensación de un expositor de los libros y hay algún error visual de solapamiento de badges.

## Decisión

Hacer que los libros tengan los cantos rectangulares, poner líneas divisoras entre fila y fila de libros, añadir algún efecto visual como en home a los libros y solucionar los solapamientos visuales.

## Durante el desarrollo

Se han cambiado las portadas con los cantos rectos y se ha implementado 2 disposiciones diferentes de los libros, una la ya existente en formato grid/matriz y otra distinta en formato fila por libro, con información relevante como titulo, autor, géneros, estado y en caso de leyendo el progreso. La preferencia de interfaz se añadió a las preferencias almacenadas en el DataStore y se aseguro que se almacenaba correctamente en el sistema de backups.

## Resolución
