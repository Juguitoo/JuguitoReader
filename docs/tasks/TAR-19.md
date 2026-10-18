# TAR-19 — Recopilación de estadísticas

- estado: en curso
- version: v1.3.0
- tipo: feat

## Problema

El tiempo, el porcentaje y la velocidad solo se guardan cuando el lector WebView cierra una sesión en `daily_reading`. Un libro físico tiene estado, fechas y nota, y no tiene sesión. El motor nativo (TAR-30) tampoco puede escribir esas cifras mientras sigan atadas al puente del WebView.

## Decisión

TAR-19 amplía los hechos que ya existen. No crea una tabla de estadísticas ni la pantalla: eso es TAR-20.

La sesión sigue siendo una fila de `daily_reading` (fecha, tiempo, porcentaje, velocidad), la escriba el lector actual, el físico o más adelante el motor nativo. Un físico terminado ya cuenta por `status` y `endDate`; el tiempo y la velocidad entran cuando haya una sesión registrada.

La forma de anotar esa sesión en un físico, y el contrato para que el motor escriba lo mismo sin pasar por el WebView, se cierran al planear esta tarea.

## Durante el desarrollo

## Resolución
