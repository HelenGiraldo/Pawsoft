# Changelog — PawSoft

Registro de cambios, correcciones y mejoras del sistema.

---

## [2026-04-11] - 11 de abril de 2026

### Nuevas Funcionalidades

#### Sistema de Precios para Medicamentos y Vacunas con Ajustes de Pago
**Tipo:** Mantenimiento Evolutivo  
**Funcionalidad:** Sistema completo de facturación detallada para medicamentos y vacunas aplicados durante procedimientos, con capacidad de ajuste de montos y auditoría completa.

**Implementación:**

**Catálogos de Precios:**
- Nuevo catálogo de medicamentos (`medication_catalog`) con precio unitario y unidad de medida
- Nuevo catálogo de vacunas (`vaccine_catalog`) con precio por dosis
- Gestión completa CRUD por administrador
- Filtrado de ítems activos/inactivos

**Facturación Detallada:**
- Nueva tabla `payment_items` para desglose de cobros
- Tipos de ítems: SERVICE (servicios), MEDICATION (medicamentos), VACCINE (vacunas)
- Cálculo automático de subtotales (cantidad × precio unitario)
- Monto total del pago = suma de todos los ítems

**Sistema de Ajustes con Auditoría:**
- Nueva tabla `payment_adjustments` para registro de modificaciones de monto
- Campos obligatorios: motivo del ajuste (mínimo 10 caracteres)
- Auditoría automática: quién ajustó (email y nombre), cuándo, monto original, monto ajustado, diferencia
- Historial completo de ajustes visible para administrador
- Solo recepcionistas pueden ajustar montos

**Endpoints Nuevos:**

Recepcionista:
- `GET /api/recepcionista/payments/medications` - Lista medicamentos activos
- `GET /api/recepcionista/payments/vaccines` - Lista vacunas activas
- `PUT /api/recepcionista/payments/{id}/adjust` - Ajusta monto de pago con motivo

Administrador:
- `GET /api/admin/payments/medications` - Lista todos los medicamentos
- `POST /api/admin/payments/medications` - Crea/actualiza medicamento
- `DELETE /api/admin/payments/medications/{id}` - Elimina medicamento
- `GET /api/admin/payments/vaccines` - Lista todas las vacunas
- `POST /api/admin/payments/vaccines` - Crea/actualiza vacuna
- `DELETE /api/admin/payments/vaccines/{id}` - Elimina vacuna

**Seguridad:**
- Validación de monto mínimo (no negativo)
- Validación de motivo de ajuste (10-500 caracteres)
- Registro en audit_log de todas las operaciones
- Nombre de recepcionista extraído del JWT token

**Archivos creados:**
- Backend Models: `MedicationCatalog.java`, `VaccineCatalog.java`, `PaymentItem.java`, `PaymentAdjustment.java`
- Backend Repositories: `MedicationCatalogRepository.java`, `VaccineCatalogRepository.java`, `PaymentItemRepository.java`, `PaymentAdjustmentRepository.java`
- Backend Services: `CatalogService.java`
- Backend DTOs: `MedicationCatalogRequest.java`, `MedicationCatalogResponse.java`, `VaccineCatalogRequest.java`, `VaccineCatalogResponse.java`, `PaymentItemRequest.java`, `PaymentItemResponse.java`, `PaymentAdjustmentRequest.java`, `PaymentAdjustmentResponse.java`

**Archivos modificados:**
- Backend: `Payment.java`, `PaymentRequest.java`, `PaymentResponse.java`, `PaymentService.java`, `RecepcionistaPaymentController.java`, `AdminPaymentController.java`

**Comportamiento preservado:**
- Sistema de pagos existente sigue funcionando
- Instantánea de datos de cita en pagos
- Estadísticas de ingresos
- Validaciones de unicidad (un pago por cita)

**Migración de BD requerida:**
```sql
CREATE TABLE medication_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(12,2) NOT NULL,
    unit VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE vaccine_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE payment_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50),
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

CREATE TABLE payment_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    original_amount DECIMAL(12,2) NOT NULL,
    adjusted_amount DECIMAL(12,2) NOT NULL,
    difference DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    adjusted_by VARCHAR(120) NOT NULL,
    adjusted_by_name VARCHAR(120) NOT NULL,
    adjusted_at DATETIME NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);
```

---

