# ADR-02: Selección de Base de Datos

## Estado
Aceptado

## Contexto
El sistema requiere persistencia de datos estructurados con relaciones claras entre entidades.

## Decisión
Se utilizará una base de datos relacional (por ejemplo, MySQL o PostgreSQL).

## Alternativas
- Base de datos NoSQL (MongoDB).
- Persistencia en archivos locales.

## Justificación
Las bases relacionales permiten integridad referencial, consistencia y consultas estructuradas mediante SQL.

## Consecuencias
- Mayor control sobre relaciones entre datos.
- Requiere diseño previo del modelo relacional.
