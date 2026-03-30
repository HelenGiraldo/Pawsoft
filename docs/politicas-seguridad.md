# Políticas de Seguridad — PawSoft

**Proyecto:** PawSoft — Sistema de gestión veterinaria  
**Universidad:** Universidad del Quindío  
**Programa:** Ingeniería de Sistemas y Computación  
**Materia:** Software III  
**Autoras:** Valentina Porras Salazar · Helen Xiomara Giraldo Libreros  
**Profesor:** Raúl Yulbraynner Rivera Gálvez  

---

## 1. Autenticación de usuarios

El sistema implementa un mecanismo de autenticación en dos fases:

- Primera fase: correo electrónico y contraseña validados contra la base de datos con BCrypt.
- Segunda fase: código OTP de 6 dígitos enviado al correo registrado, con expiración de 3 minutos.

Los tokens JWT emitidos tras la autenticación exitosa tienen una duración limitada y se transmiten únicamente por HTTPS. El frontend los almacena en memoria y los adjunta automáticamente a cada petición mediante un interceptor HTTP.

Los usuarios creados por el administrador reciben una contraseña temporal y deben cambiarla obligatoriamente en el primer acceso.

---

## 2. Control de acceso por roles (RBAC)

El sistema define cuatro roles con permisos diferenciados:

| Rol | Acceso |
|---|---|
| `ROLE_CLIENTE` | Gestión de sus mascotas, agendamiento y consulta de sus citas |
| `ROLE_VETERINARIO` | Consulta de citas asignadas, actualización de notas clínicas |
| `ROLE_RECEPCIONISTA` | Gestión de citas de todos los clientes, registro de pagos |
| `ROLE_ADMIN` | Acceso total: usuarios, precios, reportes, configuración |

El control se aplica en dos niveles:
- Backend: anotaciones `@PreAuthorize` en cada endpoint con Spring Security.
- Frontend: guards de Angular que verifican el rol antes de permitir la navegación.

---

## 3. Protección de datos sensibles

- Las contraseñas se almacenan cifradas con BCrypt. Nunca se almacenan ni transmiten en texto plano.
- Los tokens de verificación de correo (1 hora), reset de contraseña (30 min) y códigos 2FA (3 min) tienen expiración definida y se marcan como usados tras su consumo.
- El archivo `application.properties` con credenciales de base de datos, claves de API y secretos está excluido del repositorio mediante `.gitignore`.
- Las variables sensibles (claves JWT, credenciales SMTP, claves reCAPTCHA, credenciales Cloudinary) se configuran como variables de entorno en el servidor de producción.
- No se registran datos sensibles (contraseñas, tokens, roles) en logs de consola.

---

## 4. Validación de entradas

Toda entrada del usuario se valida en dos capas:

**Frontend:**
- Formularios Angular con validadores reactivos.
- Regex para contraseña fuerte: mínimo 8 caracteres, al menos una mayúscula, un número y un carácter especial (cualquier carácter no alfanumérico).
- Teléfono colombiano: exactamente 10 dígitos numéricos.
- Nombre: solo letras y espacios, máximo 100 caracteres.

**Backend:**
- DTOs con anotaciones `@Valid`, `@NotBlank`, `@Email`, `@Size`.
- Validación de contraseña fuerte en todos los flujos que la involucran: registro, reset, cambio de contraseña y perfil.
- Validación de fecha y hora de citas: no se permiten horarios pasados.
- Manejo global de excepciones con `@ControllerAdvice` que retorna respuestas estructuradas sin exponer detalles internos del sistema.

---

## 5. Prevención de vulnerabilidades comunes

| Vulnerabilidad | Medida implementada |
|---|---|
| Fuerza bruta en login | Bloqueo de cuenta tras intentos fallidos consecutivos con tiempo de espera escalonado |
| Bots y automatización | Google reCAPTCHA v2 validado en backend en login y registro |
| Acceso no autorizado a endpoints | Spring Security con JWT obligatorio en todos los endpoints protegidos |
| CORS no controlado | Configuración explícita de orígenes permitidos en `SecurityConfig` |
| Tokens robados o reutilizados | Expiración temporal y marcado de uso único en tokens sensibles |
| Inyección SQL | Acceso a datos exclusivamente mediante JPA/Hibernate con consultas parametrizadas |
| Exposición de información en errores | Respuestas de error genéricas sin stack traces ni detalles internos |

