# TAR-69 — Cambiar la navegación en drawer por un bottomNavigation

- estado: en curso
- version: v1.3.0
- tipo: feat

## Problema

Cambiar el sistema de navegación en formato drawer por uno clásico y reestructurar las pantallas de navegación y botones.

## Decisión

La barra inferior tiene Inicio, Biblioteca y Registro. Estadísticas entra como cuarto destino cuando exista TAR-20. El gestor se abre desde el menú de Biblioteca y de Registro. Ajustes se abre desde Inicio, y About y la donación viven dentro de Ajustes. La barra se oculta en el resto de pantallas y también mientras el teclado está abierto. El índice del lector conserva su drawer.

## Durante el desarrollo

Se cambió el modelo de navegación correctamente y además se aprovechó para realizar un cambio visual a las topBar de cada pantalla.

## Resolución
