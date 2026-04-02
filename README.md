# PawSoft - Sistema de Gestión Veterinaria

Backend API REST desarrollado con Spring Boot para la gestión integral de clínicas veterinarias.

---

## Descripción

PawSoft es un sistema de gestión veterinaria que centraliza y automatiza los procesos clínicos y administrativos de clínicas veterinarias pequeñas y medianas. El sistema reemplaza procesos manuales que generan desorden, pérdida de información y errores en el seguimiento de pacientes.

La plataforma incluye:

- Autenticación con 2FA y refresh tokens
- Gestión de usuarios con control de acceso por roles
- Registro y gestión de mascotas con fotos
- Sistema de agendamiento de citas con validación de disponibilidad
- Historial clínico con fotos adjuntas
- Sistema de pagos en efectivo
- Auditoría completa de acciones del sistema

---

## Roles del Sistema

**Cliente**: Gestiona sus mascotas, agenda citas, consulta historial de pagos y actualiza su perfil.

**Veterinario**: Visualiza su agenda de citas, registra atención médica, actualiza historial clínico y adjunta fotos de estudios.

**Recepcionista**: Gestiona clientes y mascotas, agenda citas para cualquier cliente, registra y confirma pagos en efectivo.

**Administrador**: Gestiona usuarios del sistema, configura precios de servicios, visualiza estadísticas financieras y consulta logs de auditoría.

---

## Stack Tecnológico

**Backend:**
- Java 17
- Spring Boot 3.x
- Spring Security (JWT + 2FA)
- Spring Data JPA / Hibernate
- Spring Mail
- Maven

**Base de Datos:**
- MySQL 8.0

**Seguridad:**
- JWT con refresh tokens
- BCrypt para contraseñas
- Autenticación de dos factores por email
- Google reCAPTCHA v2
- CORS configurado

**Servicios Externos:**
- Cloudinary (almacenamiento de imágenes)
- SMTP (envío de emails)

**Testing:**
- JUnit 5
- Mockito
- Spring Boot Test
- H2 Database (tests)

---

## Arquitectura

El backend utiliza arquitectura en capas:

```
Controllers (REST)  →  Endpoints HTTP
Services            →  Lógica de negocio
Repositories        →  Acceso a datos (JPA)
Models              →  Entidades (ORM)
```

Componentes principales:
- Security: Autenticación, autorización, JWT, 2FA
- Audit: Sistema de auditoría transversal
- Exception Handling: Manejo centralizado de errores
- DTOs: Transferencia de datos entre capas

---

## Endpoints Principales

### Autenticación
- `POST /auth/login` - Inicio de sesión
- `POST /auth/refresh` - Renovar JWT con refresh token
- `POST /auth/logout` - Cerrar sesión
- `POST /auth/verify-2fa` - Verificar código 2FA
- `POST /auth/password-reset/request` - Solicitar recuperación de contraseña
- `GET /auth/verify-email` - Verificar email con token

### Usuarios
- `POST /api/users` - Registro de cliente
- `GET /api/users` - Listar usuarios (admin)
- `POST /api/admin/users/staff` - Crear usuario staff

### Perfil
- `GET /api/profile/me` - Obtener perfil actual
- `POST /api/profile/verify-and-save` - Actualizar perfil con verificación 2FA

### Mascotas
- `GET /api/pets/my` - Mascotas del cliente
- `POST /api/pets` - Crear mascota
- `PUT /api/pets/{id}` - Actualizar mascota
- `DELETE /api/pets/{id}` - Eliminar mascota

### Citas
- `POST /api/appointments` - Crear cita (cliente)
- `GET /api/appointments/my` - Citas del cliente
- `GET /api/appointments/available-slots` - Horarios disponibles
- `PUT /api/appointments/{id}/cancel` - Cancelar cita
- `GET /api/vet/appointments/my` - Citas del veterinario
- `PUT /api/vet/appointments/{id}/start` - Iniciar atención médica
- `PUT /api/vet/appointments/{id}/cancel-start` - Cancelar atención iniciada

### Historial Clínico
- `POST /api/vet/medical-records` - Guardar registro médico
- `GET /api/vet/medical-records` - Obtener historial
- `GET /api/vet/medical-records/appointment/{id}` - Registro por cita

### Pagos
- `POST /api/recepcionista/payments` - Registrar pago
- `PUT /api/recepcionista/payments/{id}/pay` - Confirmar cobro
- `GET /api/cliente/payments/my` - Historial de pagos
- `GET /api/admin/payments/stats` - Estadísticas financieras
- `POST /api/admin/payments/prices` - Gestionar precios

