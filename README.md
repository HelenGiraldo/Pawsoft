# 🐾 PawSoft - Sistema de Gestión Veterinaria

Backend API REST desarrollado con Spring Boot para la gestión integral de clínicas veterinarias.

---

## 1. Resumen Arquitectónico

PawSoft es un sistema de gestión veterinaria cuyo propósito es centralizar y automatizar los procesos clínicos y administrativos de clínicas veterinarias pequeñas y medianas.

La plataforma permite gestionar:

- **Autenticación y Seguridad**: Sistema de login con 2FA, verificación de email, recuperación de contraseña y protección reCAPTCHA
- **Gestión de Usuarios**: Registro, perfiles, roles (Cliente, Veterinario, Recepcionista, Administrador)
- **Gestión de Mascotas**: Registro completo con fotos, historial y datos del propietario
- **Programación de Citas**: Sistema completo de agendamiento con disponibilidad de veterinarios
- **Sistema de Pagos**: Registro de pagos en efectivo, precios de servicios y estadísticas financieras
- **Auditoría**: Registro completo de todas las acciones del sistema para trazabilidad

El sistema reemplaza procesos manuales tradicionales que generan desorden, pérdida de información y errores en el seguimiento de pacientes.

PawSoft funciona mediante una aplicación web progresiva (PWA) para clientes y aplicaciones web administrativas para el personal interno. El backend expone una API REST desarrollada en Java con Spring Boot, organizada bajo una arquitectura en capas.

El sistema centraliza la información de clientes, mascotas y procesos clínicos en una única plataforma, facilitando el acceso a datos confiables, optimizando los flujos de trabajo y mejorando la eficiencia operativa.

---

## 1.1 Usuarios Principales

### Cliente
- Registra cuenta con verificación de email
- Gestiona sus mascotas (crear, editar, ver)
- Solicita, modifica o cancela citas veterinarias
- Consulta historial de pagos
- Actualiza su perfil con verificación 2FA
- Accede mediante aplicación web/móvil (PWA)

### Veterinario
- Visualiza agenda de citas asignadas
- Registra atención médica
- Actualiza historial clínico
- Accede con foto de perfil visible para clientes
- Usa aplicación web

### Recepcionista
- Gestiona clientes (crear, editar, activar/desactivar)
- Gestiona mascotas de clientes
- Agenda, cancela o modifica citas
- Registra y confirma pagos en efectivo
- Consulta precios de servicios
- Usa aplicación web

### Administrador
- Gestiona usuarios del sistema (staff)
- Configura precios de servicios veterinarios
- Visualiza estadísticas financieras y de pagos
- Revierte pagos (corrección de errores)
- Consulta logs de auditoría
- Usa aplicación web

---

## 1.2 Problema que Resuelve

El sistema resuelve la necesidad de organizar y centralizar la gestión administrativa y clínica de veterinarias pequeñas y medianas, reemplazando procesos manuales que generan:

- Desorden
- Pérdida de información
- Errores en seguimiento de pacientes

Permite el registro y consulta estructurada de:

- Clientes
- Mascotas
- Historial clínico
- Esquemas de vacunación
- Inventario de medicamentos

Mejora el control operativo, la eficiencia interna y la calidad del servicio veterinario.

---

## 1.3 Alcance del Sistema

### Incluye

- Plataforma accesible desde computador y dispositivos móviles.
- Registro y gestión de clientes.
- Registro y gestión de mascotas.
- Administración de usuarios médicos.
- Creación y consulta de historias clínicas.
- Registro y seguimiento de vacunación.
- Programación y gestión de citas.
- Control básico de inventario.
- Consulta de información por usuarios autorizados.

### No incluye

- Procesos contables o facturación electrónica.
- Pagos en línea.
- Telemedicina.
- Gestión avanzada de proveedores o compras automáticas.

---

## 1.4 Funcionalidades Implementadas

### Autenticación y Seguridad
- Registro de usuarios con verificación de email
- Login con autenticación de dos factores (2FA)
- Recuperación de contraseña por email
- Protección reCAPTCHA v3 contra bots
- JWT para autenticación stateless
- Bloqueo temporal por intentos fallidos
- Cambio de contraseña en primer acceso

