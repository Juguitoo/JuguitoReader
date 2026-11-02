# TAR-70 — Diálogo de rachas con animación currada

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

La racha ya se calcula y se ve en Estadísticas. Cerrar una lectura no la celebra: no hay un momento en el que se note que el día ha contado o que se ha igualado el récord.

## Decisión

Al guardar una sesión, si ese día empieza a contar y la racha sube, o si la racha iguala el récord, se muestra un diálogo con animación. Si hoy ya contaba, guardar otro tramo no vuelve a abrirlo.

El cálculo sigue el de TAR-20: días con sesión, no el número de veces que se abre el lector. El diálogo no cambia la racha ni las estadísticas. Sirve igual si la fila del día la escribe el lector, el cronómetro del físico (TAR-74) o el registro manual (TAR-75), siempre que sea ese guardado el que hace subir la racha.

## Durante el desarrollo

## Resolución
