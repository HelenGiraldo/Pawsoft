# ADR-03: Estrategia de Autenticación

## Estado
Aceptado

## Contexto
El sistema requiere proteger recursos y garantizar que solo usuarios autenticados accedan a ciertas funcionalidades.

## Decisión
Se implementará autenticación basada en JWT (JSON Web Tokens).

## Alternativas
- Autenticación basada en sesiones tradicionales.
- Autenticación solo del lado cliente.

## Justificación
JWT permite autenticación stateless, escalable y adecuada para APIs REST.

## Consecuencias
- Necesidad de gestionar expiración de tokens.
- Implementación de filtros de seguridad.
