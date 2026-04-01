# Herramientas y Dependencias — PawSoft

**Proyecto:** PawSoft — Sistema de gestión veterinaria  
**Universidad:** Universidad del Quindío  
**Programa:** Ingeniería de Sistemas y Computación  
**Materia:** Software III  
**Autoras:** Valentina Porras Salazar · Helen Xiomara Giraldo Libreros  
**Profesor:** Raúl Yulbraynner Rivera Gálvez  

---

## 1. Herramientas de Desarrollo

### 1.1 Control de Versiones

**Git**
- Propósito: Control de versiones del código fuente
- Uso: Gestión de ramas, commits, historial de cambios
- Convención de commits: `feat:`, `fix:`, `docs:`, `refactor:`

**GitHub**
- Propósito: Repositorio remoto y colaboración
- Repositorio: `https://github.com/HelenGiraldo/Pawsoft`

### 1.2 Gestión de Proyectos

**Jira**
- Propósito: Gestión de tareas, sprints y seguimiento de proyecto
- Uso: Planificación de sprints, historias de usuario, seguimiento de bugs

### 1.3 Entornos de Desarrollo

**IntelliJ IDEA**
- Propósito: IDE para desarrollo backend Java
- Uso: Desarrollo, debugging, ejecución de tests

**Visual Studio Code**
- Propósito: Editor para desarrollo frontend Angular
- Uso: Desarrollo, debugging, extensiones de Angular

---

## 2. Backend — Dependencias Java

### 2.1 Framework Principal

**Spring Boot 4.0.2**
- Licencia: Apache 2.0
- Propósito: Framework base para desarrollo de API REST
- Uso: Configuración automática, servidor embebido, gestión de dependencias

### 2.2 Dependencias de Spring

**spring-boot-starter-data-jpa**
- Licencia: Apache 2.0
- Propósito: Acceso a datos con JPA/Hibernate
- Uso: Mapeo objeto-relacional, repositorios, consultas a base de datos

**spring-boot-starter-security**
- Licencia: Apache 2.0
- Propósito: Seguridad y autenticación
- Uso: Autenticación JWT, control de acceso por roles, protección de endpoints

**spring-boot-starter-webmvc**
- Licencia: Apache 2.0
- Propósito: Desarrollo de API REST
- Uso: Controllers, manejo de peticiones HTTP, serialización JSON

**spring-boot-starter-validation**
- Licencia: Apache 2.0
- Propósito: Validación de datos
- Uso: Validación de DTOs con anotaciones `@Valid`, `@NotBlank`, `@Email`

**spring-boot-starter-mail**
- Licencia: Apache 2.0
- Propósito: Envío de correos electrónicos
- Uso: Códigos 2FA, recuperación de contraseña, verificación de email

**spring-boot-starter-actuator**
- Licencia: Apache 2.0
- Propósito: Monitoreo y métricas de aplicación
- Uso: Endpoint `/actuator/prometheus` para exportar métricas

### 2.3 Base de Datos

**mysql-connector-j**
- Licencia: GPL 2.0 con excepción FOSS
- Propósito: Driver JDBC para MySQL
- Uso: Conexión entre Spring Boot y MySQL



### 2.4 Seguridad

**JJWT 0.11.5** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- Licencia: Apache 2.0
- Propósito: Generación y validación de tokens JWT
- Uso: Autenticación stateless, tokens de acceso y refresh tokens

### 2.5 Utilidades

**Lombok 1.18.30**
- Licencia: MIT
- Propósito: Reducción de código boilerplate
- Uso: Generación automática de getters, setters, constructores con anotaciones

### 2.6 Monitoreo

**Micrometer Registry Prometheus**
- Licencia: Apache 2.0
- Propósito: Exportación de métricas en formato Prometheus
- Uso: Métricas de negocio, tiempos de respuesta, uso de recursos

### 2.7 Testing