#### Control de Acceso Basado en Roles (RBAC) para Chatbot
**Tipo:** Mantenimiento Evolutivo  
**Funcionalidad:** El chatbot PawBot ahora adapta sus respuestas según el rol del usuario autenticado, protegiendo información sensible y funcionalidades restringidas.

**Implementación:**
- Nuevo servicio `SystemPromptGenerator` que genera prompts dinámicos por rol
- System prompts específicos para cada rol:
  - ROLE_ADMIN: Acceso completo a todas las funcionalidades
  - ROLE_VETERINARIO: Acceso a funciones médicas (diagnósticos, historiales clínicos, gestión de citas)
  - ROLE_RECEPCIONISTA: Acceso a gestión de citas, pagos y registro de clientes
  - ROLE_CLIENTE: Acceso limitado (agendar citas propias, ver mascotas, realizar pagos)
- Extracción segura del rol desde JWT token en el backend
- Validación de autenticación en cada solicitud al chatbot
- Logging de auditoría con información del rol del usuario
- Límite de historial de conversación a 10 mensajes para optimizar rendimiento

**Seguridad:**
- El rol se extrae exclusivamente del JWT token (nunca desde el frontend)
- Chatbot requiere autenticación (cambio de `permitAll()` a `authenticated()`)
- Respuestas educadas cuando se solicita información fuera del alcance del rol
- Caché de prompts en memoria para optimizar rendimiento

**Archivos modificados:**
- Backend: `SystemPromptGenerator.java` (nuevo), `ChatbotController.java`, `SecurityConfig.java`

**Comportamiento preservado:**
- Funcionalidades de accesibilidad disponibles para todos los roles
- Información de contacto y soporte disponible para todos
- Tono amigable y profesional del chatbot

---

### Correcciones

#### Fix: Mensaje de Error en Cambio de Contraseña sin Login
**Tipo:** Mantenimiento Correctivo  
**Problema:** Cuando un usuario cambiaba su contraseña por primera vez y luego intentaba cambiarla nuevamente sin haber iniciado sesión con la nueva contraseña, el sistema mostraba un mensaje genérico "El usuario ya ha cambiado la contraseña" que no explicaba qué debía hacer el usuario.

**Solución:**
- Actualizado mensaje de error a: "No puedes cambiar la contraseña porque ya se solicitó un cambio anteriormente y aún no has iniciado sesión con la nueva contraseña"
- El mensaje ahora proporciona información accionable al usuario
- Mejora la experiencia de usuario al explicar claramente el requisito de iniciar sesión

**Archivos modificados:**
- `backendPawsoft/src/main/java/co/edu/uniquindio/backendpawsoft/service/AuthService.java`

**Comportamiento preservado:**
- Primer cambio de contraseña exitoso (primerAcceso = true → false)
- Validaciones de seguridad de contraseña (mínimo 8 caracteres, mayúscula, número, carácter especial)
- Validación que previene usar la misma contraseña temporal
- Manejo de usuarios no encontrados (NotFoundException)
- Generación de tokens JWT y refresh tokens

---

## [2026-03-31] - 31 de marzo de 2026

### Documentación

#### Actualización Completa de Documentación
**Tipo:** Mantenimiento Evolutivo

**Documentos creados/actualizados:**
- `README.md` - Reescrito profesionalmente
- `docs/documentacion-funcional.md` - Especificación funcional completa
- `docs/operaciones.md` - Manual de operaciones
- `docs/politicas-seguridad.md` - Actualizado con refresh tokens
- Comentarios JavaDoc en servicios críticos
- Comentarios TSDoc en componentes frontend

---

## [2026-03-30] - 30 de marzo de 2026

### Correcciones

