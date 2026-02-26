# ADR-05: Manejo Global de Errores

## Estado
Aceptado

## Contexto
La aplicación debe manejar errores de manera uniforme y clara para el cliente.

## Decisión
Se implementará un manejador global de excepciones (por ejemplo, usando @ControllerAdvice en Spring).

## Alternativas
- Manejo manual de errores en cada controlador.
- No manejar excepciones explícitamente.

## Justificación
Centralizar el manejo de errores mejora consistencia y claridad en respuestas HTTP.

## Consecuencias
- Respuestas uniformes ante errores.
- Mayor control sobre códigos HTTP.