**JUnit 6.0.2** (transitivo desde Spring Boot)
- Licencia: EPL 2.0
- Propósito: Framework de testing
- Uso: Ejecución de 32 pruebas unitarias

**Mockito 5.20.0** (transitivo desde Spring Boot)
- Licencia: MIT
- Propósito: Mocking de dependencias en tests
- Uso: Simulación de servicios, repositorios y componentes externos en tests unitarios

### 2.8 Gestión de Dependencias

**Maven 3.6+**
- Licencia: Apache 2.0
- Propósito: Gestión de dependencias y build del proyecto
- Uso: Compilación, ejecución de tests, empaquetado de JAR
- Archivo de configuración: `pom.xml`

**Nota sobre dependencias transitivas:**
- JUnit, Mockito, AssertJ y otras herramientas de testing vienen incluidas transitivamente a través de `spring-boot-starter-test`
- No es necesario declararlas explícitamente en el `pom.xml`

---

## 3. Frontend — Dependencias JavaScript

### 3.1 Framework Principal

**Angular 20.0.0**
- Licencia: MIT
- Propósito: Framework para desarrollo de aplicación web
- Uso: Componentes, routing, formularios reactivos, servicios

### 3.2 UI Framework

**Ionic 8.0.0**
- Licencia: MIT
- Propósito: Componentes UI móviles y web
- Uso: Interfaz de usuario, navegación, modales, alertas

**Ionicons 7.0.0**
- Licencia: MIT
- Propósito: Iconos para la interfaz
- Uso: Iconos en botones, menús y navegación

### 3.3 Utilidades

**RxJS 7.8.0**
- Licencia: Apache 2.0
- Propósito: Programación reactiva
- Uso: Observables, manejo de eventos asíncronos, HTTP requests

**Zone.js 0.15.0**
- Licencia: MIT
- Propósito: Detección de cambios en Angular
- Uso: Change detection automático

**TypeScript 5.9.0**
- Licencia: Apache 2.0
- Propósito: Lenguaje tipado sobre JavaScript
- Uso: Desarrollo con tipos estáticos, compilación a JavaScript

### 3.4 Testing

**Jasmine 5.1.0**
- Licencia: MIT
- Propósito: Framework de testing
- Uso: Escritura de tests unitarios

**Karma 6.4.0**
- Licencia: MIT
- Propósito: Test runner
- Uso: Ejecución de tests en navegador

### 3.5 Linting y Calidad

**ESLint 9.16.0**
- Licencia: MIT
- Propósito: Análisis estático de código
- Uso: Detección de errores, aplicación de estándares de código

**@angular-eslint 20.0.0**
- Licencia: MIT
- Propósito: Reglas de ESLint específicas para Angular
- Uso: Validación de buenas prácticas de Angular

**@typescript-eslint 8.18.0**
- Licencia: MIT
- Propósito: Reglas de ESLint para TypeScript
- Uso: Validación de código TypeScript

### 3.6 Build Tools

**Angular CLI 20.0.0**
- Licencia: MIT
- Propósito: Herramienta de línea de comandos para Angular
- Uso: Generación de componentes, build, desarrollo local

**@ionic/angular-toolkit 12.0.0**
- Licencia: MIT
- Propósito: Integración de Ionic con Angular CLI
- Uso: Build optimizado para aplicaciones Ionic

### 3.7 Gestión de Dependencias

**npm**
- Licencia: Artistic 2.0
- Propósito: Gestión de dependencias JavaScript
- Uso: Instalación de paquetes, scripts de build
- Archivo de configuración: `package.json`

---

## 4. Servicios Externos

### 4.1 Almacenamiento de Imágenes

**Cloudinary**
- Propósito: Almacenamiento y gestión de imágenes en la nube
- Uso: Fotos de mascotas, fotos adjuntas en registros médicos
- Límite: 2MB por imagen
- Integración: Frontend sube directamente a Cloudinary API, backend solo almacena URLs