---

## 6. Contraseñas seguras y cifrado

**Política de contraseñas:**
- Mínimo 8 caracteres
- Al menos una letra mayúscula
- Al menos un número
- Al menos un carácter especial (cualquier símbolo no alfanumérico)

Esta política se aplica en: registro, recuperación de contraseña, cambio de contraseña en primer acceso y cambio de contraseña desde el perfil.

**Cifrado:**
- Contraseñas: BCrypt con salt aleatorio por usuario.
- Comunicación: TLS/HTTPS en todos los endpoints de producción.

---

## 7. Protección de la conexión

- Todo el tráfico entre el cliente y el servidor se realiza sobre HTTPS.
- El frontend se sirve desde CloudFront con certificado SSL gestionado por AWS.
- El backend expone sus endpoints bajo el dominio `api.pawsoft.online` con certificado SSL en EC2.
- Los tokens JWT se transmiten en el header `Authorization: Bearer <token>` y nunca en la URL.

---

## 8. Copias de seguridad y recuperación

**Estado actual:** backup automático diario configurado en el servidor EC2 mediante `mysqldump` + `cron`.

**Script de backup** (`/home/ec2-user/backup-db.sh`):
```bash
#!/bin/bash
FECHA=$(date +%Y%m%d_%H%M%S)
ARCHIVO="/home/ec2-user/backups/pawsoft_$FECHA.sql"
mysqldump -h <host-rds> -u admin -p<password> pawsoft > "$ARCHIVO"
find /home/ec2-user/backups -name "*.sql" -mtime +7 -delete
echo "Backup completado: $ARCHIVO"
```

**Cron configurado** (todos los días a las 2:00 AM UTC):
```
0 2 * * * /home/ec2-user/backup-db.sh >> /home/ec2-user/backups/backup.log 2>&1
```

**Política:**
- Frecuencia: diaria automática.
- Retención: 7 días (los backups más antiguos se eliminan automáticamente).
- Almacenamiento: directorio `/home/ec2-user/backups/` en EC2.
- Log de ejecución: `/home/ec2-user/backups/backup.log`.

---

## 9. Auditoría de eventos

El sistema registra en la tabla `audit_log` las siguientes acciones críticas:

| Evento | Descripción |
|---|---|
| `APPOINTMENT_CREATE` | Creación de una cita |
| `APPOINTMENT_CANCEL` | Cancelación de una cita |
| `PAYMENT_CREATE` | Registro de un pago |
| `PAYMENT_CONFIRMED` | Confirmación de pago |
| `PAYMENT_REVERTED` | Reversión de pago a pendiente |
| `PRICE_UPSERT` | Creación o actualización de precio de servicio |
| `PRICE_DELETE` | Eliminación de precio de servicio |
| `PROFILE_UPDATE_EMAIL` | Cambio de correo electrónico |
| `PROFILE_UPDATE_PHONE` | Cambio de teléfono |
| `PROFILE_UPDATE_PASSWORD` | Cambio de contraseña desde perfil |

Cada registro incluye: acción, descripción, entidad afectada, ID de la entidad y timestamp automático.

---

## 10. Resumen de cumplimiento

| Criterio | Estado |
|---|---|
| Autenticación de usuarios | Cumple — JWT + 2FA por OTP |
| Control de acceso por roles | Cumple — RBAC con 4 roles en backend y frontend |
| Protección de datos sensibles | Cumple — BCrypt, tokens con expiración, secrets fuera del repo |
| Validación de entradas | Cumple — frontend y backend con reglas explícitas |
| Prevención de vulnerabilidades | Cumple — fuerza bruta, bots, CORS, inyección SQL |
| Contraseñas seguras y cifrado | Cumple — política de contraseñas + BCrypt + HTTPS |
| Protección de la conexión | Cumple — HTTPS en frontend y backend con certificados SSL |
| Copias de seguridad | Cumple parcialmente — backup manual disponible, sin automatización |
| Auditoría de eventos | Cumple — tabla `audit_log` con acciones críticas |
| Políticas documentadas | Cumple — este documento |
