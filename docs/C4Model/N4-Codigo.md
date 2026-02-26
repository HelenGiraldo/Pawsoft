# 2.4 Código (N4)

## 2.4.1 Estructura del repositorio y módulos

El sistema se implementa en dos módulos principales alineados con el Nivel 2 (Contenedores):

- Una aplicación móvil PWA (Onsen UI) como canal del cliente.
- Un Backend en Java (Spring Boot) que centraliza la lógica de negocio e integraciones externas.

---

## Estructura del Repositorio (Árbol)

    /pawsoft-uq/
    │
    ├── /mobile-app-pwa/        (PWA Onsen UI – canal del cliente)
    │   ├── /js/                (Lógica de presentación: ViewModels, controllers)
    │   ├── /pages/             (Páginas de la aplicación)
    │   ├── /components/        (Componentes reutilizables)
    │   ├── /css/               (Estilos de la aplicación)
    │   ├── /utils/             (Helpers, constantes y utilidades)
    │   └── index.html          (Entrada principal de la PWA)
    │
    ├── /backend/               (Backend Java Spring Boot)
    │   ├── src/main/java/co/edu/uniquindio/backendpawsoft/
    │   │   ├── /controller/    (APIs REST: manejo de usuarios, mascotas, citas)
    │   │   ├── /service/       (Lógica de negocio: AuthService, PetService, AppointmentService)
    │   │   ├── /repository/    (Acceso a datos JPA: UserRepository, PetRepository, AppointmentRepository)
    │   │   ├── /model/         (Entities JPA: User, Pet, Appointment)
    │   │   ├── /integration/   (Clientes externos: NotificationClient, StatementStoreClient)
    │   │   └── /config/        (SecurityConfig y configuración general)
    │   │
    │   ├── src/main/resources/
    │   │   ├── application.yml     (Configuración de la app y conexión a MySQL)
    │   │   └── /db/migration/      (Scripts de migración con Flyway/Liquibase)
    │   │
    │   └── pom.xml (o build.gradle)
    │
    └── /docs/
        ├── C4Model/                       (Diagramas C4: N1, N2, N3, N4)
        ├── arquitectura/                  (Descripción general de arquitectura)
        └── decisiones-arquitectonicas/    (Registro de ADRs)

---

## 2.4.2 Descripción Técnica por Módulo

### Módulo Mobile-App-PWA

Responsable de:
- Interfaz de usuario.
- Consumo de APIs REST del backend.
- Gestión de navegación y vistas.
- Manejo de estado básico del cliente.

Tecnologías utilizadas:
- HTML5
- CSS3
- JavaScript
- Onsen UI

La aplicación cliente no contiene lógica de negocio crítica ni validación de seguridad; estas responsabilidades se delegan completamente al backend.

---

### Módulo Backend (Spring Boot)

Responsable de:
- Exposición de APIs REST.
- Autenticación y autorización (JWT).
- Lógica de negocio centralizada.
- Persistencia de datos.
- Integraciones externas.

Capas principales:

Controller  
Define los endpoints REST y recibe solicitudes HTTP.

Service  
Contiene la lógica de negocio y reglas del sistema.

Repository  
Interacción con base de datos mediante Spring Data JPA.

Model  
Entidades JPA que representan el dominio del sistema.

Integration  
Clientes para comunicación con servicios externos.

Config  
Configuración de seguridad, CORS, JWT y parámetros globales.

---

## 2.4.3 Principios Aplicados

- Arquitectura en capas
- Separación de responsabilidades
- Seguridad centralizada en backend
- Diseño RESTful
- Persistencia desacoplada mediante JPA
- Migraciones versionadas de base de datos

---

## 2.4.4 Alineación con C4 Nivel 2 y 3

- El módulo mobile-app-pwa corresponde al contenedor "Frontend".
- El módulo backend corresponde al contenedor "API Backend".
- Las carpetas internas del backend representan los componentes definidos en el Nivel 3 del modelo C4.