#### Fix: Scroll Bar en Login
**Tipo:** Mantenimiento Correctivo  
**Problema:** Barra de scroll aparecía al hacer click en inputs del login  
**Solución:**
- Agregado `[scrollY]="false"` a `ion-content`
- `overflow: hidden` en contenedores

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/auth/login/login.page.html`
- `Front-end-pawsoft/src/app/pages/auth/login/login.page.scss`

---

## [2026-03-29] - 29 de marzo de 2026

### Nuevas Funcionalidades

#### Cancelación de Atención Iniciada
**Tipo:** Mantenimiento Evolutivo  
**Problema:** Veterinarios iniciaban atención en cita equivocada sin poder revertir  
**Solución:**
- Endpoint: `/api/vet/appointments/{id}/cancel-start`
- Revierte estado de IN_PROGRESS → CONFIRMED
- Modal de confirmación en frontend

**Archivos modificados:**
- Backend: `AppointmentService.java`, `VetAppointmentController.java`
- Frontend: `appointment.service.ts`, `formulario-consulta.component.ts`, `formulario-consulta.component.html`, `formulario-consulta.component.scss`

---

## [2026-03-28] - 28 de marzo de 2026

### Correcciones

#### Fix: Error al Iniciar Cita
**Tipo:** Mantenimiento Correctivo  
**Error:** `Data truncated for column 'status' at row 1`  
**Causa:** Columna `status` muy pequeña para `IN_PROGRESS` (11 caracteres)  
**Solución:** Aumentado tamaño de columna a VARCHAR(20)

**Migración de BD:**
```sql
ALTER TABLE appointments MODIFY COLUMN status VARCHAR(20);
```

**Archivos modificados:**
- `backendPawsoft/src/main/java/co/edu/uniquindio/backendpawsoft/model/Appointment.java`

---

## [2026-03-27] - 27 de marzo de 2026

### Nuevas Funcionalidades

#### Subida de Fotos en Registros Médicos
**Tipo:** Mantenimiento Evolutivo  
**Funcionalidad:**
- Veterinarios pueden adjuntar fotos a registros médicos
- Integración con Cloudinary para almacenamiento
- Límite: 2MB por foto
- Campo `fotosAdjuntas` almacena URLs en formato JSON

**Archivos modificados:**
- Backend: `MedicalRecord.java`, `MedicalRecordRequest.java`, `MedicalRecordResponse.java`, `MedicalRecordService.java`
- Frontend: `medical-record.service.ts`, `formulario-consulta.component.ts`, `formulario-consulta.component.html`, `formulario-consulta.component.scss`

---

### Correcciones

#### Fix: Campos Numéricos Aceptaban Letras
**Tipo:** Mantenimiento Correctivo  
**Problema:** Campos de peso, temperatura, frecuencia cardíaca aceptaban texto  
**Solución:** Cambiados a `type="number"` con validaciones min/max/step

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/formulario-consulta/formulario-consulta.component.html`
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/formulario-consulta/formulario-consulta.component.ts`

---

## [2026-03-26] - 26 de marzo de 2026

### Nuevas Funcionalidades

#### Estado IN_PROGRESS para Citas
**Tipo:** Mantenimiento Evolutivo  
**Problema:** Veterinarios podían iniciar múltiples diagnósticos simultáneamente  
**Solución:**
- Agregado estado `IN_PROGRESS` a citas
- Validación: solo una cita IN_PROGRESS por veterinario
- Endpoint: `/api/vet/appointments/{id}/start`

**Archivos modificados:**
- Backend: `AppointmentStatus.java`, `AppointmentService.java`, `VetAppointmentController.java`, `MedicalRecordService.java`
- Frontend: `appointment.service.ts`, `atencion-medica.component.ts`, `dashboard-vet.component.ts`, `dashboard-vet.component.scss`

---

### Correcciones

#### Fix: Lógica de Filtrado de Citas
**Tipo:** Mantenimiento Correctivo  
**Mejoras:**
- "Mis Citas de Hoy": solo `UPCOMING`, `CONFIRMED`, `IN_PROGRESS`
- "Próximas Citas": solo `UPCOMING`, `CONFIRMED`
- Excluidas: `COMPLETED`, `CANCELLED`, `NO_SHOW`
- Estadística "En progreso" en lugar de "Pendientes"

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/dashboard-vet.component.ts`

---

#### Fix: Filtro de Próximas Citas
**Tipo:** Mantenimiento Correctivo  
**Problema:** "Próximas Citas" mostraba citas de hoy  
**Solución:** Cambiado filtro de `date >= today` a `date > today`

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/dashboard-vet.component.ts`

---

## [2026-03-25] - 25 de marzo de 2026

### Correcciones

#### Fix: Botón Deshabilitado en Modo Claro
**Tipo:** Mantenimiento Correctivo  
**Problema:** Botón "Iniciar atención" no mostraba estado deshabilitado en modo claro  
**Solución:** Agregado estilo `:disabled` para `.btn-iniciar`

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/atencion-medica/atencion-medica.component.scss`

---

