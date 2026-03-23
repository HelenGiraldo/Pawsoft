# Modelo de Base de Datos — PawSoft

Base de datos relacional MySQL gestionada con Spring Data JPA / Hibernate.

---

## Relaciones entre tablas

```
┌─────────────────────┐         ┌─────────────────────────┐
│        users        │         │          pets           │
│─────────────────────│         │─────────────────────────│
│ id (PK)             │         │ id (PK)                 │
│ name                │         │ name                    │
│ email (UK)          │         │ species                 │
│ password            │         │ breed                   │
│ primer_acceso       │         │ birth_date              │
│ role                │         │ sex                     │
│ failed_attempts     │         │ owner_email             │
│ lock_time           │         │ photo_url               │
│ two_factor_code     │         └──────────┬──────────────┘
│ two_factor_expiry   │                    │
│ enabled             │                    │ pet_id
│ photo_url           │                    │
│ phone               │         ┌──────────▼──────────────┐
└──────┬──────────────┘         │      appointments       │
       │                        │─────────────────────────│
       │ client_id              │ id (PK)                 │
       ├───────────────────────►│ date                    │
       │ vet_id                 │ time                    │
       ├───────────────────────►│ reason                  │
       │                        │ status                  │
       │                        │ client_id (FK → users)  │
       │                        │ vet_id    (FK → users)  │
       │                        │ pet_id    (FK → pets)   │
       │                        │ notes                   │
       │                        │ cancel_reason           │
       │                        └──────────┬──────────────┘
       │                                   │ appointment_id
       │                        ┌──────────▼──────────────┐
       │                        │        payments         │
       │                        │─────────────────────────│
       │                        │ id (PK)                 │
       │                        │ appointment_id (nullable)│
       │                        │ client_name             │
       │                        │ client_email            │
       │                        │ pet_name                │
       │                        │ vet_name                │
       │                        │ appointment_date        │
       │                        │ appointment_time        │
       │                        │ concept                 │
       │                        │ base_amount             │
       │                        │ amount                  │
       │                        │ status                  │
       │                        │ payment_date            │
       │                        │ received_by             │
       │                        │ notes                   │
       │                        │ created_at              │
       │                        └─────────────────────────┘

       │
       ├──────────────────────────────────────────────────┐
       │                                                  │
       │  user_id                              user_id    │
┌──────▼──────────────┐         ┌─────────────▼──────────┤
│     codigos_2fa     │         │  email_verification_   │
│─────────────────────│         │        token           │
│ id (PK)             │         │────────────────────────│
│ user_id (FK)        │         │ id (PK)                │
│ codigo              │         │ token (UK)             │
│ creado_en           │         │ user_id (FK)           │
│ expira_en           │         │ expiration_date        │
│ usado               │         └────────────────────────┘
│ intentos_fallidos   │
│ cantidad_reenvios   │         ┌────────────────────────┐
│ bloqueos_acumulados │         │  password_reset_tokens │
│ bloqueado_hasta     │         │────────────────────────│
│ resultado           │         │ id (PK)                │
│ fecha_uso           │         │ token (UK)             │
│ ip_origen           │         │ user_id (FK → users)   │
└─────────────────────┘         │ expiration_date        │
                                │ used                   │
                                └────────────────────────┘

┌─────────────────────────────────┐
│         service_prices          │
│─────────────────────────────────│
│ id (PK)                         │
│ service_type (UK)               │
│ display_name                    │
│ price                           │
│ description                     │
│ active                          │
└─────────────────────────────────┘
```

> `service_prices` no tiene FK directa con otras tablas. El campo `service_type`
> coincide por valor con el campo `reason` de `appointments`.

---

## Tablas

### `users`
Usuarios del sistema. Rol puede ser `ROLE_CLIENTE`, `ROLE_VETERINARIO`, `ROLE_RECEPCIONISTA` o `ROLE_ADMIN`.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `name` | VARCHAR(100) | Nombre completo |
| `email` | VARCHAR(150) UK | Correo electrónico (único, usado como username) |
| `password` | VARCHAR | Contraseña encriptada con BCrypt |
| `primer_acceso` | BOOLEAN | Si `true`, debe cambiar contraseña al primer login |
| `role` | VARCHAR | Rol del usuario |
| `failed_attempts` | INT | Intentos fallidos de login (anti fuerza bruta) |
| `lock_time` | DATETIME | Fecha hasta la que la cuenta está bloqueada |
| `two_factor_code` | VARCHAR | Código 2FA temporal |
| `two_factor_expiration` | DATETIME | Expiración del código 2FA temporal |
| `enabled` | BOOLEAN | `false` hasta verificar el correo |
| `photo_url` | VARCHAR(500) | URL pública de foto de perfil (Cloudinary) |
| `phone` | VARCHAR(20) | Teléfono de 10 dígitos |

---

### `pets`
Mascotas registradas por los clientes.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `name` | VARCHAR | Nombre de la mascota |
| `species` | VARCHAR | Especie (ej: Perro, Gato) |
| `breed` | VARCHAR | Raza (opcional) |
| `birth_date` | VARCHAR | Fecha de nacimiento |
| `sex` | VARCHAR | Sexo (Macho / Hembra) |
| `owner_email` | VARCHAR | Correo del propietario |
| `photo_url` | VARCHAR(500) | URL pública de foto de la mascota |

