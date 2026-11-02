# TAR-72 — Sistema de randomización de siguiente lectura en TBR

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

La lista TBR (TAR-67) acumula lo que se quiere leer. Elegir el siguiente a mano, cuando la lista crece, es justo lo que un sorteo evita.

## Decisión

Un botón devuelve un ítem al azar de toda la lista, con la misma probabilidad. No lo quita de la TBR ni lo pasa a la biblioteca.

Filtros por género, pesos o excluir el último salen después, cuando TAR-67 cierre qué datos tiene un ítem. Esta versión no los anticipa. Si la lista está vacía, no hay nada que sortear.

## Durante el desarrollo

## Resolución
