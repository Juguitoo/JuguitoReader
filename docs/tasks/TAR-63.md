# TAR-63 — Pase de modo oscuro (Classic + Neon)

- estado: hecho
- version: v1.3.0
- tipo: enhance

## Problema

Classic Dark tenía el fondo más claro que las tarjetas y un acento demasiado oscuro para leerse encima. Neon pintaba los contenedores casi del mismo negro que el fondo, así que desaparecían. En ambos, varios textos iban en blanco fijo sobre un color que ya no era oscuro.

## Decisión

Classic Dark toma los roles de color de Warm Editorial Night, tal como están en el bloque de tokens: cada `on*` va emparejado con su fondo. Neon conserva azul, violeta y rosa, con contenedores más claros y texto blanco encima. Donde un componente forzaba blanco o negro, pasa a usar el rol del color que tiene detrás.

## Durante el desarrollo

El texto narrativo del documento pide terracota `#e06d44` con texto blanco en el botón. Ese par no llega al contraste de texto normal. El botón usa `primary` `#ffb59c` con `onPrimary` `#5c1900`, y la terracota queda en `primaryContainer`.

## Resolución

Classic Dark y Neon definen el esquema completo, incluidas las superficies por capas. Barras del lector, día seleccionado del calendario, badges de estado, tarjetas del Home, chips, interruptores y la estantería de carpetas leen el color del tema en vez de un blanco o un negro fijos.
