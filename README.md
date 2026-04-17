<div align="center">

# 🐾 PawSoft - Sistema de Gestión Veterinaria

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot)
![Angular](https://img.shields.io/badge/Angular-20.0.0-red?style=for-the-badge&logo=angular)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3-orange?style=for-the-badge&logo=amazonaws)

**Sistema integral de gestión veterinaria para clínicas pequeñas y medianas**

[Documentación](#-documentación) • [Instalación](#-instalación-rápida) • [API](#-api-rest) • [Seguridad](#-seguridad)

</div>

---

## 📋 Descripción

PawSoft centraliza y automatiza procesos clínicos y administrativos de clínicas veterinarias, eliminando procesos manuales que generan desorden, pérdida de información y errores en el seguimiento de pacientes.

### ✨ Características Principales

| Módulo | Funcionalidades |
|--------|----------------|
| 🔐 **Autenticación** | JWT + 2FA por email, refresh tokens (7 días), recuperación de contraseña |
| 👥 **Usuarios** | 4 roles (Cliente, Veterinario, Recepcionista, Admin), gestión completa de perfiles |
| 🐕 **Mascotas** | Registro con fotos (Cloudinary), historial médico completo, perfiles médicos |
| 📅 **Citas** | Agendamiento con validación de disponibilidad, estados del ciclo de vida, cancelación |
| 🏥 **Historial Clínico** | Registros médicos digitales, fotos adjuntas, hospitalización, notas diarias |
| � **Pagos** | Facturación detallada (servicios, medicamentos, vacunas), ajustes con auditoría |
| 📊 **Administración** | Estadísticas financieras, gestión de precios, catálogos de medicamentos/vacunas |
| 🔍 **Auditoría** | Registro completo de acciones críticas con IP, timestamp y usuario |
| 🤖 **Chatbot** | Asistente inteligente con control de acceso por roles (PawBot) |

---

## 🏗️ Arquitectura

### Stack Tecnológico

<table>
<tr>
<td width="50%">

**Backend**
- Java 17
- Spring Boot 4.0.2
  - Spring Security (JWT + 2FA)
  - Spring Data JPA / Hibernate
  - Spring Mail
  - Spring Actuator
- MySQL 8.0 + Flyway
- Maven 3.6+

</td>
<td width="50%">

**Frontend**
- Angular 20.0.0
- Ionic 8.0.0
- TypeScript 5.9.0
- RxJS 7.8.0

</td>
</tr>
</table>

### Servicios Externos

| Servicio | Propósito |
|----------|-----------|
| **Cloudinary** | Almacenamiento de imágenes (mascotas, registros médicos) |
| **Gmail SMTP** | Envío de emails (2FA, recuperación de contraseña) |
| **Google reCAPTCHA v2** | Protección anti-bots en login y registro |

### Infraestructura AWS

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   CloudFront    │──────│   S3 Bucket      │      │   EC2 Instance  │
│   (Frontend)    │      │   (Static Web)   │      │   (Backend API) │
└─────────────────┘      └──────────────────┘      └─────────────────┘
                                                             │
                                                             ▼
                                                    ┌─────────────────┐
                                                    │  Clever Cloud   │
                                                    │  MySQL 8.0      │
                                                    └─────────────────┘
```

### Arquitectura en Capas

```
┌─────────────────────────────────────────────────┐
│  Controllers (REST)  →  Endpoints HTTP          │
├─────────────────────────────────────────────────┤
│  Services            →  Lógica de negocio       │
├─────────────────────────────────────────────────┤
│  Repositories        →  Acceso a datos (JPA)    │
├─────────────────────────────────────────────────┤
│  Models              →  Entidades (ORM)         │
└─────────────────────────────────────────────────┘
```

**Componentes Transversales:**
- **Security**: JWT, 2FA, RBAC, BCrypt
- **Audit**: Registro de acciones críticas
- **Exception Handling**: Manejo centralizado de errores
- **DTOs**: Transferencia de datos entre capas

---

## � Roles del Sistema

| Rol | Permisos |
|-----|----------|
| 🔵 **Cliente** | Gestiona sus mascotas, agenda citas propias, consulta historial de pagos, actualiza perfil |
| 🟢 **Veterinario** | Visualiza agenda asignada, registra atención médica, actualiza historial clínico, adjunta fotos |
| 🟡 **Recepcionista** | Gestiona clientes y mascotas, agenda citas para cualquier cliente, registra y confirma pagos |
| 🔴 **Administrador** | Acceso total: usuarios, precios, reportes, configuración, logs de auditoría |

---

## 🚀 Instalación Rápida

### Requisitos Previos

```bash
Java 17+
Maven 3.6+
MySQL 8.0+
Node.js 18+
npm 9+
```

### Backend

```bash
# 1. Clonar repositorio
git clone https://github.com/HelenGiraldo/Pawsoft.git
cd backendPawsoft

# 2. Crear base de datos
mysql -u root -p
CREATE DATABASE pawsoft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. Configurar variables (copiar application.properties.example)
cp src/main/resources/application.properties.example src/main/resources/application-local.properties
# Editar application-local.properties con tus credenciales

# 4. Compilar y ejecutar
./mvnw clean install
./mvnw spring-boot:run
```

**API disponible en:** `http://localhost:8080`

### Frontend

```bash
cd Front-end-pawsoft
npm install
npm start
```

**App disponible en:** `http://localhost:4200`

### Variables de Entorno Requeridas

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/pawsoft
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# JWT
jwt.secret=clave_secreta_minimo_256_bits
jwt.expiration=3600000
jwt.refresh.expiration=604800000

# Email (Gmail)
spring.mail.username=tu_correo@gmail.com
spring.mail.password=tu_app_password

# Cloudinary
cloudinary.cloud-name=tu_cloud_name
cloudinary.api-key=tu_api_key
cloudinary.api-secret=tu_api_secret

# reCAPTCHA
recaptcha.secret=tu_secret_key

# Frontend URL
frontend.url=http://localhost:4200
```

---

## 🔒 Seguridad

### Capas de Seguridad Implementadas

✅ **Autenticación JWT** con refresh tokens (7 días)  
✅ **2FA por email** (código 6 dígitos, expira en 3 min)  
✅ **Contraseñas cifradas** con BCrypt  
✅ **RBAC** (Role-Based Access Control)  
✅ **Validación de entradas** en frontend y backend  
✅ **Protección anti fuerza bruta** con bloqueo temporal  
✅ **reCAPTCHA v2** en login y registro  
✅ **HTTPS** en producción  
✅ **Auditoría completa** de acciones críticas  
✅ **Tokens con expiración** temporal  
✅ **Cierre automático** por inactividad (15-60 min según rol)  
✅ **Restricciones de integridad** a nivel de BD (25+ CHECK constraints)

### Política de Contraseñas

- Mínimo 8 caracteres
- Al menos 1 letra mayúscula
- Al menos 1 número
- Al menos 1 carácter especial

### Flujo de Autenticación


```
1. POST /auth/login (email + password + reCAPTCHA)
   ↓
2. Sistema envía código 2FA al email (expira en 3 min)
   ↓
3. POST /auth/verify-2fa (email + código)
   ↓
4. Sistema retorna JWT (1h) + Refresh Token (7 días)
   ↓
5. Frontend renueva JWT automáticamente 5 min antes de expirar
```

---

## � API REST

### Especificación OpenAPI

La API completa está documentada en formato OpenAPI 3.0:

📄 **[openapi-pawsoft.yaml](openapi-pawsoft.yaml)**

### Endpoints Principales

| Módulo | Endpoint | Método | Autenticación |
|--------|----------|--------|---------------|
| **Auth** | `/auth/login` | POST | No |
| | `/auth/verify-2fa` | POST | No |
| | `/auth/register` | POST | No |
| **Admin** | `/api/admin/users` | GET, POST | Admin |
| | `/api/admin/payments/stats` | GET | Admin |
| **Recepcionista** | `/api/recepcionista/appointments` | GET, POST | Recepcionista |
| | `/api/recepcionista/payments` | GET, POST | Recepcionista |
| **Veterinario** | `/api/vet/appointments` | GET | Veterinario |
| | `/api/vet/medical-records` | POST | Veterinario |
| **Cliente** | `/api/client/appointments` | GET, POST | Cliente |
| | `/api/client/pets` | GET, POST | Cliente |

**Autenticación:** Todos los endpoints protegidos requieren header:
```
Authorization: Bearer <jwt_token>
```

---

## 🗄️ Base de Datos

### Modelo Relacional

El sistema utiliza **MySQL 8.0** con **25+ tablas** principales:

- `users` - Usuarios del sistema (4 roles)
- `pets` - Mascotas registradas
- `appointments` - Citas veterinarias
- `medical_records` - Historial clínico
- `payments` - Registro de pagos
- `payment_items` - Facturación detallada
- `payment_adjustments` - Ajustes con auditoría
- `medication_catalog` - Catálogo de medicamentos
- `vaccine_catalog` - Catálogo de vacunas
- `service_prices` - Precios de servicios
- `audit_logs` - Auditoría de acciones
- `refresh_tokens` - Tokens de sesión
- `codigos_2fa` - Códigos de autenticación

### Migraciones con Flyway

El sistema utiliza **Flyway** para gestión de migraciones:

- `V1__Initial_Schema.sql` - Esquema inicial
- `V2__Add_Refresh_Tokens.sql` - Sistema de refresh tokens
- `V3__Add_Payment_System.sql` - Sistema de pagos
- `V4__Add_Medical_Records.sql` - Registros médicos
- `V5__Add_Medical_Profile_Hospitalization.sql` - Perfiles y hospitalización
- `V6__Add_Integrity_Constraints.sql` - **25+ CHECK constraints** (Rating 5/5)

**Documentación completa:** [docs/modelo-base-datos.md](docs/modelo-base-datos.md)

---

## 🧪 Testing

### Cobertura de Pruebas

El proyecto incluye **32 pruebas unitarias** que validan:

- ✅ Servicios de autenticación y seguridad
- ✅ Lógica de negocio de pagos
- ✅ Gestión de perfiles con 2FA
- ✅ Validación de reCAPTCHA
- ✅ Manejo de excepciones

```bash
# Ejecutar tests
./mvnw test

# Con cobertura
./mvnw test jacoco:report
```

---

## 📚 Documentación

| Documento | Descripción |
|-----------|-------------|
| 🗄️ [**Base de Datos**](docs/modelo-base-datos.md) | Modelo completo con 25+ tablas y relaciones |
| 🏛️ [**ADRs**](docs/ADRs.md) | Decisiones arquitectónicas (17 ADRs documentados) |
| 📝 [**CHANGELOG**](docs/CHANGELOG.md) | Historial de cambios y mejoras |
| 🔌 [**OpenAPI**](openapi-pawsoft.yaml) | Especificación completa de la API REST |
---

## 🚀 Despliegue en Producción

### Backend (AWS EC2)

```bash
# Compilar JAR
./mvnw clean package -DskipTests

# Conectar a EC2
ssh -i pawsoft-key.pem ec2-user@[EC2_IP]

# Desplegar
cd /opt/pawsoft/backend
git pull origin main
mvn clean package -DskipTests
sudo systemctl restart pawsoft-backend

# Verificar
sudo systemctl status pawsoft-backend
sudo journalctl -u pawsoft-backend -f
```

### Frontend (AWS S3 + CloudFront)

```bash
# Compilar
npm run build

# Subir a S3
aws s3 sync dist/pawsoft s3://pawsoft-frontend/ --delete

# Invalidar caché
aws cloudfront create-invalidation --distribution-id [ID] --paths "/*"
```

### Backups Automáticos

- **Frecuencia:** Diaria a las 2:00 AM (cron)
- **Retención:** 5 backups más recientes
- **Ubicación:** `/opt/pawsoft/backups/` en EC2
- **Compresión:** Automática (.gz)

---

## 📊 Monitoreo

### Prometheus + Grafana

- **Prometheus:** `http://[EC2_IP]:9090`
- **Grafana:** `http://[EC2_IP]:3000`

**Métricas monitoreadas:**
- Tiempo de respuesta por endpoint
- Requests por segundo
- Errores HTTP (4xx, 5xx)
- Uso de memoria y CPU
- Disponibilidad del servicio

---

## 🎯 Roadmap

### ✅ Completado (v1.0)

- [x] Sistema de autenticación con 2FA
- [x] Gestión de usuarios y roles
- [x] Agendamiento de citas
- [x] Historial clínico digital
- [x] Sistema de pagos con facturación detallada
- [x] Auditoría completa
- [x] Restricciones de integridad en BD 
- [x] Chatbot inteligente con RBAC
---

## 👨‍💻 Equipo de Desarrollo

<div align="center">

### Universidad del Quindío
**Ingeniería de Sistemas y Computación** • **Software III**

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/ValentinaPorras">
        <img src="https://github.com/ValentinaPorras.png" width="100px;" alt="Valentina Porras"/>
        <br />
        <sub><b>Valentina Porras Salazar</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/HelenGiraldo">
        <img src="https://github.com/HelenGiraldo.png" width="100px;" alt="Helen Giraldo"/>
        <br />
        <sub><b>Helen Xiomara Giraldo Libreros</b></sub>
      </a>
    </td>
  </tr>
</table>

**Profesor:** Raúl Yulbraynner Rivera Gálvez

---

📧 **Contacto:** pawsoft.vet@gmail.com  
🔗 **Repositorio:** [github.com/HelenGiraldo/Pawsoft](https://github.com/HelenGiraldo/Pawsoft)  
🌐 **Producción:** [www.pawsoft.online](https://www.pawsoft.online)

</div>

---

## 📄 Licencia

Proyecto académico desarrollado para la Universidad del Quindío.

---

<div align="center">
  <sub>Hecho con ❤️ para mejorar la atención veterinaria</sub>
</div>
