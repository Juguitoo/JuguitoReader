# TAR-77 — Aviso de racha a mitad del día

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

La racha se ve en Estadísticas y, al subir, TAR-70 la celebra. No hay un aviso a tiempo si el día va a cerrarse sin sesión y la racha se va a romper.

## Decisión

Notificación local, sin servidor, a mitad del día. Salta solo si hay una racha viva y hoy todavía no existe fila en `daily_reading`. Si hoy ya hay sesión, o la racha está en cero, no se envía. La hora concreta se cierra al planearla.

Es un ajuste que el usuario enciende. Al encenderlo se pide el permiso normal de notificaciones, el mismo que puede usar TAR-76. Si lo rechaza o apaga el aviso, el resto de la app no cambia. No sustituye al diálogo de TAR-70.

## Durante el desarrollo

## Resolución
