# Plan de Pruebas — PawSoft

**Proyecto:** PawSoft — Sistema de gestión veterinaria  
**Universidad:** Universidad del Quindío  
**Programa:** Ingeniería de Sistemas y Computación  
**Materia:** Software III  
**Autoras:** Valentina Porras Salazar · Helen Xiomara Giraldo Libreros  
**Profesor:** Raúl Yulbraynner Rivera Gálvez  

---

## 1. Objetivo

Verificar que el sistema PawSoft cumple con los requisitos funcionales y no funcionales definidos, garantizando la calidad del software antes de su entrega final.

---

## 2. Alcance

Las pruebas cubren los módulos principales del sistema:

- Autenticación y seguridad (login, registro, 2FA, reCAPTCHA, recuperación de contraseña)
- Gestión de mascotas
- Gestión de citas (por rol: cliente, recepcionista, veterinario, administrador)
- Gestión de pagos y precios de servicios
- Perfil de usuario
- Monitoreo y auditoría

---

## 3. Tipos de pruebas realizadas

| Tipo | Herramienta | Estado |
|---|---|---|
| Pruebas unitarias (backend) | JUnit 5 + Mockito | Realizadas |
| Pruebas de integración (backend) | Spring Boot Test | Realizadas |
| Pruebas funcionales (frontend) | Karma + Jasmine | Realizadas |
| Pruebas de aceptación | Manual en producción | Realizadas |
| Pruebas de seguridad | Manual + revisión de código | Realizadas |

---

## 4. Casos de prueba — Backend

### 4.1 RecaptchaService

| ID | Caso de prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| REC-01 | Token válido retorna `true` | `true` | PASS |
| REC-02 | Token inválido retorna `false` | `false` | PASS |
| REC-03 | Token `null` retorna `false` sin llamar a Google | `false`, sin llamada HTTP | PASS |
| REC-04 | Token vacío `""` retorna `false` | `false` | PASS |
| REC-05 | Token en blanco `"   "` retorna `false` | `false` | PASS |
| REC-06 | Respuesta `null` de Google retorna `false` | `false` | PASS |
| REC-07 | Excepción de red retorna `false` | `false` | PASS |

### 4.2 ProfileService

| ID | Caso de prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| PRF-01 | Solicitar verificación envía código 2FA por correo | Código enviado | PASS |
| PRF-02 | Solicitar verificación con usuario inexistente lanza `NotFoundException` | Excepción | PASS |
| PRF-03 | Actualizar email con código válido guarda el nuevo email | Email actualizado | PASS |
| PRF-04 | Actualizar email ya en uso lanza `UnauthorizedException` | Excepción | PASS |
| PRF-05 | Actualizar teléfono con código válido guarda el nuevo teléfono | Teléfono actualizado | PASS |
| PRF-06 | Actualizar contraseña fuerte con código válido la encripta y guarda | Contraseña actualizada | PASS |
| PRF-07 | Actualizar contraseña débil lanza `UnauthorizedException` | Excepción | PASS |
| PRF-08 | Obtener perfil retorna nombre, email y teléfono | Datos correctos | PASS |
| PRF-09 | Obtener perfil con teléfono `null` retorna cadena vacía | `""` en teléfono | PASS |
| PRF-10 | Obtener perfil de usuario inexistente lanza `NotFoundException` | Excepción | PASS |

### 4.3 PaymentService

| ID | Caso de prueba | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| PAY-01 | Crear pago exitosamente guarda y retorna respuesta | Pago creado | PASS |
| PAY-02 | Crear pago para cita que ya tiene pago lanza `IllegalStateException` | Excepción | PASS |
| PAY-03 | Marcar pago como pagado actualiza estado | Estado `PAID` | PASS |
| PAY-04 | Marcar como pagado un pago ya pagado lanza `IllegalStateException` | Excepción | PASS |
| PAY-05 | Revertir pago a pendiente actualiza estado | Estado `PENDING` | PASS |
| PAY-06 | Obtener todos los pagos retorna lista ordenada | Lista correcta | PASS |
| PAY-07 | Obtener pago por ID de cita retorna el pago | Pago encontrado | PASS |
| PAY-08 | Obtener pagos por email de cliente retorna lista | Lista correcta | PASS |
| PAY-09 | Obtener precios activos retorna lista ordenada | Lista correcta | PASS |
| PAY-10 | Crear o actualizar precio de servicio guarda correctamente | Precio guardado | PASS |
| PAY-11 | Eliminar precio existente lo elimina | Eliminado | PASS |
| PAY-12 | Eliminar precio inexistente lanza `NoSuchElementException` | Excepción | PASS |
| PAY-13 | Obtener precio base por tipo de servicio retorna precio | Precio correcto | PASS |
| PAY-14 | Obtener precio de servicio inexistente retorna `BigDecimal.ZERO` | `0` | PASS |

---

## 5. Pruebas funcionales — Frontend

