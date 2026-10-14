# TAR-49 — Controles biblioteca (filtros + ordenación)

- estado: en curso
- version: v1.3.0
- tipo: feat

## Problema

Los filtros de la pantalla de biblioteca es bastante pobre y visualmente los filterChips no pegan con la estética de la pantalla.

## Decisión

Hacer un refactor de la ficha de filtros ya existentes en registro y reutilizarla en la pantalla de libreria.

## Durante el desarrollo

Se hizo el refactor para que el componente fuera reutilizable y se limpió todo el sistema antiguo que utilizaba `LibraryScreen` mediante eventos. También se tuvieron que realizar cambios en el LibraryViewmodel para que los filtros enviados desde la navegación de la pantalla Home se reflejen en la ficha de filtros actual. Durante el desarrollo se tomó la decisión de separar lógicamente el comportamiento de los filtros para los libros, por una parte se aplicarán los filtros referidos a atributos y por otra parte los filtros referidos a la ordenación de los libros.

## Resolución

El refactor se realizó correctamente y se conectó a la lógica de la libreria, también se hicieron unos cambios visuales y la funcionalidad de filtrado por favoritos se queda integrado en la ficha general de filtros, se ha eliminado el botón directo.
