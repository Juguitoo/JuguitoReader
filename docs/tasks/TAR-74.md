# TAR-74 — Modo lectura para libros en físico

- estado: pendiente
- version: v1.3.1
- tipo: feat

## Problema

Un libro físico tiene estado, fechas y nota, y no hay forma de contar el tiempo mientras se lee. Las estadísticas leen `daily_reading`, así que sin esa fila el tiempo del físico se queda en cero.

## Decisión

Pantalla con cronómetro para un libro físico. Al parar, los minutos de esa sentada se suman a la fila de `daily_reading` de ese libro y de ese día. Si el día ya tenía fila, se acumula el tiempo. No se abre otra sesión.

El cronómetro aporta el tiempo. No cambia el estado ni el porcentaje de la sesión: si la fila del día ya tenía porcentaje o velocidad, se quedan, y si es nueva no se inventan.

Al parar, antes de cerrar, un diálogo breve pregunta por qué página va. Se puede omitir, y el tiempo se guarda igual. Si responde, esa página actualiza el progreso del físico (TAR-78). No pide capítulos ni totales: eso se edita en la ficha del libro.

El cronómetro cuenta mientras la pantalla está abierta. No cambia el No molestar del sistema: eso pide un acceso especial en Ajustes y queda fuera. Una notificación con el tiempo en segundo plano es TAR-76.

El registro a mano de un día, también de un digital, es TAR-75. Las dos escriben la misma fila.

## Durante el desarrollo

## Resolución