### Gestión de Usuarios
- CRUD completo de usuarios
- Roles: ADMIN, VETERINARIO, RECEPCIONISTA, CLIENTE
- Gestión de perfiles con verificación 2FA
- Activación/desactivación de cuentas
- Creación de staff con contraseñas temporales

### Gestión de Mascotas
- Registro con foto (Cloudinary)
- Información completa: especie, raza, sexo, fecha de nacimiento
- Asociación con propietario
- Edición y eliminación

### Gestión de Citas
- Agendamiento por cliente
- Verificación de disponibilidad de veterinarios
- Estados: PENDING, CONFIRMED, CANCELLED, COMPLETED
- Gestión por recepcionista (crear, editar, cancelar)
- Visualización por veterinario
- Cancelación por cliente

### Sistema de Pagos
- Registro de pagos en efectivo
- Gestión de precios de servicios
- Estados: PENDING, PAID
- Confirmación de cobro por recepcionista
- Historial de pagos por cliente
- Estadísticas financieras para administrador
- Reporte de ingresos por período y concepto

### Auditoría
- Registro automático de todas las acciones
- Información capturada: usuario, acción, entidad, valores antes/después, IP, timestamp
- Consultas por usuario, rol, entidad, acción o fecha
- Trazabilidad completa para compliance

---

# 2. Arquitectura del Sistema (Modelo C4)

Las vistas C4 se enfocan en el alcance del cliente, incluyendo:

- Interfaz web (PWA)
- Backend API REST
- Base de datos MySQL
- Servicio de notificaciones
- Almacenamiento en nube

---

## 2.1 Nivel 1 - Diagrama de Contexto

PawSoft interactúa con:

- Cliente (persona)
- Servicio de Notificaciones y Correo (sistema externo)

El cliente agenda citas y consulta información.
El sistema envía notificaciones y recordatorios.

---

## 2.2 Nivel 2 - Diagrama de Contenedores

El sistema está compuesto por:

- Mobile App (PWA con Onsen UI)
- Backend (Java + Spring Boot)
- Base de Datos MySQL
- AWS S3 (almacenamiento de imágenes)
- Servicio de notificaciones externo

Comunicación mediante HTTPS y REST (JSON).

---

## 2.3 Nivel 3 - Diagrama de Componentes (Backend)

El backend incluye:

- API de Inicio de Sesión
- API de Gestión de Citas
- API de Mascotas
- Componente de Seguridad (Spring Security + JWT)
- Componente de Citas (lógica de negocio)
- Componente de Historial Clínico
- Componente de Notificaciones

Se utiliza arquitectura en capas:

- Controller
- Service
- Repository
- Model

---

# 3. Stack Tecnológico

## Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** (JWT + 2FA)
- **Spring Data JPA** / Hibernate
- **Spring Mail** (notificaciones por email)
- **Maven** (gestión de dependencias)

## Base de Datos
- **MySQL 8.0** (Relacional)
- Índices optimizados para consultas frecuentes
- Relaciones con integridad referencial

## Seguridad
- **JWT** (JSON Web Tokens)
- **BCrypt** (hash de contraseñas)
- **2FA** (códigos de 6 dígitos por email)
- **reCAPTCHA v3** (Google)
- **CORS** configurado

## Servicios Externos
- **Cloudinary** (almacenamiento de imágenes)
- **Google reCAPTCHA API**
- **SMTP** (envío de emails)

## Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **H2 Database** (tests)
- **32 pruebas unitarias** implementadas (100% exitosas)

## Herramientas de Desarrollo
- **Lombok** (reducción de boilerplate)
- **MapStruct** (mapeo de DTOs)
- **Validation API** (validación de datos)

---

# 4. Arquitectura

## Patrón de Capas

```
┌─────────────────────────────────────┐
│         Controllers (REST)          │  ← Endpoints HTTP
├─────────────────────────────────────┤
│      Services (Lógica Negocio)      │  ← Reglas de negocio
├─────────────────────────────────────┤
│    Repositories (Acceso a Datos)    │  ← JPA/Hibernate
├─────────────────────────────────────┤
│         Models (Entidades)          │  ← Mapeo ORM
└─────────────────────────────────────┘
```

