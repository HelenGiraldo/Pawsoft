# Decisiones de Arquitectura (ADRs) — PawSoft

---

## ADR-01: Canal del cliente mediante aplicación web progresiva

**Decisión:** Implementar el canal del cliente como una aplicación web progresiva (PWA) desarrollada con Ionic + Angular.

**Contexto:** El cliente requiere gestionar citas, consultar el historial de sus mascotas y recibir notificaciones desde distintos dispositivos: Android, iOS o navegador.

**Alternativas consideradas:**
- Aplicación nativa Android
- Aplicación web tradicional con Bootstrap

**Justificación:** Permite una única base de código adaptable a múltiples dispositivos, cumpliendo el requisito académico de PWA y ofreciendo una experiencia similar a la de una aplicación móvil.

**Consecuencias:** Dependencia del navegador y menor acceso a funcionalidades nativas frente a apps totalmente nativas.

---

## ADR-02: Backend como API REST en arquitectura en capas

**Decisión:** Implementar un backend independiente, expuesto como servicios API REST sobre HTTPS, estructurado en arquitectura en capas (Controller → Service → Repository → Model).

**Contexto:** La app móvil necesita consumir servicios de autenticación, gestión de citas e historial clínico, manteniendo separada la lógica de negocio de la interfaz del cliente.

**Alternativas consideradas:**
- Backend con renderizado server-side
- Arquitectura de microservicios

**Justificación:** REST facilita la comunicación cliente-servidor y la arquitectura en capas mejora la organización y la mantenibilidad del backend sin añadir complejidad innecesaria.

**Consecuencias:** Se deben versionar endpoints, documentar la API y manejar aspectos de seguridad y errores de red.

---

## ADR-03: Tecnología del backend: Java + Spring Boot

**Decisión:** Desarrollar el backend en Java utilizando Spring Boot.

**Contexto:** Se requiere un framework estable para construir APIs, gestionar seguridad y conectarse con bases de datos.

**Alternativas consideradas:**
- Node.js / Express
- Django

**Justificación:** Spring Boot ofrece un ecosistema estable y productivo para APIs empresariales, con soporte nativo para seguridad, JPA y testing.

**Consecuencias:** Mayor estructura y configuración que soluciones más ligeras.

---

## ADR-04: Persistencia en MySQL

**Decisión:** Usar MySQL como base de datos principal.

**Contexto:** El sistema maneja datos relacionales como usuarios, citas e historial clínico, que requieren integridad y consistencia.

**Alternativas consideradas:**
- PostgreSQL
- NoSQL (MongoDB)
- Archivos planos

**Justificación:** El modelo relacional favorece la integridad referencial y las consultas confiables para historial y trazabilidad.

**Consecuencias:** Requiere diseño de esquema y gestión de migraciones.

---

## ADR-05: Acceso a datos mediante repositorios y ORM

**Decisión:** Encapsular el acceso a datos en una capa de Repository usando ORM (JPA / Hibernate).

**Contexto:** Evitar acceso directo a la base de datos desde los controladores y mantener separación de responsabilidades.

**Alternativas consideradas:**
- SQL directo en controladores
- DAOs sin ORM

**Justificación:** Mejora la mantenibilidad, facilita las pruebas y garantiza consistencia en el acceso a datos.

**Consecuencias:** Curva de aprendizaje y necesidad de ajustar consultas para rendimiento en casos complejos.

---

## ADR-06: Gestión de autenticación mediante JWT

**Decisión:** Implementar autenticación basada en JWT en el backend.

**Contexto:** El sistema requiere que clientes y administradores inicien sesión de forma segura desde la app móvil.

**Alternativas consideradas:**
- Sesiones tradicionales en el servidor
- Autenticación básica HTTP

**Justificación:** Autenticación stateless, escalable y segura para aplicaciones móviles. No requiere estado en el servidor.

**Consecuencias:** Se debe manejar la expiración de tokens y la protección contra uso indebido (robo de token).

---

## ADR-07: Autenticación de dos factores (2FA) mediante códigos OTP por correo

**Decisión:** Implementar 2FA con códigos OTP de 6 dígitos enviados por email.

**Contexto:** El sistema maneja información sensible de salud. Se requiere seguridad adicional más allá de usuario y contraseña.

**Alternativas consideradas:**
- SMS
- Google Authenticator
- Biometría
- Solo contraseña

**Justificación:** El correo electrónico es universal, gratuito y balancea seguridad con usabilidad. Los códigos expiran en 3 minutos.

**Consecuencias:** Dependencia de Gmail SMTP. Los usuarios deben acceder a su correo durante el login. Requiere scheduler para limpiar códigos expirados.

---

## ADR-08: Despliegue en nube pública (AWS)

**Decisión:** Desplegar la aplicación en Amazon Web Services, utilizando servicios de cómputo y almacenamiento como Amazon EC2 y Amazon S3.

**Contexto:** El proyecto requiere un entorno de despliegue real en nube pública para simular condiciones de producción.

**Alternativas consideradas:**
- Microsoft Azure
- Google Cloud
- Despliegue local

**Justificación:** AWS ofrece alta disponibilidad, escalabilidad y un nivel gratuito suficiente para el entorno académico.

**Consecuencias:** Dependencia del proveedor de nube y necesidad de controlar recursos para evitar costos adicionales.

---

## ADR-09: Almacenamiento de imágenes en Cloudinary

**Decisión:** Usar Cloudinary para almacenar fotos de veterinarios, mascotas e imágenes médicas importantes.

**Contexto:** Se requiere almacenar y servir imágenes desde múltiples dispositivos con optimización automática.

**Alternativas consideradas:**
- Base de datos (BLOB)
- Google Cloud Storage

