# TAR-50 — Filtro por autor en el registro

- estado: hecho
- version: v1.2.3
- tipo: feat

## Problema

Añadir la opción de filtro por autor en la pantalla de registro.

## Decisión

## Durante el desarrollo

Se añadió una nueva variable calculada al RegistryUiState para obtener la lista de los autores de los libros listados. Se implementó el nuevo filtro por autor para aplicarlo en el `applyCriteria` de `BookCriteria.kt`. Por último, se añadió un nuevo Dropdown al `RegistryFilterSheet.kt` usando la lista de los autores que se le inyecta desde `RegistryScreen.kt`.

## Resolución
