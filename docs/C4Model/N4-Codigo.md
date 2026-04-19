# 2.4 Código (N4)

## 2.4.1 Estructura del repositorio y módulos

El sistema se implementa en dos repositorios principales alineados con el Nivel 2 (Contenedores): una aplicación web progresiva (PWA) desarrollada con Ionic + Angular como frontend y un Backend en Java (Spring Boot) que centraliza la lógica de negocio e integraciones externas.

---

## Estructura del repositorio (árbol)

```
/pawsoft-uq/
│
├── /Front-end-pawsoft/                  # Frontend PWA (Ionic + Angular)
│   └── /src/
│       ├── /app/
│       │   ├── /component/             # Componentes reutilizables (modales, widgets)
│       │   ├── /guards/                # Guards de autenticación y autorización
│       │   ├── /interceptors/          # Interceptores HTTP (token, errores)
│       │   ├── /pages/                 # Páginas de la aplicación por módulo
│       │   │   ├── /auth/              # Autenticación (login, registro, recuperación)
│       │   │   ├── /appointments/      # Dashboards por rol (cliente, recepcionista, veterinario, admin)
│       │   │   │   └── /dashboard-vet/ # Dashboard veterinario con historial clínico
│       │   │   │       ├── /atencion-medica/     # Atención médica y consultas
│       │   │   │       └── /formulario-consulta/ # Formularios de consulta médica
│       │   │   ├── /pet/               # Gestión de mascotas
│       │   │   ├── /admin-payments/    # Panel de pagos del administrador
│       │   │   └── /perfil-cliente/    # Perfil del usuario
│       │   ├── /services/              # Servicios de comunicación con el backend
│       │   └── /share/                 # Componentes compartidos (sidebar, accessibility)
│       │   app.component.ts            # Componente raíz
│       │   app.routes.ts               # Configuración de rutas
│       ├── /assets/                    # Recursos estáticos (imágenes, iconos, i18n)
│       ├── /environments/              # Configuración por entorno (dev, prod)
│       └── /theme/                     # Estilos globales y temas de Ionic
│       index.html                      # Entrada principal de la PWA
│       main.ts                         # Bootstrap de la aplicación Angular
│   /www/                               # Build de producción (generado)
│   angular.json                        # Configuración de Angular CLI
│   capacitor.config.ts                 # Configuración de Capacitor (apps nativas)
│   ionic.config.json                   # Configuración de Ionic
│   package.json                        # Dependencias del proyecto
│   tsconfig.json                       # Configuración de TypeScript
│
└── /backendPawsoft/                     # Backend Java (Spring Boot)
    └── /src/main/java/co/edu/uniquindio/backendpawsoft/
    │   ├── /audit/                      # Sistema de auditoría (logs de acciones)
    │   ├── /config/                     # Configuración de seguridad, CORS, beans
    │   ├── /controller/                 # APIs REST por módulo (Auth, Appointments, Pets, Payments, Users, Medical History)
    │   ├── /dto/                        # Data Transfer Objects (request/response)
    │   ├── /enums/                      # Enumeraciones (roles, estados, tipos de hospitalización)
    │   ├── /exception/                  # Manejo global de excepciones
    │   ├── /model/                      # Entidades JPA (User, Pet, Appointment, Payment, MedicalHistory, Hospitalization, etc.)
    │   ├── /repository/                 # Repositorios JPA (acceso a datos)
    │   ├── /scheduler/                  # Tareas programadas (limpieza de códigos 2FA)
    │   ├── /security/                   # JWT, filtros de autenticación
    │   └── /service/                    # Lógica de negocio (Auth, Appointments, Pets, Payments, Email, MedicalHistory, etc.)
    │   BackendPawsoftApplication.java   # Clase principal de Spring Boot
    ├── /src/main/resources/
    │   application.properties           # Configuración de la aplicación y MySQL
    ├── /docs/                           # Documentación del proyecto
    │   ├── /C4Model/                    # Diagramas C4 (contexto, contenedores, componentes)
    │   ├── /arquitectura/               # Diagramas y descripción de arquitectura
    │   └── /decisiones-arquitectonicas/ # Registro de decisiones importantes (ADRs)
    pom.xml                              # Dependencias Maven
    openapi-pawsoft.yaml                 # Especificación OpenAPI 3.0 de la API
```

