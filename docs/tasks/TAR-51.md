# TAR-51 — Marcadores en libros

- estado: hecho
- version: v1.3.1
- tipo: feat

## Problema

El lector guarda un solo punto de lectura. No hay forma de dejar varios sitios de un EPUB para volver a ellos, ni de marcar una frase en el visor, ni de asociarle un texto escrito por el lector.

## Decisión

Solo en digitales. Ninguna marca lleva nombre. Desde la lista se vuelve a ese sitio. Hay cuatro: marcador de posición, subrayado, nota e imagen pineada.

El marcador guarda el capítulo y la frase del inicio de la pantalla. En el visor se ve una cinta a la derecha de esa frase. El texto no se subraya. En la lista se enseña esa zona. Si no hay frase, no se guarda.

El subrayado guarda la frase elegida y un color de la paleta de la app: ámbar, verde, azul, rosa y lila. En el visor esa frase queda marcada. En la lista se enseña la frase. No lleva texto del usuario.

La nota es un subrayado con un texto escrito por el usuario. Misma frase y mismo color. En la lista se enseña la frase, hasta dos líneas, y el texto de la nota.

Una imagen del capítulo, como un mapa, es una cuarta marca. Se pinea con una pulsación larga y se guarda el src, sin copiar el fichero. Lleva un texto opcional, del mismo modo que la nota. En la hoja tiene su propia pestaña y se abre sin cambiar de capítulo.

El ancla que se guarda es el href del capítulo y la frase, con un poco de contexto delante y detrás. En el marcador, esa frase es la del inicio de la pantalla. En el subrayado y en la nota, es la frase elegida. En la imagen, el ancla es el src. El color y el texto escrito por el lector son datos y no dependen del visor. El porcentaje de scroll puede guardarse como atajo para el visor actual. El motor nativo resolverá la misma marca buscando la frase o, si es una imagen, el src. La captura y el pintado se escriben en cada visor.

Crear o borrar una marca no sustituye el punto de lectura. Abrir una marca de otro capítulo cambia de capítulo, igual que el índice, y el visor busca la frase. Si la marca está en el capítulo abierto, el punto de lectura no se mueve.

## Durante el desarrollo

La marca vive en una sola tabla. El tipo distingue el marcador de posición, el subrayado, la nota y la imagen pineada. El visor pinta la frase al abrir el capítulo: una cinta en el marcador, el color de la paleta en el subrayado y en la nota, y una chincheta junto a la imagen. El texto de la nota y el de la imagen se escriben en un diálogo y no entran en el HTML.

La lista es una hoja a pantalla completa con dos pestañas, Marcadores e Imágenes. El menú de selección sustituye al del sistema y deja a la vista copiar, compartir y seleccionar todo, junto a resaltar, anotar y borrar.

## Resolución

El lector guarda marcadores, subrayados, notas e imágenes pineadas, y vuelve a ellos desde la misma hoja. Deshacer el borrado del libro en Inicio restaura las marcas. Sustituir el EPUB las borra con el progreso.

Commits: `3bb1b69` `da599ef` `02b24c6`
