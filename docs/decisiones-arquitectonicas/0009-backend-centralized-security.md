# ADR-09: Seguridad – Autenticación Centralizada en el Backend

## Estado
Aceptado

## Contexto
El sistema maneja credenciales y sesiones/tokens del cliente, por lo que requiere un mecanismo seguro y centralizado de autenticación y autorización.

## Decisión
Gestionar la autenticación y autorización en el backend mediante un componente de seguridad (por ejemplo, AuthController + SecurityConfig con Spring Security y JWT).

## Alternativas
- Autenticación solo del lado cliente.
- Credenciales sin cifrado.

## Justificación
Reduce riesgos de seguridad y centraliza el control de acceso a los recursos protegidos.

## Consecuencias
- Se deben definir políticas de expiración de tokens.
- Protección adecuada de endpoints.
- Almacenamiento seguro de credenciales.
