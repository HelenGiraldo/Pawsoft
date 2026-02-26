# ADR 0001: Arquitectura Base del Proyecto

## Estado
Aceptado

## Contexto
Se necesita definir una arquitectura clara y escalable para el proyecto que permita:

- Separación de responsabilidades
- Fácil mantenimiento
- Escalabilidad futura
- Pruebas unitarias claras

## Decisión
Se adopta una arquitectura basada en capas:

- Controllers → Manejan las solicitudes
- Services → Contienen la lógica de negocio
- Repositories → Acceso a datos
- Models → Definición de entidades

La estructura del proyecto será:

