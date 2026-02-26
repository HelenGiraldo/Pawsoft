# 🐾 PawSoft - Sistema de Gestión Veterinaria

---

## 1. Resumen Arquitectónico

PawSoft es un sistema de gestión veterinaria cuyo propósito es centralizar y automatizar los procesos clínicos y administrativos de clínicas veterinarias pequeñas y medianas.

La plataforma permite gestionar:

- Programación de citas
- Registro de historial clínico
- Control básico de inventario de medicamentos

El sistema reemplaza procesos manuales tradicionales que generan desorden, pérdida de información y errores en el seguimiento de pacientes.

PawSoft funciona mediante una aplicación web progresiva (PWA) para clientes y aplicaciones web administrativas para el personal interno. El backend expone una API REST desarrollada en Java con Spring Boot, organizada bajo una arquitectura en capas.

El sistema centraliza la información de clientes, mascotas y procesos clínicos en una única plataforma, facilitando el acceso a datos confiables, optimizando los flujos de trabajo y mejorando la eficiencia operativa.

---

## 1.1 Usuarios Principales

### Cliente
- Solicita, modifica o cancela citas veterinarias.
- Consulta información básica de sus mascotas.
- Accede al sistema mediante una aplicación móvil (PWA).

### Veterinario
- Registra atención médica.
- Registra diagnósticos, tratamientos y vacunas.
- Actualiza historial clínico.
- Usa aplicación web.

### Recepcionista
- Agenda, cancela o modifica citas.
- Confirma asistencia.
- Gestiona usuarios básicos del sistema.
- Usa aplicación web.

### Administrador
- Gestiona usuarios médicos.
- Supervisa operaciones del negocio.
- Administra inventario.
- Usa aplicación web.

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

## 1.4 Procesos del Sistema

### Gestión de Usuarios
- Registro de usuarios.
- Autenticación e inicio de sesión.
- Asignación de roles.
- Gestión de perfiles.

### Gestión de Citas
- Solicitud y agendamiento.
- Verificación de disponibilidad.
- Confirmación de asistencia.
- Cambio de estados: Agendada, Confirmada, En Curso, Atendida, No Asistió, Cancelada.
- Modificación y cancelación.

### Gestión de Historial Clínico
- Registro de atención médica.
- Registro de diagnóstico y tratamiento.
- Registro de medicamentos formulados.
- Registro de vacunas.
- Actualización de historial.

### Control de Inventario
- Registro de productos.
- Actualización de existencias.
- Descuento por consumo.
- Control de niveles mínimos.
- Alertas por stock o caducidad.

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
- Java
- Spring Boot
- Spring Security
- JWT
- JPA / Hibernate

## Base de Datos
- MySQL (Relacional)

## Frontend
- HTML5
- CSS3
- JavaScript
- Onsen UI (PWA)

## Cloud
- AWS (Amazon Web Services)
- Amazon S3

## CI/CD
- Commit → Build → Test → Artifact → Deploy → Monitoring

---

# 4. Seguridad

- Autenticación basada en JWT.
- Autorización por roles.
- Seguridad centralizada en backend.
- Protección de endpoints mediante Spring Security.

---

# 5. Estado del Proyecto

En desarrollo 
