# READER-008 — TOC con `#fragment` falla

- estado: pendiente
- version: v1.4.0
- tipo: fix
- severidad: media

Diferido a la compatibilidad EPUB3 (TAR-31). No es trabajo de la 1.2.x.

## Problema

Un destino del índice con fragmento, como `chapter.xhtml#section2`, no encaja bien con el spine cuando se compara solo por el nombre final del archivo. El índice queda ambiguo.

Va junto a READER-009 y a TAR-31.

## Durante el desarrollo

## Resolución