## Componentes Principales

- **Security**: Autenticación, autorización, JWT, 2FA
- **Audit**: Sistema de auditoría transversal
- **Exception Handling**: Manejo centralizado de errores
- **DTOs**: Transferencia de datos entre capas
- **Config**: Configuración de seguridad, CORS, beans

---

# 4. Seguridad

## Autenticación
- **JWT** (JSON Web Tokens) con expiración configurable
- **2FA** obligatorio en login (código de 6 dígitos por email)
- **Verificación de email** en registro de clientes
- **Contraseñas temporales** para staff (cambio obligatorio en primer acceso)
- **Recuperación de contraseña** con token temporal

## Protección
- **reCAPTCHA v3** en registro y login
- **BCrypt** para hash de contraseñas
- **Bloqueo temporal** tras intentos fallidos
- **CORS** configurado para frontend específico
- **Rate limiting** en endpoints sensibles

## Autorización
- **Roles**: ADMIN, VETERINARIO, RECEPCIONISTA, CLIENTE
- **Endpoints protegidos** por rol con Spring Security
- **Validación de permisos** en capa de servicio

## Auditoría
- **Logs automáticos** de todas las acciones
- **Trazabilidad completa**: quién, qué, cuándo, desde dónde
- **Valores antes/después** en modificaciones
- **Inmutabilidad** de registros de auditoría

---

# 5. API Endpoints

## Autenticación (`/auth`)
- `POST /auth/login` - Login con email/password
- `POST /auth/verify-2fa` - Verificar código 2FA
- `POST /auth/resend-2fa` - Reenviar código 2FA
- `POST /auth/change-password-first` - Cambiar contraseña temporal
- `POST /auth/password-reset/request` - Solicitar reset de contraseña
- `POST /auth/password-reset/confirm` - Confirmar reset con token
- `GET /auth/verify-email` - Verificar email con token
- `POST /auth/resend-verification` - Reenviar email de verificación

## Usuarios (`/api/users`, `/api/admin/users`)
- `POST /api/users` - Registro de cliente
- `GET /api/users` - Listar usuarios (admin)
- `GET /api/users/{id}` - Obtener usuario
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario
- `POST /api/admin/users/staff` - Crear usuario staff

## Perfil (`/api/profile`)
- `GET /api/profile/me` - Obtener perfil actual
- `POST /api/profile/request-verification` - Solicitar código 2FA
- `POST /api/profile/verify-and-save` - Actualizar perfil con 2FA

## Mascotas (`/api/pets`)
- `GET /api/pets/my` - Mascotas del cliente autenticado
- `POST /api/pets` - Crear mascota
- `PUT /api/pets/{id}` - Actualizar mascota
- `DELETE /api/pets/{id}` - Eliminar mascota
- `GET /api/admin/pets` - Todas las mascotas (admin)

## Citas (`/api/appointments`, `/api/recepcionista/appointments`, `/api/vet/appointments`)
- `POST /api/appointments` - Crear cita (cliente)
- `GET /api/appointments/my` - Citas del cliente
- `GET /api/appointments/available-slots` - Horarios disponibles
- `PUT /api/appointments/{id}/cancel` - Cancelar cita
- `GET /api/recepcionista/appointments` - Todas las citas (recepcionista)
- `POST /api/recepcionista/appointments` - Crear cita (recepcionista)
- `PUT /api/recepcionista/appointments/{id}` - Actualizar cita
- `GET /api/vet/appointments/my` - Citas del veterinario

## Pagos (`/api/admin/payments`, `/api/cliente/payments`, `/api/recepcionista/payments`)
- `GET /api/admin/payments` - Todos los pagos
- `GET /api/admin/payments/stats` - Estadísticas financieras
- `GET /api/admin/payments/prices` - Gestión de precios
- `POST /api/admin/payments/prices` - Crear/actualizar precio
- `PUT /api/admin/payments/{id}/revert` - Revertir pago
- `GET /api/cliente/payments/my` - Historial de pagos del cliente
- `GET /api/cliente/payments/prices` - Precios de servicios
- `POST /api/recepcionista/payments` - Registrar pago
- `PUT /api/recepcionista/payments/{id}/pay` - Confirmar cobro