### 4.2 Envío de Emails

**SMTP (Gmail)**
- Propósito: Envío de correos electrónicos
- Uso: Códigos 2FA, recuperación de contraseña, verificación de email
- Configuración: `spring.mail.*` en `application.properties`

### 4.3 Protección contra Bots

**Google reCAPTCHA v2**
- Propósito: Validación de que el usuario es humano
- Uso: Login y registro de usuarios
- Validación: Backend verifica el token con API de Google

---

## 5. Infraestructura

### 5.1 Servicios AWS

**EC2 (Elastic Compute Cloud)**
- Propósito: Servidor para backend Spring Boot
- Instancia: t2.micro o superior
- Sistema operativo: Amazon Linux 2
- Uso: Ejecución del JAR de Spring Boot con systemd

**RDS (Relational Database Service)**
- Propósito: Base de datos MySQL gestionada
- Motor: MySQL 8.0
- Instancia: db.t3.micro
- Uso: Almacenamiento de datos de producción

**S3 (Simple Storage Service)**
- Propósito: Almacenamiento de archivos estáticos del frontend
- Uso: Hosting de archivos compilados de Angular

**CloudFront**
- Propósito: CDN para distribución del frontend
- Uso: Entrega rápida de contenido, certificado SSL, caché

**Certificate Manager**
- Propósito: Gestión de certificados SSL/TLS
- Uso: HTTPS en frontend y backend

### 5.2 Monitoreo

**Prometheus**
- Licencia: Apache 2.0
- Propósito: Recolección y almacenamiento de métricas
- URL: `http://3.135.224.139:9090`
- Uso: Scraping de métricas del endpoint `/actuator/prometheus`

**Grafana**
- Licencia: AGPL 3.0
- Propósito: Visualización de métricas
- URL: `http://3.135.224.139:3000`
- Uso: Dashboards de métricas de negocio y sistema

### 5.3 Backups

**mysqldump**
- Propósito: Exportación de base de datos MySQL
- Uso: Backups automáticos diarios con cron
- Script: `/home/ec2-user/backup-db.sh`

**cron**
- Propósito: Programación de tareas automáticas
- Uso: Ejecución diaria de backups a las 2:00 AM UTC

---

## 6. Resumen de Licencias

Todas las dependencias utilizadas tienen licencias permisivas que permiten uso comercial y académico:

| Licencia | Dependencias |
|---|---|
| Apache 2.0 | Spring Boot, JJWT, RxJS, TypeScript, Prometheus, Micrometer |
| MIT | Angular, Ionic, Lombok, Jasmine, Karma, ESLint |
| GPL 2.0 con excepción FOSS | MySQL Connector |
| AGPL 3.0 | Grafana (solo para visualización) |

No se utilizan dependencias con licencias restrictivas que impidan el uso académico o comercial del proyecto.

---

## 7. Instalación de Herramientas

### 7.1 Backend

**Requisitos:**
- Java 17 o superior
- Maven 3.6 o superior
- MySQL 8.0 o superior

**Instalación de dependencias:**
```bash
cd backendPawsoft
./mvnw clean install
```

### 7.2 Frontend

**Requisitos:**
- Node.js 18 o superior
- npm 9 o superior

**Instalación de dependencias:**
```bash
cd Front-end-pawsoft
npm install
```

### 7.3 Infraestructura

**AWS CLI**
- Propósito: Gestión de recursos AWS desde línea de comandos
- Uso: Despliegue de frontend a S3, invalidación de caché CloudFront

**Instalación:**
```bash
brew install awscli  # macOS
```

---

## 8. Dependencias por Propósito

### 8.1 Autenticación y Seguridad

