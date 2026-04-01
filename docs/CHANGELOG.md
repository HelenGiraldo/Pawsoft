# Changelog — PawSoft

Registro de cambios, correcciones y mejoras del sistema.

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