## Clientes (Recepcionista) (`/api/recepcionista`)
- `GET /api/recepcionista/clients` - Listar clientes
- `POST /api/recepcionista/clients` - Crear cliente
- `PUT /api/recepcionista/clients/{id}` - Actualizar cliente
- `PATCH /api/recepcionista/clients/{id}/toggle` - Activar/desactivar
- `DELETE /api/recepcionista/clients/{id}` - Eliminar cliente
- `GET /api/recepcionista/clients/{email}/pets` - Mascotas del cliente
- `POST /api/recepcionista/pets` - Crear mascota
- `PUT /api/recepcionista/pets/{id}` - Actualizar mascota
- `DELETE /api/recepcionista/pets/{id}` - Eliminar mascota

---

# 6. Configuración y Despliegue

## Variables de Entorno Requeridas

El proyecto requiere las siguientes variables de entorno. **NUNCA** incluyas valores reales en el repositorio.

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://[HOST]:[PORT]/[DATABASE_NAME]
spring.datasource.username=[DB_USERNAME]
spring.datasource.password=[DB_PASSWORD]

# JWT
jwt.secret=[GENERATE_SECURE_SECRET_KEY]
jwt.expiration=86400000

# Email (SMTP)
spring.mail.host=[SMTP_HOST]
spring.mail.port=[SMTP_PORT]
spring.mail.username=[EMAIL_ADDRESS]
spring.mail.password=[EMAIL_APP_PASSWORD]

# Cloudinary
cloudinary.cloud-name=[YOUR_CLOUD_NAME]
cloudinary.api-key=[YOUR_API_KEY]
cloudinary.api-secret=[YOUR_API_SECRET]

# reCAPTCHA
recaptcha.secret=[YOUR_RECAPTCHA_SECRET]
recaptcha.verify-url=https://www.google.com/recaptcha/api/siteverify

# Frontend URL (CORS)
frontend.url=[YOUR_FRONTEND_URL]
```

## Ejecución Local

```bash
# Clonar repositorio
git clone https://github.com/HelenGiraldo/Pawsoft.git
cd backendPawsoft

# Configurar variables de entorno
# Crea un archivo application-local.properties con tus credenciales
# NUNCA lo subas a Git (ya está en .gitignore)

# Compilar y ejecutar
./mvnw spring-boot:run

# O generar JAR
./mvnw clean package
java -jar target/backendpawsoft-0.0.1-SNAPSHOT.jar
```

## Ejecutar Tests

```bash
./mvnw test
```

## Notas de Seguridad

⚠️ **IMPORTANTE**:
- Nunca subas archivos con credenciales reales al repositorio
- Usa variables de entorno o archivos de configuración locales
- El archivo `application.properties` con valores reales debe estar en `.gitignore`
- Genera claves JWT seguras (mínimo 256 bits)
- Usa contraseñas de aplicación para SMTP, no tu contraseña personal
- Mantén actualizadas las dependencias de seguridad

---

# 7. Equipo de Desarrollo

**Proyecto**: PawSoft - Sistema de Gestión Veterinaria  
**Universidad**: Universidad del Quindío  
**Programa**: Ingeniería de Sistemas y Computación  
**Materia**: Software III  

**Autoras**:
- Valentina Porras Salazar
- Helen Xiomara Giraldo Libreros

**Profesor**:
- Raúl Yulbraynner Rivera Gálvez

---

# 8. Estado del Proyecto

✅ **Funcionalidades Completadas**:
- Sistema de autenticación con 2FA
- Gestión de usuarios y roles
- Gestión de mascotas
- Sistema de citas
- Sistema de pagos
- Auditoría completa
- Gestión de clientes por recepcionista
- 32 pruebas unitarias (100% exitosas)

📊 **Cobertura de Tests**:
- PaymentService: 14 tests
- ProfileService: 10 tests
- RecaptchaService: 7 tests
- ApplicationTests: 1 test

🚧 **En Desarrollo**:
- Historial clínico
- Gestión de inventario
- Reportes avanzados

---

# 9. Licencia

Este proyecto es desarrollado con fines académicos para la Universidad del Quindío. 