**Justificación:** Cloudinary ofrece CDN global, transformaciones automáticas, upload directo desde frontend y tier gratuito suficiente para el proyecto.

**Consecuencias:** Dependencia de servicio externo. Solo se guardan URLs en la base de datos. Posible costo si se excede el tier gratuito.

---

## ADR-10: Protección anti-bots con Google reCAPTCHA v2

**Decisión:** Implementar reCAPTCHA v2 en registro y login.

**Contexto:** Los endpoints de autenticación son vulnerables a ataques de fuerza bruta y bots automatizados.

**Alternativas consideradas:**
- reCAPTCHA v3
- hCaptcha
- Sin protección

**Justificación:** reCAPTCHA v2 es validado en el backend, gratuito y no afecta negativamente la experiencia del usuario.

**Consecuencias:** Dependencia de Google. Requiere claves de sitio y secretas configuradas en frontend y backend.

---

## ADR-11: Notificaciones mediante Gmail SMTP

**Decisión:** Usar Gmail SMTP para el envío de correos del sistema.

**Contexto:** Se requiere enviar verificaciones de cuenta, códigos 2FA, recuperación de contraseña y contraseñas temporales.

**Alternativas consideradas:**
- Amazon SES
- SendGrid
- Mailgun

**Justificación:** Gmail SMTP es gratuito, confiable y fácil de configurar para un entorno académico.

**Consecuencias:** Límite de 500 correos diarios. Requiere App Passwords de Google. No apto para producción a gran escala.

---

## ADR-12: Control de acceso basado en roles (RBAC)

**Decisión:** Implementar RBAC con Spring Security usando 4 roles: `ROLE_CLIENTE`, `ROLE_VETERINARIO`, `ROLE_RECEPCIONISTA`, `ROLE_ADMIN`.

**Contexto:** Diferentes usuarios requieren permisos distintos según sus responsabilidades en el sistema.

**Alternativas consideradas:**
- ABAC (control basado en atributos)
- Permisos granulares por endpoint

**Justificación:** RBAC es simple, se integra nativamente con JWT, usa anotaciones declarativas (`@PreAuthorize`) y es suficiente para los 4 roles bien definidos del sistema.

**Consecuencias:** Roles fijos en código. Cambios de permisos requieren modificación y redespliegue.

---

## ADR-13: Despliegue directo en EC2 con systemd

**Decisión:** Desplegar el backend como JAR ejecutable en EC2 gestionado por systemd.

**Contexto:** Se requiere un método de despliegue simple y confiable para un proyecto académico con recursos limitados.

**Alternativas consideradas:**
- Docker
- Kubernetes
- Serverless

**Justificación:** Despliegue directo con systemd es simple, no requiere aprender Docker, consume menos recursos, y systemd proporciona gestión automática de reinicio y logs.

**Consecuencias:** Menos portabilidad que Docker. Configuración de entorno directamente en el servidor. Dependencia de la versión de Java instalada en EC2.

---

## ADR-14: Distribución de frontend con CloudFront CDN

**Decisión:** Servir el frontend desde S3 y distribuirlo con CloudFront CDN en el dominio `www.pawsoft.online`.

**Contexto:** El frontend debe ser accesible globalmente con baja latencia y HTTPS para funcionar como PWA.

**Alternativas consideradas:**
- Servir desde el backend
- Netlify
- Nginx en EC2

**Justificación:** S3 + CloudFront ofrece distribución global, escalabilidad automática, HTTPS gratuito y separación clara entre frontend y backend.

**Consecuencias:** Configuración inicial compleja. Dependencia de múltiples servicios AWS. Invalidaciones de caché necesarias en cada despliegue.

---

## ADR-15: Tokens de seguridad con expiración temporal

**Decisión:** Implementar tokens con expiración definida: verificación de email (1 hora), reset de contraseña (30 min), códigos 2FA (3 min).

**Contexto:** Se requieren mecanismos seguros para verificar identidad en procesos sensibles sin dejar ventanas de ataque abiertas.

**Alternativas consideradas:**
- Tokens sin expiración
- Tokens de un solo uso sin tiempo límite

**Justificación:** La expiración temporal limita la ventana de ataque, permite limpieza automática y cumple las mejores prácticas de seguridad.

**Consecuencias:** Requiere scheduler para limpieza periódica. Almacenamiento adicional en base de datos.

---

## ADR-16: Auditoría de acciones críticas

**Decisión:** Registrar acciones críticas (citas, pagos, eliminaciones) en tabla `audit_log`.

**Contexto:** Se requiere trazabilidad de quién hizo qué y cuándo, para seguridad y resolución de conflictos.

**Alternativas consideradas:**
- Sin auditoría
- Solo logs de aplicación (archivos de texto)

**Justificación:** La tabla de auditoría proporciona trazabilidad completa, información estructurada y control desde la propia aplicación.

**Consecuencias:** Crecimiento continuo de la tabla. Espacio adicional en base de datos. Requiere política de retención.

---

## ADR-17: Monitoreo con Prometheus y Grafana

**Decisión:** Monitorear el backend con Prometheus (recolección de métricas) y Grafana (visualización) desplegados en el servidor EC2.

**Contexto:** Se requiere visibilidad de métricas de rendimiento, uso de recursos y errores en producción.

**Alternativas consideradas:**
- AWS CloudWatch
- ELK Stack
- Datadog

**Justificación:** Stack open-source gratuito, integración nativa con Spring Boot (Actuator + Micrometer), dashboards personalizables y despliegue sencillo junto al backend.

**Consecuencias:** Consumo adicional de recursos en EC2. Configuración de métricas y dashboards. Curva de aprendizaje inicial.