| Dependencia | Versión | Propósito |
|---|---|---|
| spring-boot-starter-security | 4.0.2 | Autenticación y autorización |
| jjwt-api | 0.11.5 | Generación y validación de JWT |
| jjwt-impl | 0.11.5 | Implementación de JJWT |
| jjwt-jackson | 0.11.5 | Serialización JSON para JWT |

### 8.2 Base de Datos

| Dependencia | Versión | Propósito |
|---|---|---|
| spring-boot-starter-data-jpa | 4.0.2 | ORM con Hibernate |
| mysql-connector-j | 9.5.0 | Driver MySQL |

### 8.3 Validación

| Dependencia | Versión | Propósito |
|---|---|---|
| spring-boot-starter-validation | 4.0.2 | Validación de entradas con Bean Validation |

### 8.4 Comunicación

| Dependencia | Versión | Propósito |
|---|---|---|
| spring-boot-starter-mail | 4.0.2 | Envío de emails |

### 8.5 Monitoreo

| Dependencia | Versión | Propósito |
|---|---|---|
| spring-boot-starter-actuator | 4.0.2 | Endpoints de métricas |
| micrometer-registry-prometheus | (gestionado por Spring) | Exportación de métricas a Prometheus |

### 8.6 Utilidades

| Dependencia | Versión | Propósito |
|---|---|---|
| lombok | 1.18.30 | Reducción de código boilerplate |

### 8.7 Testing

| Dependencia | Versión | Propósito |
|---|---|---|
| JUnit Jupiter | 6.0.2 | Framework de testing (transitivo) |
| Mockito | 5.20.0 | Mocking de dependencias (transitivo) |
| AssertJ | 3.27.6 | Assertions fluidas (transitivo) |

Nota: Estas dependencias vienen incluidas transitivamente a través de los starters de Spring Boot Test.

---

## 9. Frontend — Dependencias por Propósito

### 9.1 Framework y UI

| Dependencia | Versión | Propósito |
|---|---|---|
| @angular/core | 20.0.0 | Framework base |
| @angular/common | 20.0.0 | Directivas y pipes comunes |
| @angular/forms | 20.0.0 | Formularios reactivos |
| @angular/router | 20.0.0 | Navegación entre páginas |
| @ionic/angular | 8.0.0 | Componentes UI móviles |
| ionicons | 7.0.0 | Iconos |

### 9.2 Utilidades

| Dependencia | Versión | Propósito |
|---|---|---|
| rxjs | 7.8.0 | Programación reactiva |
| zone.js | 0.15.0 | Change detection |
| tslib | 2.3.0 | Helpers de TypeScript |

### 9.3 Testing

| Dependencia | Versión | Propósito |
|---|---|---|
| @angular/cli | 20.0.0 | CLI de Angular |
| @angular-devkit/build-angular | 20.0.0 | Build de aplicación |
| @ionic/angular-toolkit | 12.0.0 | Integración Ionic-Angular |
| typescript | 5.9.0 | Compilador TypeScript |

| Dependencia | Versión | Propósito |
|---|---|---|
| jasmine-core | 5.1.0 | Framework de testing |
| karma | 6.4.0 | Test runner |
| karma-jasmine | 5.1.0 | Adaptador Jasmine-Karma |
| karma-chrome-launcher | 3.2.0 | Ejecución en Chrome |

### 9.4 Linting

| Dependencia | Versión | Propósito |
|---|---|---|
| eslint | 9.16.0 | Linter JavaScript/TypeScript |
| @angular-eslint/eslint-plugin | 20.0.0 | Reglas Angular |
| @typescript-eslint/eslint-plugin | 8.18.0 | Reglas TypeScript |

### 9.5 Desarrollo y Build

| Dependencia | Versión | Propósito |
|---|---|---|
| @angular/cli | 20.0.0 | CLI de Angular |
| @angular-devkit/build-angular | 20.0.0 | Build de aplicación |
| @ionic/angular-toolkit | 12.0.0 | Integración Ionic-Angular |
| typescript | 5.9.0 | Compilador TypeScript |