| ID | Módulo | Caso de prueba | Resultado |
|---|---|---|---|
| FE-01 | Login | Componente se crea correctamente | PASS |
| FE-02 | Registro | Componente se crea correctamente | PASS |
| FE-03 | AuthService | Servicio se inyecta correctamente | PASS |
| FE-04 | Login | Campos vacíos no permiten envío del formulario | PASS |
| FE-05 | Registro | Validación de contraseña muestra requisitos mientras se escribe | PASS |
| FE-06 | Registro | Contraseña con carácter especial no estándar (ej: `.`, `¿`) es aceptada | PASS |
| FE-07 | Citas | Slots de hora pasada no aparecen disponibles cuando la fecha es hoy | PASS |
| FE-08 | Perfil | Cambio de contraseña muestra requisitos mientras se escribe | PASS |
| FE-09 | Recuperar contraseña | Muestra requisitos de contraseña mientras se escribe | PASS |
| FE-10 | Inactividad | Sesión se cierra automáticamente tras 10 minutos sin actividad | PASS |

---

## 6. Pruebas de integración

| ID | Caso de prueba | Descripción | Resultado |
|---|---|---|---|
| INT-01 | Contexto Spring Boot carga correctamente | `BackendPawsoftApplicationTests` verifica que la aplicación arranca sin errores | PASS |
| INT-02 | Flujo completo de login con 2FA | Login → validación reCAPTCHA → envío OTP → verificación → JWT emitido | PASS |
| INT-03 | Flujo de registro y verificación de correo | Registro → email de verificación → activación de cuenta | PASS |
| INT-04 | Flujo de recuperación de contraseña | Solicitud → email con token → reset con nueva contraseña | PASS |
| INT-05 | Creación de cita y pago | Agendar cita → confirmar → registrar pago → historial actualizado | PASS |
| INT-06 | Eliminación de mascota en cascada | Eliminar mascota elimina sus citas asociadas sin errores | PASS |

---

## 7. Pruebas de aceptación

Realizadas manualmente en el entorno de producción (`https://www.pawsoft.online`).

| ID | Historia de usuario | Criterio de aceptación | Resultado |
|---|---|---|---|
| AC-01 | Como cliente, quiero registrarme | Registro exitoso con verificación de correo | PASS |
| AC-02 | Como cliente, quiero iniciar sesión con 2FA | Login con OTP por correo funciona correctamente | PASS |
| AC-03 | Como cliente, quiero agendar una cita | Selección de fecha, hora, mascota y veterinario funciona | PASS |
| AC-04 | Como cliente, no puedo agendar en horarios pasados | Slots pasados no aparecen disponibles | PASS |
| AC-05 | Como cliente, quiero ver el historial de mis mascotas | Historial de citas visible en el dashboard | PASS |
| AC-06 | Como recepcionista, quiero registrar un pago | Registro de pago con concepto y monto funciona | PASS |
| AC-07 | Como administrador, quiero gestionar precios | CRUD de precios de servicios funciona | PASS |
| AC-08 | Como veterinario, quiero ver mis citas del día | Dashboard de veterinario muestra citas correctamente | PASS |
| AC-09 | Como usuario, quiero recuperar mi contraseña | Flujo de reset por correo funciona en producción | PASS |
| AC-10 | Como usuario, quiero cambiar mis datos de perfil | Cambio de email, teléfono y contraseña con 2FA funciona | PASS |

---

## 8. Gestión de defectos

Defectos detectados durante el desarrollo y corregidos antes de la entrega final:

| ID | Descripción | Severidad | Estado |
|---|---|---|---|
| BUG-01 | Era posible agendar citas en horarios pasados del día actual | Alta | Corregido |
| BUG-02 | Regex de contraseña rechazaba caracteres especiales válidos (ej: `.`, `¿`, `#`) | Media | Corregido |
| BUG-03 | Al eliminar una mascota, sus citas no se eliminaban en cascada | Alta | Corregido |
| BUG-04 | El frontend apuntaba a `localhost` en producción en lugar de `api.pawsoft.online` | Crítica | Corregido |
| BUG-05 | Logs de consola exponían información sensible (roles, tokens) | Alta | Corregido |
| BUG-06 | El dashboard de Grafana tenía división por cero en paneles de métricas | Media | Corregido |
| BUG-07 | La sesión no expiraba por inactividad, dejando cuentas abiertas | Media | Corregido |
| BUG-08 | Los hints de contraseña no aparecían en cambiar contraseña ni en recuperación | Baja | Corregido |

---

## 9. Resultados de ejecución de pruebas unitarias

Las pruebas unitarias del backend se ejecutan con Maven:

```bash
mvn test
```

Resultado de la última ejecución:

```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Cobertura por servicio:

| Servicio | Tests | Estado |
|---|---|---|
| RecaptchaService | 7 | PASS |
| ProfileService | 10 | PASS |
| PaymentService | 14 | PASS |
| BackendPawsoftApplication | 1 (contexto) | PASS |

---

## 10. Evidencia de validación final

El sistema fue validado en el entorno de producción con las siguientes verificaciones:

- Frontend accesible en `https://www.pawsoft.online` con HTTPS y certificado válido
- Backend respondiendo en `https://api.pawsoft.online` con todos los endpoints operativos
- Flujo completo de registro, login con 2FA, gestión de citas y pagos verificado manualmente
- Panel de Grafana mostrando métricas en tiempo real del backend
- Auditoría registrando acciones críticas en tabla `audit_log`
- Todos los roles (CLIENTE, VETERINARIO, RECEPCIONISTA, ADMIN) verificados con usuarios de prueba

**Conclusión:** El sistema cumple con los requisitos funcionales y no funcionales definidos para el proyecto académico. Los 8 defectos detectados durante el desarrollo fueron corregidos antes de la entrega final.