---

## 2.4.2 Descripción técnica por módulo

### Módulo Front-end-pawsoft (PWA Ionic + Angular)

Responsable de:
- Interfaz de usuario adaptable a móvil, tablet y escritorio.
- Consumo de APIs REST del backend mediante servicios Angular.
- Gestión de navegación y rutas protegidas por guards.
- Manejo de sesión y token JWT mediante interceptores HTTP.
- Experiencia PWA: instalable, con soporte offline básico.

Tecnologías utilizadas:
- Angular 20
- Ionic Framework
- TypeScript
- SCSS

La aplicación cliente no contiene lógica de negocio crítica ni validación de seguridad definitiva; estas responsabilidades se delegan al backend.

---

### Módulo backendPawsoft (Spring Boot)

Responsable de:
- Exposición de APIs REST sobre HTTPS.
- Autenticación y autorización (JWT + 2FA).
- Lógica de negocio centralizada.
- Persistencia de datos con JPA / Hibernate sobre MySQL.
- Integraciones externas (Cloudinary, Gmail SMTP, reCAPTCHA).
- Auditoría de acciones críticas.
- Gestión de historiales clínicos y hospitalización.
- Manejo de archivos médicos con Cloudinary.
- Monitoreo de métricas con Prometheus y Grafana.

Capas principales:

| Capa | Responsabilidad |
|---|---|
| `controller` | Define los endpoints REST y recibe solicitudes HTTP |
| `service` | Contiene la lógica de negocio y reglas del sistema |
| `repository` | Interacción con base de datos mediante Spring Data JPA |
| `model` | Entidades JPA que representan el dominio del sistema |
| `security` | Filtros JWT, configuración de Spring Security |
| `config` | Configuración de CORS, beans, Cloudinary, reCAPTCHA |
| `dto` | Objetos de transferencia de datos (request / response) |
| `audit` | Registro de acciones críticas en tabla `audit_log` |
| `scheduler` | Limpieza periódica de tokens y códigos 2FA expirados |
| `exception` | Manejo global de errores con `@ControllerAdvice` |

### Nuevos componentes del sistema de historial clínico:

| Componente | Descripción |
|---|---|
| `MedicalHistoryController` | Endpoints para gestión de historiales médicos |
| `HospitalizationController` | APIs para manejo de hospitalizaciones |
| `MedicalProfileService` | Lógica de negocio para perfiles médicos |
| `FileAttachmentService` | Gestión de archivos médicos con Cloudinary |
| `HospitalizationType` (enum) | Tipos de hospitalización disponibles |
| `MedicalHistory` (model) | Entidad para historiales clínicos |
| `Hospitalization` (model) | Entidad para registros de hospitalización |

---

## 2.4.3 Principios aplicados

- Arquitectura en capas (Controller → Service → Repository → Model)
- Separación de responsabilidades
- Seguridad centralizada en backend (JWT stateless + RBAC)
- Diseño RESTful con versionado de endpoints
- Persistencia desacoplada mediante JPA
- Inmutabilidad del historial financiero (snapshot en `payments`)
- Trazabilidad mediante auditoría de acciones críticas

---

## 2.4.4 Alineación con C4 Nivel 2 y 3

- El módulo `Front-end-pawsoft` corresponde al contenedor "Frontend PWA" del Nivel 2.
- El módulo `backendPawsoft` corresponde al contenedor "API Backend" del Nivel 2.
- Las carpetas internas del backend (`controller`, `service`, `repository`, etc.) representan los componentes definidos en el Nivel 3 del modelo C4.
