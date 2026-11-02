# TAR-51 — Marcadores en libros

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

El lector guarda un solo punto de lectura. No hay forma de dejar varios sitios de un EPUB para volver a ellos, ni de marcar una frase en el visor, ni de asociarle un texto escrito por el lector.

## Decisión

Solo en digitales. Al cerrar, el punto de lectura sigue siendo el progreso actual. Hay tres marcas y ninguna lleva nombre. Desde la lista se vuelve a ese sitio.

El marcador guarda el capítulo y la posición del inicio de la pantalla. En el visor se ve un icono de bookmark a la derecha de esa posición. El texto no se subraya. En la lista se enseña el texto de esa zona, hasta dos líneas.

El subrayado guarda la frase elegida y un color de una paleta fija. Los colores concretos se cierran al planearla. En el visor esa frase queda marcada. En la lista se enseña la frase, hasta dos líneas. No lleva texto del usuario.

La nota es un subrayado con un texto escrito por el usuario. Misma frase y mismo color. En la lista se enseña la frase, hasta dos líneas, y el texto de la nota.

El ancla que se guarda es el href del capítulo y la frase, con un poco de contexto delante y detrás. En el marcador, esa frase es la del inicio de la pantalla. En el subrayado y en la nota, es la frase elegida. El color y el texto de la nota son datos y no dependen del visor. El porcentaje de scroll puede guardarse como atajo para el visor actual. El motor nativo resuelve la marca buscando la frase. La captura y el pintado se escriben en cada visor.

## Durante el desarrollo

## Resolución
