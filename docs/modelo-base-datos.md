# Modelo de Base de Datos — PawSoft

Base de datos relacional MySQL gestionada con Spring Data JPA / Hibernate.

---

## Relaciones entre tablas

- `users` → `appointments` mediante `client_id` (cliente que agenda) y `vet_id` (veterinario asignado)
- `pets` → `appointments` mediante `pet_id`
- `appointments` → `payments` mediante `appointment_id` (nullable: el pago se conserva aunque se elimine la cita)
- `appointments` → `medical_records` mediante `appointment_id`
- `pets` → `medical_records` mediante `pet_id`
- `users` → `medical_records` mediante `vet_id`
- `users` → `codigos_2fa` mediante `user_id`
- `users` → `email_verification_token` mediante `user_id`
- `users` → `password_reset_tokens` mediante `user_id`
- `users` → `refresh_tokens` mediante `user_email`
- `service_prices` no tiene FK directa; su campo `service_type` coincide por valor con el campo `reason` de `appointments`

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
| `status` | VARCHAR | Estado: `UPCOMING`, `CONFIRMED`, `IN_PROGRESS`, `NO_SHOW`, `CANCELLED`, `COMPLETED` |
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

### `medical_records`
Registros médicos de consultas veterinarias.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `appointment_id` | BIGINT FK → appointments | Cita asociada |
| `pet_id` | BIGINT FK → pets | Mascota atendida |
| `vet_id` | BIGINT FK → users | Veterinario que realizó la consulta |
| `peso` | DOUBLE | Peso de la mascota en kg |
| `temperatura` | DOUBLE | Temperatura en °C |
| `frecuencia_cardiaca` | INT | Frecuencia cardíaca en bpm |
| `observaciones_generales` | VARCHAR(1000) | Observaciones del examen físico |
| `diagnostico_principal` | VARCHAR(500) | Diagnóstico principal |
| `diagnostico_secundario` | VARCHAR(500) | Diagnóstico secundario (opcional) |
| `notas_clinicas` | VARCHAR(1000) | Notas clínicas internas del veterinario |
| `medicamentos` | TEXT | JSON de medicamentos usados durante el procedimiento |
| `indicaciones` | VARCHAR(1000) | Indicaciones internas del veterinario |
| `diagnostico_cliente` | VARCHAR(500) | Diagnóstico en lenguaje simple para el propietario |
| `medicamentos_recetados` | TEXT | JSON de medicamentos recetados para tratar en casa |
| `indicaciones_cliente` | VARCHAR(1000) | Indicaciones para el propietario |
| `vacunas_aplicadas` | TEXT | JSON de vacunas aplicadas |
| `proximo_control_fecha` | DATE | Fecha del próximo control (opcional) |
| `proximo_control_motivo` | VARCHAR(300) | Motivo del próximo control |
| `fotos_adjuntas` | TEXT | JSON de URLs de fotos (Cloudinary) |
| `creado_en` | DATETIME | Fecha de creación |
| `actualizado_en` | DATETIME | Fecha de última actualización |

---

### `audit_logs`
Registro de auditoría de acciones críticas del sistema.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `user_id` | INT | ID del usuario que realizó la acción |
| `user_role` | VARCHAR(20) | Rol del usuario: `ADMIN`, `VETERINARIAN`, `RECEPTIONIST`, `CLIENT` |
| `user_name` | VARCHAR(100) | Nombre del usuario |
| `action` | VARCHAR(100) | Código de la acción (ej: `APPOINTMENT_CREATE`, `PAYMENT_CONFIRMED`) |
| `description` | VARCHAR(255) | Descripción legible de la acción |
| `entity` | VARCHAR(50) | Tipo de entidad afectada (ej: `APPOINTMENT`, `PAYMENT`, `USER`) |
| `entity_id` | INT | ID de la entidad afectada |
| `old_value` | JSON | Valor anterior (opcional, formato JSON) |
| `new_value` | JSON | Valor nuevo (opcional, formato JSON) |
| `ip_address` | VARCHAR(45) | Dirección IP del usuario (IPv4/IPv6) |
| `user_agent` | VARCHAR(255) | User agent del navegador |
| `created_at` | DATETIME | Timestamp de la acción (inmutable) |

---

### `refresh_tokens`
Tokens de renovación de sesión con duración de 7 días.

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador autoincremental |
| `token_hash` | VARCHAR(500) UK | Hash SHA-256 del refresh token |
| `user_email` | VARCHAR | Email del usuario propietario |
| `created_at` | DATETIME | Fecha de creación |
| `expires_at` | DATETIME | Fecha de expiración (7 días) |
| `revoked` | BOOLEAN | `true` si fue invalidado (logout o cambio de contraseña) |
| `device_info` | VARCHAR(100) | Información del dispositivo (opcional) |

---

## Notas de diseño

- `payments.appointment_id` es nullable a propósito: si una cita se elimina, el registro de pago se conserva como historial contable permanente.
- `pets.owner_email` se actualiza en cascada desde `ProfileService` cuando el cliente cambia su correo.
- `codigos_2fa` tiene `CascadeType.ALL + orphanRemoval = true` desde `User`, por lo que al eliminar un usuario todos sus códigos 2FA se eliminan automáticamente.
- Los índices en `payments` optimizan las consultas más frecuentes del panel de administración (por cliente, estado y fecha).
