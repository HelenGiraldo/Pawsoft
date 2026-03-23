# Modelo de Base de Datos — PawSoft

Base de datos relacional gestionada por **Spring Data JPA / Hibernate** sobre **MongoDB Atlas** (o el motor configurado en `application.properties`).  
El esquema se genera automáticamente a partir de las entidades JPA con `spring.jpa.hibernate.ddl-auto`.

---

## Diagrama entidad-relación (Mermaid)

```mermaid
erDiagram

    users {
        BIGINT      id              PK
        VARCHAR(100) name
        VARCHAR(150) email          UK
        VARCHAR     password
        BOOLEAN     primer_acceso
        VARCHAR     role
        INT         failed_attempts
        DATETIME    lock_time
        VARCHAR     two_factor_code
        DATETIME    two_factor_expiration
        BOOLEAN     enabled
        VARCHAR(500) photo_url
        VARCHAR(20)  phone
    }

    pets {
        BIGINT      id              PK
        VARCHAR     name
        VARCHAR     species
        VARCHAR     breed
        VARCHAR     birth_date
        VARCHAR     sex
        VARCHAR     owner_email
        VARCHAR(500) photo_url
    }

    appointments {
        BIGINT      id              PK
        DATE        date
        TIME        time
        VARCHAR(255) reason
        VARCHAR     status
        BIGINT      client_id       FK
        BIGINT      vet_id          FK
        BIGINT      pet_id          FK
        VARCHAR(500) notes
        VARCHAR(300) cancel_reason
    }

    payments {
        BIGINT      id              PK
        BIGINT      appointment_id
        VARCHAR(120) client_name
        VARCHAR(120) client_email
        VARCHAR(80)  pet_name
        VARCHAR(120) vet_name
        DATE        appointment_date
        TIME        appointment_time
        VARCHAR(100) concept
        DECIMAL(12,2) base_amount
        DECIMAL(12,2) amount
        VARCHAR(20)  status
        DATETIME    payment_date
        VARCHAR(120) received_by
        VARCHAR(255) notes
        DATETIME    created_at
    }

    service_prices {
        BIGINT      id              PK
        VARCHAR(100) service_type   UK
        VARCHAR(100) display_name
        DECIMAL(12,2) price
        VARCHAR(255) description
        BOOLEAN     active
    }

    codigos_2fa {
        BIGINT      id              PK
        BIGINT      user_id         FK
        VARCHAR     codigo
        DATETIME    creado_en
        DATETIME    expira_en
        BOOLEAN     usado
        INT         intentos_fallidos
        INT         cantidad_reenvios
        INT         bloqueos_acumulados
        DATETIME    bloqueado_hasta
        VARCHAR     resultado
        DATETIME    fecha_uso
        VARCHAR(45) ip_origen
    }

    email_verification_token {
        BIGINT      id              PK
        VARCHAR     token           UK
        BIGINT      user_id         FK
        DATETIME    expiration_date
    }

    password_reset_tokens {
        BIGINT      id              PK
        VARCHAR(255) token          UK
        BIGINT      user_id         FK
        DATETIME    expiration_date
        BOOLEAN     used
    }

    users         ||--o{ appointments          : "client_id (cliente)"
    users         ||--o{ appointments          : "vet_id (veterinario)"
    pets          ||--o{ appointments          : "pet_id"
    users         ||--o{ codigos_2fa           : "user_id"
    users         ||--o| email_verification_token : "user_id"
    users         ||--o{ password_reset_tokens : "user_id"
```

---

## Descripción de tablas

### `users`
Usuarios del sistema. Un usuario puede tener rol `ROLE_CLIENTE`, `ROLE_VETERINARIO`, `ROLE_RECEPCIONISTA` o `ROLE_ADMIN`.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `name` | VARCHAR(100) | Nombre completo |
| `email` | VARCHAR(150) UK | Correo electrónico (único, usado como username) |
| `password` | VARCHAR | Contraseña encriptada (BCrypt) |
| `primer_acceso` | BOOLEAN | Si `true`, debe cambiar contraseña al primer login |
| `role` | VARCHAR | Rol del usuario (enum `Role`) |
| `failed_attempts` | INT | Intentos fallidos de login (anti fuerza bruta) |
| `lock_time` | DATETIME | Fecha hasta la que la cuenta está bloqueada |
| `two_factor_code` | VARCHAR | Código 2FA temporal (campo de soporte) |
| `two_factor_expiration` | DATETIME | Expiración del código 2FA temporal |
| `enabled` | BOOLEAN | `false` hasta verificar el correo |
| `photo_url` | VARCHAR(500) | URL pública de foto de perfil (Cloudinary) |
| `phone` | VARCHAR(20) | Teléfono colombiano de 10 dígitos |

---

### `pets`
Mascotas registradas por los clientes.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `name` | VARCHAR | Nombre de la mascota |
| `species` | VARCHAR | Especie (ej: Perro, Gato) |
| `breed` | VARCHAR | Raza (opcional) |
| `birth_date` | VARCHAR | Fecha de nacimiento (formato libre) |
| `sex` | VARCHAR | Sexo (Macho / Hembra) |
| `owner_email` | VARCHAR | Correo del propietario (se actualiza en cascada si cambia el email del usuario) |
| `photo_url` | VARCHAR(500) | URL pública de foto de la mascota |

---

### `appointments`
Citas veterinarias agendadas. Restricción única sobre `(date, time, vet_id)` para evitar solapamiento.

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
Registro contable de pagos. Almacena una instantánea de los datos de la cita al momento del cobro para garantizar inmutabilidad del historial financiero.

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
| `concept` | VARCHAR(100) | Concepto cobrado (ej: "Consulta general") |
| `base_amount` | DECIMAL(12,2) | Precio base de `service_prices` al momento del cobro |
| `amount` | DECIMAL(12,2) | Monto final cobrado (puede incluir ajuste) |
| `status` | VARCHAR(20) | Estado: `PENDING`, `PAID`, `CANCELLED` |
| `payment_date` | DATETIME | Fecha/hora de confirmación del cobro |
| `received_by` | VARCHAR(120) | Email de la recepcionista que registró el pago |
| `notes` | VARCHAR(255) | Notas adicionales |
| `created_at` | DATETIME | Fecha de creación del registro |

---

### `service_prices`
Tabla de precios base por tipo de servicio, gestionada por el administrador.

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
Códigos de segundo factor de autenticación. Un usuario puede tener múltiples registros a lo largo del tiempo (uno por sesión de login).

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
| `expiration_date` | DATETIME | Expiración del token |
| `used` | BOOLEAN | `true` si ya fue consumido |

---

## Notas de diseño

- **`payments.appointment_id` es nullable** a propósito: si una cita se elimina, el registro de pago se conserva como historial contable permanente.
- **`pets.owner_email`** se actualiza en cascada desde `ProfileService` cuando el cliente cambia su correo.
- **`codigos_2fa`** tiene `CascadeType.ALL + orphanRemoval = true` desde `User`, por lo que al eliminar un usuario todos sus códigos 2FA se eliminan automáticamente.
- Los índices en `payments` optimizan las consultas más frecuentes del panel de administración (por cliente, estado y fecha).