---

## Configuración

### Variables de Entorno

Crear archivo `application-local.properties` con las siguientes variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pawsoft
spring.datasource.username=usuario
spring.datasource.password=contraseña

jwt.secret=clave_secreta_256_bits
jwt.expiration=3600000
jwt.refresh.expiration=604800000

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=correo@gmail.com
spring.mail.password=contraseña_aplicacion

cloudinary.cloud-name=nombre_cloud
cloudinary.api-key=api_key
cloudinary.api-secret=api_secret

recaptcha.secret=secret_key
recaptcha.verify-url=https://www.google.com/recaptcha/api/siteverify

frontend.url=http://localhost:4200
```

### Base de Datos

Crear base de datos MySQL:

```sql
CREATE DATABASE pawsoft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Las tablas se crean automáticamente con Hibernate al iniciar la aplicación.

---

## Instalación y Ejecución

### Requisitos
- Java 17 o superior
- Maven 3.6 o superior
- MySQL 8.0 o superior

### Pasos

1. Clonar el repositorio:
```bash
git clone https://github.com/HelenGiraldo/Pawsoft.git
cd backendPawsoft
```

2. Configurar variables de entorno en `application-local.properties`

3. Compilar el proyecto:
```bash
./mvnw clean install
```

4. Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

5. La API estará disponible en `http://localhost:8080`

### Ejecutar Tests

```bash
./mvnw test
```

---

## Despliegue en Producción

### Backend (Azure App Service)

1. Compilar JAR:
```bash
./mvnw clean package -DskipTests
```

2. Desplegar a Azure:
```bash
az webapp deploy --resource-group pawsoft-rg --name pawsoft-backend --src-path target/backendpawsoft-0.0.1-SNAPSHOT.jar
```

3. Configurar variables de entorno en Azure Portal o CLI

4. La aplicación se ejecuta automáticamente en: `https://pawsoft-backend.azurewebsites.net`

### Base de Datos (Clever Cloud MySQL)

Base de datos MySQL 8.0 gestionada en Clever Cloud:
- Plan: Gratis permanente
- Host: bjupuy...clever-cloud.com
- Backups automáticos incluidos en el plan
- Acceso mediante credenciales proporcionadas por Clever Cloud

### Backups

Los backups son gestionados automáticamente por Clever Cloud. Para backups manuales adicionales, usar:

```bash
mysqldump -h bjupuy...clever-cloud.com -u [user] -p pawsoft > backup_$(date +%Y%m%d).sql
```

---

## Documentación

- `docs/documentacion-funcional.md` - Módulos, flujos de trabajo y reglas de negocio
- `docs/politicas-seguridad.md` - Políticas de seguridad implementadas
- `docs/modelo-base-datos.md` - Modelo de base de datos
- `docs/plan-de-pruebas.md` - Plan de pruebas y resultados
- `docs/ADRs.md` - Decisiones arquitectónicas
- `docs/C4Model/` - Diagramas de arquitectura (4 niveles)
- `openapi-pawsoft.yaml` - Especificación OpenAPI de la API

---

## Seguridad

El sistema implementa:

- Autenticación JWT con refresh tokens (7 días)
- Autenticación de dos factores por email
- Contraseñas cifradas con BCrypt
- Control de acceso basado en roles (RBAC)
- Validación de entradas en frontend y backend
- Protección contra fuerza bruta con bloqueo temporal
- Protección contra bots con reCAPTCHA
- Comunicación HTTPS en producción
- Auditoría completa de acciones críticas
- Tokens con expiración temporal
- Cierre de sesión automático por inactividad

---

## Testing

El proyecto incluye 32 pruebas unitarias que validan:

- Servicios de autenticación y seguridad
- Lógica de negocio de pagos
- Gestión de perfiles con 2FA
- Validación de reCAPTCHA
- Manejo de excepciones

Ejecutar tests:
```bash
./mvnw test
```

---

## Equipo de Desarrollo

**Universidad del Quindío**  
Ingeniería de Sistemas y Computación  
Software III

**Autoras:**
- Valentina Porras Salazar
- Helen Xiomara Giraldo Libreros

**Profesor:**
- Raúl Yulbraynner Rivera Gálvez

---

## Licencia

Proyecto académico desarrollado para la Universidad del Quindío.
