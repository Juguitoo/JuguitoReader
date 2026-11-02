# TAR-76 — Notificación del cronómetro en segundo plano

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

TAR-74 cuenta el tiempo de un físico solo con la pantalla abierta. Al salir de la app, la sentada desaparece y no hay forma de verla ni de seguirla.

## Decisión

Al dejar la app con el cronómetro en marcha, una notificación local muestra la sentada y el tiempo sigue contando. Al volver, es la misma sesión. Al parar, los minutos entran en la fila del día como en TAR-74.

El permiso es el de notificaciones (`POST_NOTIFICATIONS`), el diálogo normal del sistema. Se pide cuando el usuario va a dejar el cronómetro corriendo fuera de la pantalla. Si lo rechaza, el cronómetro sigue valiendo con la pantalla abierta y, al salir, se pausa.

Mantener el reloj con la app cerrada implica un servicio en primer plano. No es el acceso especial de No molestar, que sigue fuera. En Play, ese servicio hay que declararlo.

## Durante el desarrollo

## Resolución