### 9.6 Gestión de Dependencias

**npm**
- Licencia: Artistic 2.0
- Propósito: Gestión de dependencias JavaScript
- Uso: Instalación de paquetes, scripts de build
- Archivo de configuración: `package.json`

---

## 10. Servicios Externos y APIs

### 10.1 Cloudinary

**Propósito:** Almacenamiento de imágenes en la nube  
**Uso en el proyecto:**
- Subida de fotos de mascotas
- Subida de fotos adjuntas en registros médicos

**Integración:**
- Frontend sube directamente a Cloudinary API con upload preset
- Backend solo almacena las URLs devueltas por Cloudinary
- Límite: 2MB por imagen

### 10.2 Google reCAPTCHA v2

**Propósito:** Protección contra bots y automatización  
**Uso en el proyecto:**
- Validación en login
- Validación en registro de usuarios

**Integración:**
- Frontend: Widget de reCAPTCHA en formularios
- Backend: Validación del token con API de Google

### 10.3 SMTP (Gmail)

**Propósito:** Envío de correos electrónicos  
**Uso en el proyecto:**
- Códigos 2FA (6 dígitos, expiran en 3 minutos)
- Recuperación de contraseña (token expira en 30 minutos)
- Verificación de email (token expira en 1 hora)

**Integración:**
- Backend: Spring Mail con credenciales SMTP configuradas

---

## 11. Herramientas de Infraestructura

### 11.1 Despliegue

**systemd**
- Propósito: Gestión de servicios en Linux
- Uso: Ejecución del backend como servicio en EC2
- Configuración: `/etc/systemd/system/pawsoft-backend.service`

**AWS CLI**
- Propósito: Gestión de recursos AWS
- Uso: Despliegue de frontend, invalidación de caché

### 11.2 Monitoreo

**Prometheus**
- Propósito: Recolección de métricas
- Configuración: Scraping del endpoint `/actuator/prometheus` cada 15 segundos
- Almacenamiento: Retención de 15 días

**Grafana**
- Propósito: Visualización de métricas
- Dashboards: PawSoft Métricas de Negocio
- Actualización: Cada 30 segundos

### 11.3 Backups

**mysqldump**
- Propósito: Exportación de base de datos
- Uso: Backups diarios automáticos
- Retención: 7 días

**cron**
- Propósito: Programación de tareas
- Uso: Ejecución diaria de script de backup a las 2:00 AM UTC

---

## 12. Verificación de Dependencias

### 12.1 Backend

**Listar dependencias:**
```bash
cd backendPawsoft
./mvnw dependency:tree
```

**Verificar actualizaciones:**
```bash
./mvnw versions:display-dependency-updates
```

### 12.2 Frontend

**Listar dependencias:**
```bash
cd Front-end-pawsoft
npm list
```

**Verificar actualizaciones:**
```bash
npm outdated
```

---

## 13. Actualización de Dependencias

### 13.1 Política de Actualización

**Parches de seguridad:** Aplicar inmediatamente  
**Versiones menores:** Revisar mensualmente  
**Versiones mayores:** Evaluar impacto antes de actualizar

### 13.2 Proceso de Actualización

1. Verificar actualizaciones disponibles
2. Revisar changelog de la dependencia
3. Actualizar en ambiente local
4. Ejecutar tests completos
5. Si los tests pasan, desplegar en producción
6. Monitorear comportamiento post-actualización

---

## 14. Resumen de Cumplimiento

El proyecto documenta:

- Todas las herramientas de desarrollo utilizadas
- Todas las dependencias backend con versiones y propósito
- Todas las dependencias frontend con versiones y propósito
- Todos los servicios externos integrados
- Todas las herramientas de infraestructura
- Licencias de todas las dependencias
- Proceso de instalación y actualización

Esta documentación cumple con el requisito académico de documentar el uso de herramientas y dependencias del proyecto.