---

### `appointments`
Citas veterinarias. Restricción única sobre `(date, time, vet_id)` para evitar solapamiento.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `date` | DATE | Fecha de la cita |
| `time` | TIME | Hora de la cita |
| `reason` | VARCHAR(255) | Motivo / tipo de servicio |
| `status` | VARCHAR | Estado: `PENDIENTE`, `CONFIRMADA`, `COMPLETADA`, `CANCELADA` |
| `client_id` | BIGINT FK → users | Cliente que agenda |
| `vet_id` | BIGINT FK → users | Veterinario asignado |
| `pet_id` | BIGINT FK → pets | Mascota de la cita |
| `notes` | VARCHAR(500) | Notas del recepcionista |
| `cancel_reason` | VARCHAR(300) | Motivo de cancelación |

---

### `payments`
Registro contable de pagos. Almacena una instantánea de los datos al momento del cobro para garantizar inmutabilidad del historial financiero.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `appointment_id` | BIGINT (nullable) | FK lógica a `appointments` (nullable para preservar el pago si se elimina la cita) |
| `client_name` | VARCHAR(120) | Nombre del cliente al momento del cobro |
| `client_email` | VARCHAR(120) | Correo del cliente al momento del cobro |
| `pet_name` | VARCHAR(80) | Nombre de la mascota al momento del cobro |
| `vet_name` | VARCHAR(120) | Nombre del veterinario al momento del cobro |
| `appointment_date` | DATE | Fecha de la cita cobrada |
| `appointment_time` | TIME | Hora de la cita cobrada |
| `concept` | VARCHAR(100) | Concepto cobrado |
| `base_amount` | DECIMAL(12,2) | Precio base de `service_prices` al momento del cobro |
| `amount` | DECIMAL(12,2) | Monto final cobrado |
| `status` | VARCHAR(20) | Estado: `PENDING`, `PAID`, `CANCELLED` |
| `payment_date` | DATETIME | Fecha/hora de confirmación del cobro |
| `received_by` | VARCHAR(120) | Email de la recepcionista que registró el pago |
| `notes` | VARCHAR(255) | Notas adicionales |
| `created_at` | DATETIME | Fecha de creación del registro |

---

### `service_prices`
Precios base por tipo de servicio, gestionados por el administrador.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `service_type` | VARCHAR(100) UK | Clave del servicio (coincide con `reason` de `appointments`) |
| `display_name` | VARCHAR(100) | Nombre legible para la UI |
| `price` | DECIMAL(12,2) | Precio base en COP |
| `description` | VARCHAR(255) | Descripción opcional |
| `active` | BOOLEAN | Si `false`, no aparece en el wizard de cobros |

---

### `codigos_2fa`
Códigos de segundo factor de autenticación por sesión de login.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `user_id` | BIGINT FK → users | Usuario propietario |
| `codigo` | VARCHAR | Código numérico de 6 dígitos |
| `creado_en` | DATETIME | Fecha/hora de generación |
| `expira_en` | DATETIME | Expiración (creado_en + 3 min) |
| `usado` | BOOLEAN | `true` si ya fue procesado |
| `intentos_fallidos` | INT | Intentos fallidos de verificación (máx 3) |
| `cantidad_reenvios` | INT | Reenvíos solicitados en la sesión (máx 5) |
| `bloqueos_acumulados` | INT | Bloqueos acumulados (escala el tiempo de espera) |
| `bloqueado_hasta` | DATETIME | Fecha hasta la que el usuario está bloqueado |
| `resultado` | VARCHAR | Resultado final: `EXITOSO`, `FALLIDO`, `EXPIRADO`, `INVALIDADO` |
| `fecha_uso` | DATETIME | Fecha en que fue consumido o invalidado |
| `ip_origen` | VARCHAR(45) | IP de la solicitud (IPv4/IPv6) |

---

### `email_verification_token`
Token de verificación de correo enviado al registrarse.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `token` | VARCHAR UK | Token UUID único |
| `user_id` | BIGINT FK → users | Usuario a verificar |
| `expiration_date` | DATETIME | Expiración del token (1 hora) |

---

### `password_reset_tokens`
Tokens para el flujo "olvidé mi contraseña".

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `token` | VARCHAR(255) UK | Token UUID único enviado por correo |
| `user_id` | BIGINT FK → users | Usuario que solicitó el restablecimiento |
| `expiration_date` | DATETIME | Expiración del token (30 min) |
| `used` | BOOLEAN | `true` si ya fue consumido |

---

## Notas de diseño

- `payments.appointment_id` es nullable a propósito: si una cita se elimina, el registro de pago se conserva como historial contable permanente.
- `pets.owner_email` se actualiza en cascada desde `ProfileService` cuando el cliente cambia su correo.
- `codigos_2fa` tiene `CascadeType.ALL + orphanRemoval = true` desde `User`, por lo que al eliminar un usuario todos sus códigos 2FA se eliminan automáticamente.
- Los índices en `payments` optimizan las consultas más frecuentes del panel de administración (por cliente, estado y fecha).