#### Fix: Workflow de Formulario de Consulta
**Tipo:** Mantenimiento Correctivo  
**Problema:** Veterinarios cerraban atención antes de completar formulario del cliente  
**Solución:**
- Botón "Continuar al resumen →" en sección interna
- Botón "✓ Cerrar atención" solo en sección de cliente
- Validación de campos requeridos antes de continuar
- Estilos de error visual (borde rojo, fondo rosa)

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/formulario-consulta/formulario-consulta.component.ts`
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/formulario-consulta/formulario-consulta.component.html`
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/formulario-consulta/formulario-consulta.component.scss`

---

## [2026-03-24] - 24 de marzo de 2026

### Nuevas Funcionalidades

#### Sistema de Refresh Tokens
**Tipo:** Mantenimiento Evolutivo  
**Problema:** Usuarios perdían sesión después de 1 hora trabajando en registros médicos  
**Solución:**
- Implementado sistema de refresh tokens con duración de 7 días
- Tokens se renuevan automáticamente 5 minutos antes de expirar
- Interceptor HTTP maneja renovación transparente
- Endpoints: `/auth/refresh`, `/auth/logout`

**Archivos modificados:**
- Backend: `RefreshToken.java`, `RefreshTokenRepository.java`, `RefreshTokenService.java`, `AuthService.java`, `AuthController.java`, `JwtService.java`, `JwtAuthenticationFilter.java`, `LoginResponse.java`, `RefreshTokenRequest.java`
- Frontend: `token-refresh.interceptor.ts`, `auth.service.ts`, `login.page.ts`, `main.ts`

---

### Correcciones

#### Fix: Timeout de Inactividad por Rol
**Tipo:** Mantenimiento Correctivo  
**Problema:** Sesión se cerraba después de 3 minutos para todos los roles  
**Solución:**
- Timeouts diferenciados: Veterinario/Admin: 60min, Recepcionista: 30min, Cliente: 15min
- Timer se reinicia con cualquier acción del usuario

**Archivos modificados:**
- `Front-end-pawsoft/src/app/services/inactivity.service.ts`

---

#### Fix: Scroll en Historial Clínico
**Tipo:** Mantenimiento Correctivo  
**Problema:** No se podía hacer scroll en el historial clínico  
**Solución:** Agregado `overflow-y: auto` a `.content-area`

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/historial-clinico/historial-clinico.component.scss`

---

#### Fix: Filtros de Fecha en Historial Clínico
**Tipo:** Mantenimiento Correctivo  
**Problemas:**
- No se veía icono de calendario en modo claro
- Faltaban labels para los filtros de fecha
- No había restricciones de fechas

**Solución:**
- Agregados labels "Desde:" y "Hasta:"
- Fijado icono de calendario con `color-scheme: light`
- Restricciones: min = 20 años atrás, max = hoy
- Estilos para modo oscuro

**Archivos modificados:**
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/historial-clinico/historial-clinico.component.html`
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/historial-clinico/historial-clinico.component.scss`
- `Front-end-pawsoft/src/app/pages/appointments/dashboard-vet/historial-clinico/historial-clinico.component.ts`

---

### Configuración

#### Sistema de Backups Automáticos
**Tipo:** Configuración de Infraestructura

- Script de backup diario a las 2:00 AM UTC
- Retención de 7 días
- Logs de ejecución en `/home/ec2-user/backups/backup.log`
- Primer backup exitoso: `pawsoft_20260324_020001.sql`

---

## [2026-02] - Febrero 2026

### Configuración Inicial

#### Infraestructura en AWS
- Configuración de EC2 para backend
- Configuración de RDS MySQL 8.0
- Configuración de S3 + CloudFront para frontend
- Certificados SSL configurados

#### Monitoreo
- Instalación y configuración de Prometheus (`http://3.135.224.139:9090`)
- Instalación y configuración de Grafana (`http://3.135.224.139:3000`)
- Dashboard de métricas de negocio

---

## Cómo Interpretar Este Changelog

**Mantenimiento Correctivo:** Correcciones de bugs y problemas reportados por usuarios  
**Mantenimiento Evolutivo:** Nuevas funcionalidades y mejoras al sistema

**Evidencia adicional:**
- Commits de Git con mensajes descriptivos
- Tabla `audit_log` en base de datos
- Logs de aplicación en servidor
- Backups diarios desde 24 de marzo de 2026
