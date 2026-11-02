# TAR-51 — Marcadores en libros

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

El lector guarda un solo punto de lectura. No hay forma de dejar varios sitios de un EPUB para volver a ellos más tarde.

## Decisión

Marcadores solo en digitales: varios por libro, cada uno con el capítulo y el punto de lectura. Desde la lista se vuelve a ese sitio. No sustituyen el progreso actual; el punto de lectura sigue donde el lector lo deja al cerrar.

Cada marcador puede llevar un nombre corto, para distinguir dos del mismo capítulo. Un físico no tiene visor, así que no entra. Tampoco es un subrayado ni una nota al margen.

## Durante el desarrollo

## Resolución
