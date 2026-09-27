# TAR-56 — Colapsar sección gestión en drawer

- estado: hecho
- version: v1.2.3
- tipo: feat

## Problema

Cambiar el drawer de la app para que los items de `Crear libro`, `Nueva carpeta` y `Crear género` sean hijos de `Gestor de contenido` y se puedan colapsar y expandir.

## Decisión

Editar la estructura de `JuguitoApp.kt` para que los 3 items hijos pasen a un `AnimatedVisibility` y que el item principal tenga un nuevo `IconButton` para enseñar o ocultar los hijos.

## Durante el desarrollo

Se ha creado una variable para almacenar el estado de la visibilidad de los hijos, se ha cubierto el DrawerItem del Gestor de contenidos en una Row y una Box y se ha añadido un IconButton para poder cambiar el estado de la variable. A su vez, se han envuelto los hijos en un AnimatedVisibility que cambia según la variable que guarda el estado.

## Resolución

El drawer de la aplicación ahora permite colapsar y expandir la sección de gestión de contenidos. De base está expandido.
