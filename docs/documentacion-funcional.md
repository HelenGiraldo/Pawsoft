# Documentación Funcional — PawSoft

**Proyecto:** PawSoft — Sistema de gestión veterinaria  
**Universidad:** Universidad del Quindío  
**Programa:** Ingeniería de Sistemas y Computación  
**Materia:** Software III  
**Autoras:** Valentina Porras Salazar · Helen Xiomara Giraldo Libreros  
**Profesor:** Raúl Yulbraynner Rivera Gálvez  

---

## 1. Descripción General del Sistema

PawSoft es un sistema web de gestión veterinaria diseñado para clínicas pequeñas y medianas. Centraliza la información de clientes, mascotas, citas médicas, historiales clínicos y pagos en una única plataforma accesible desde navegador web y dispositivos móviles.

El sistema reemplaza procesos manuales tradicionales que generan desorden, pérdida de información y errores en el seguimiento de pacientes.

---

## 2. Módulos Funcionales

### 2.1 Autenticación y Seguridad

**Registro de clientes:**
- El usuario ingresa nombre, apellido, teléfono, correo y contraseña
- El sistema valida que la contraseña cumpla con los requisitos de seguridad
- El sistema valida reCAPTCHA para prevenir bots
- El sistema envía un correo de verificación con enlace temporal
- El usuario debe verificar su correo antes de poder iniciar sesión

**Inicio de sesión:**
- El usuario ingresa correo y contraseña
- El sistema valida las credenciales contra la base de datos
- El sistema valida reCAPTCHA
- El sistema envía un código OTP de 6 dígitos al correo del usuario
- El usuario ingresa el código OTP (válido por 3 minutos)
- El sistema genera un token JWT (válido por 1 hora) y un refresh token (válido por 7 días)
- El sistema redirige al usuario según su rol

**Renovación automática de sesión:**
- El sistema renueva automáticamente el JWT 5 minutos antes de su expiración
- Si el JWT expira, el sistema usa el refresh token para obtener uno nuevo
- El usuario puede trabajar sin interrupciones mientras esté activo

**Cierre de sesión por inactividad:**
- El sistema detecta inactividad del usuario (sin clicks, sin llamadas API)
- Tiempo de inactividad por rol: Veterinario/Admin 60 min, Recepcionista 30 min, Cliente 15 min
- El sistema cierra la sesión automáticamente y redirige al login

**Recuperación de contraseña:**
- El usuario solicita recuperación ingresando su correo
- El sistema envía un enlace temporal (válido por 30 minutos)
- El usuario ingresa nueva contraseña cumpliendo requisitos de seguridad
- El sistema actualiza la contraseña

**Primer acceso de staff:**
- El administrador crea una cuenta de staff con contraseña temporal
- El usuario recibe la contraseña por correo
- En el primer inicio de sesión, el sistema obliga al usuario a cambiar la contraseña

---

### 2.2 Gestión de Usuarios

**Roles del sistema:**
- ROLE_CLIENTE: Propietarios de mascotas
- ROLE_VETERINARIO: Médicos veterinarios
- ROLE_RECEPCIONISTA: Personal administrativo
- ROLE_ADMIN: Administradores del sistema

**Creación de usuarios staff (Admin):**
- El administrador ingresa nombre, apellido, correo, teléfono y rol
- El sistema genera una contraseña temporal aleatoria
- El sistema envía las credenciales por correo
- El nuevo usuario debe cambiar la contraseña en el primer acceso

**Gestión de perfil:**
- El usuario puede actualizar su correo, teléfono y contraseña
- Para cambios de correo o contraseña, el sistema solicita código OTP de verificación
- El sistema valida el código antes de aplicar los cambios

**Activación/desactivación de cuentas:**
- El administrador puede desactivar cuentas de usuarios
- Los usuarios desactivados no pueden iniciar sesión
- El administrador puede reactivar cuentas desactivadas

---

### 2.3 Gestión de Mascotas

**Registro de mascota:**
- El cliente o recepcionista ingresa: nombre, especie, raza, sexo, fecha de nacimiento, color, peso
- El usuario puede subir una foto de la mascota (máximo 2MB)
- El sistema almacena la foto en Cloudinary
- El sistema asocia la mascota con su propietario

**Consulta de mascotas:**
- El cliente ve únicamente sus propias mascotas
- El recepcionista y veterinario pueden buscar mascotas por propietario
- El administrador puede ver todas las mascotas del sistema

**Edición y eliminación:**
- El propietario puede editar información de sus mascotas
- El recepcionista puede editar mascotas de cualquier cliente
- La eliminación de una mascota requiere que no tenga citas pendientes

---

### 2.4 Gestión de Citas

**Agendamiento de cita (Cliente):**
- El cliente selecciona una de sus mascotas
- El cliente selecciona fecha, hora y veterinario
- El sistema valida disponibilidad del veterinario en ese horario
- El sistema valida que no haya otra cita para esa mascota en el mismo día
- El sistema crea la cita con estado UPCOMING
- El sistema registra el pago asociado con estado PENDING

**Gestión de citas (Recepcionista):**
- La recepcionista puede crear citas para cualquier cliente
- La recepcionista puede modificar fecha, hora o veterinario de citas existentes
- La recepcionista puede cancelar citas
- La recepcionista puede confirmar citas (cambiar estado de UPCOMING a CONFIRMED)

**Visualización de citas (Veterinario):**
- El veterinario ve sus citas del día en la pestaña "Mis Citas de Hoy"
- El veterinario ve sus citas futuras en la pestaña "Próximas Citas"
- Las citas se muestran ordenadas por hora
- El veterinario puede ver detalles de la mascota y propietario

**Inicio de atención médica:**
- El veterinario hace click en "Iniciar atención" en una cita CONFIRMED
- El sistema cambia el estado de la cita a IN_PROGRESS
- El sistema valida que el veterinario no tenga otra cita IN_PROGRESS
- El sistema redirige al formulario de consulta médica

**Cancelación de atención iniciada:**
- Si el veterinario inició la atención por error, puede cancelarla
- El sistema revierte el estado de IN_PROGRESS a CONFIRMED
- El sistema registra la cancelación en auditoría
- El veterinario puede iniciar otra cita

**Cierre de atención:**
- El veterinario completa el formulario de consulta médica
- El sistema valida que todos los campos obligatorios estén llenos
- El sistema guarda el registro médico
- El sistema cambia el estado de la cita a COMPLETED
- El sistema genera el resumen para el cliente

---

### 2.5 Historial Clínico

**Registro de consulta médica:**
- El veterinario ingresa datos del examen físico: peso, temperatura, frecuencia cardíaca
- El veterinario ingresa observaciones generales, diagnóstico principal y notas clínicas
- El veterinario puede agregar medicamentos recetados con dosis e indicaciones
- El veterinario puede registrar vacunas aplicadas
- El veterinario puede programar próximo control con fecha y motivo
- El veterinario puede adjuntar fotos (radiografías, análisis) hasta 2MB cada una

**Resumen para cliente:**
- El veterinario ingresa diagnóstico e indicaciones en lenguaje comprensible para el cliente
- El sistema genera un resumen que el cliente puede consultar

**Consulta de historial:**
- El veterinario puede consultar el historial clínico completo de cualquier mascota
- El historial muestra todas las consultas ordenadas por fecha
- El veterinario puede filtrar por mascota, propietario o rango de fechas
- El veterinario puede expandir cada registro para ver detalles completos

---

### 2.6 Sistema de Pagos

**Registro de pago (Recepcionista):**
- La recepcionista selecciona el cliente y el servicio
- El sistema muestra el precio del servicio
- La recepcionista ingresa el monto pagado
- El sistema crea el registro de pago con estado PENDING

**Confirmación de cobro:**
- La recepcionista confirma que recibió el pago en efectivo
- El sistema cambia el estado del pago a PAID
- El sistema registra la fecha y hora de confirmación

**Gestión de precios (Admin):**
- El administrador puede crear nuevos servicios con sus precios
- El administrador puede actualizar precios de servicios existentes
- El administrador puede eliminar servicios que no tengan pagos asociados

**Consulta de pagos:**
- El cliente puede ver su historial de pagos con estado y fecha
- El administrador puede ver todos los pagos del sistema ordenados por fecha

**Estadísticas financieras (Admin):**
- El administrador puede consultar ingresos totales por período
- El administrador puede ver ingresos desglosados por tipo de servicio
- El administrador puede ver cantidad de pagos pendientes vs pagados

**Reversión de pagos (Admin):**
- El administrador puede revertir un pago de PAID a PENDING
- Esta función se usa para corregir errores de registro
- El sistema registra la reversión en auditoría

---

### 2.7 Auditoría

**Registro automático:**

El sistema guarda un registro de todas las acciones importantes que ocurren en la plataforma. Cada vez que un usuario realiza una acción crítica, el sistema guarda:
- Quién lo hizo (nombre y rol del usuario)
- Qué hizo (tipo de acción)
- Cuándo lo hizo (fecha y hora exacta)
- Desde dónde lo hizo (dirección IP)

Estos registros no se pueden modificar ni eliminar, garantizando un historial confiable.

**Acciones que se registran:**

Citas:
- Creación de citas
- Modificación de citas
- Cancelación de citas
- Inicio de atención médica
- Cancelación de atención iniciada
- Cierre de atención (cita completada)

Pagos:
- Registro de pagos
- Confirmación de cobro
- Reversión de pagos

Precios:
- Creación de servicios
- Actualización de precios
- Eliminación de servicios

Usuarios:
- Registro de nuevos clientes
- Creación de usuarios staff
- Cambios en perfil (correo, teléfono, contraseña)
- Activación/desactivación de cuentas
- Intentos de login fallidos

Mascotas:
- Registro de mascotas
- Actualización de datos
- Eliminación de mascotas

Registros médicos:
- Guardado de consultas médicas

**Consulta de registros:**

El administrador puede consultar todos los registros de auditoría directamente desde la base de datos usando consultas SQL. Los registros se pueden filtrar por usuario, tipo de acción, entidad afectada o rango de fechas.

Nota: No existe interfaz gráfica para consultar auditoría. El acceso es mediante consultas SQL directas a la tabla `audit_log`.

---

## 3. Flujos de Trabajo Principales

### 3.1 Flujo: Cliente agenda una cita

1. Cliente inicia sesión con correo, contraseña y código OTP
2. Cliente navega a "Mis Citas"
3. Cliente hace click en "Agendar Cita"
4. Cliente selecciona una de sus mascotas
5. Cliente selecciona fecha y consulta horarios disponibles
6. Cliente selecciona hora y veterinario
7. Sistema valida disponibilidad
8. Sistema crea la cita con estado UPCOMING
9. Sistema registra el pago con estado PENDING
10. Sistema muestra confirmación al cliente

### 3.2 Flujo: Veterinario atiende una cita

1. Veterinario inicia sesión
2. Veterinario navega a "Mis Citas"
3. Veterinario ve la lista de citas del día
4. Veterinario hace click en "Iniciar atención" en la cita correspondiente
5. Sistema cambia estado a IN_PROGRESS
6. Sistema redirige al formulario de consulta
7. Veterinario completa examen físico (peso, temperatura, frecuencia cardíaca)
8. Veterinario ingresa observaciones, diagnóstico y notas clínicas
9. Veterinario agrega medicamentos recetados (opcional)
10. Veterinario registra vacunas aplicadas (opcional)
11. Veterinario programa próximo control (opcional)
12. Veterinario adjunta fotos (opcional)
13. Veterinario hace click en "Continuar al resumen"
14. Sistema valida campos obligatorios
15. Veterinario completa resumen para cliente
16. Veterinario hace click en "Cerrar atención"
17. Sistema guarda el registro médico
18. Sistema cambia estado de cita a COMPLETED
19. Sistema redirige a lista de citas

### 3.3 Flujo: Recepcionista confirma pago

1. Recepcionista inicia sesión
2. Recepcionista navega a "Pagos"
3. Recepcionista busca el pago del cliente
4. Cliente paga en efectivo
5. Recepcionista hace click en "Confirmar cobro"
6. Sistema cambia estado del pago a PAID
7. Sistema registra fecha y hora de confirmación
8. Sistema muestra confirmación

### 3.4 Flujo: Administrador gestiona precios

1. Administrador inicia sesión
2. Administrador navega a "Precios"
3. Administrador hace click en "Nuevo Servicio"
4. Administrador ingresa nombre del servicio y precio
5. Sistema valida que el precio sea mayor a cero
6. Sistema guarda el nuevo servicio
7. Sistema registra la acción en auditoría

---

## 4. Reglas de Negocio

### 4.1 Citas

- Una mascota no puede tener más de una cita en el mismo día
- Un veterinario no puede tener dos citas en el mismo horario
- Las citas solo se pueden agendar en horarios futuros
- Un veterinario solo puede tener una cita IN_PROGRESS a la vez
- Las citas COMPLETED no se pueden modificar ni cancelar
- Solo se pueden iniciar citas con estado CONFIRMED

### 4.2 Pagos

- Todo agendamiento de cita genera un pago con estado PENDING
- Los pagos solo se pueden confirmar si están en estado PENDING
- Los pagos PAID solo pueden ser revertidos por el administrador
- No se pueden eliminar servicios que tengan pagos asociados
- El precio de un servicio debe ser mayor a cero

### 4.3 Usuarios

- Los correos electrónicos deben ser únicos en el sistema
- Los usuarios deben verificar su correo antes de poder iniciar sesión
- Las contraseñas deben cumplir: mínimo 8 caracteres, una mayúscula, un número, un carácter especial
- Los usuarios staff deben cambiar su contraseña temporal en el primer acceso
- Los usuarios desactivados no pueden iniciar sesión

### 4.4 Mascotas

- Una mascota debe estar asociada a un propietario
- No se pueden eliminar mascotas que tengan citas pendientes o historial clínico
- Las fotos de mascotas no pueden exceder 2MB
- La fecha de nacimiento no puede ser futura

### 4.5 Historial Clínico

- Solo se puede crear un registro médico para citas IN_PROGRESS
- Los campos obligatorios son: peso, temperatura, frecuencia cardíaca, observaciones generales, diagnóstico principal, notas clínicas, diagnóstico cliente, indicaciones cliente
- Las fotos adjuntas no pueden exceder 2MB cada una
- Los registros médicos son inmutables (no se pueden editar después de creados)

---

## 5. Permisos por Rol

### Cliente
- Ver y editar su propio perfil (correo, teléfono, contraseña)
- Registrar y gestionar sus mascotas
- Agendar y cancelar sus propias citas
- Ver su historial de pagos
- Ver precios de servicios

### Veterinario
- Ver su propio perfil
- Ver sus citas asignadas
- Iniciar y cancelar atención médica
- Registrar consultas médicas
- Consultar historial clínico de cualquier mascota
- Adjuntar fotos a registros médicos

### Recepcionista
- Ver su propio perfil
- Gestionar clientes (crear, editar, activar/desactivar)
- Gestionar mascotas de clientes
- Gestionar citas de todos los clientes
- Registrar y confirmar pagos
- Ver precios de servicios

### Administrador
- Acceso completo a todas las funcionalidades
- Gestionar usuarios staff
- Gestionar precios de servicios
- Ver estadísticas financieras
- Revertir pagos
- Consultar log de auditoría

---

## 6. Validaciones del Sistema

### 6.1 Validaciones de Formularios

**Registro de usuario:**
- Nombre: solo letras y espacios, máximo 100 caracteres
- Apellido: solo letras y espacios, máximo 100 caracteres
- Teléfono: exactamente 10 dígitos numéricos
- Correo: formato válido de email
- Contraseña: mínimo 8 caracteres, una mayúscula, un número, un carácter especial

**Registro de mascota:**
- Nombre: obligatorio, máximo 50 caracteres
- Especie: obligatorio
- Fecha de nacimiento: no puede ser futura
- Peso: número positivo, máximo 999 kg
- Foto: formato JPG/PNG, máximo 2MB

**Agendamiento de cita:**
- Mascota: obligatorio
- Fecha: no puede ser pasada
- Hora: debe estar en horarios disponibles
- Veterinario: obligatorio
- Motivo: obligatorio, máximo 500 caracteres

**Registro médico:**
- Peso: número positivo entre 0.1 y 999 kg
- Temperatura: número entre 30 y 45 grados
- Frecuencia cardíaca: número entero entre 1 y 999 bpm
- Observaciones generales: obligatorio
- Diagnóstico principal: obligatorio
- Notas clínicas: obligatorio
- Diagnóstico cliente: obligatorio
- Indicaciones cliente: obligatorio
- Fotos adjuntas: formato JPG/PNG, máximo 2MB cada una

**Registro de pago:**
- Cliente: obligatorio
- Servicio: obligatorio
- Monto: debe coincidir con el precio del servicio

### 6.2 Validaciones de Negocio

**Disponibilidad de horarios:**
- No se muestran horarios pasados si la fecha seleccionada es hoy
- No se muestran horarios donde el veterinario ya tiene cita
- Los horarios disponibles se calculan en intervalos de 30 minutos

**Inicio de atención:**
- Solo se puede iniciar atención en citas CONFIRMED
- Un veterinario solo puede tener una cita IN_PROGRESS a la vez
- Si hay una cita IN_PROGRESS, las demás citas muestran el botón deshabilitado

**Cierre de atención:**
- Solo se puede cerrar atención si la cita está IN_PROGRESS
- Todos los campos obligatorios deben estar completos
- El sistema valida que el formulario interno esté completo antes de permitir acceso al resumen cliente

---

## 7. Notificaciones del Sistema

**Correos enviados automáticamente:**
- Verificación de correo al registrarse (con enlace de activación)
- Código OTP al iniciar sesión (válido por 3 minutos)
- Código OTP al solicitar cambio de correo o contraseña desde perfil (válido por 3 minutos)
- Contraseña temporal al crear usuario staff (por admin o recepcionista)
- Enlace de recuperación de contraseña (válido por 30 minutos)

**Mensajes en pantalla:**
- Confirmación de acciones exitosas (cita creada, pago confirmado, etc.)
- Errores de validación en formularios
- Mensajes de sesión expirada por inactividad
- Mensajes de sesión cerrada manualmente

---

## 8. Integraciones Externas

### 8.1 Cloudinary
- Almacenamiento de fotos de mascotas
- Almacenamiento de fotos adjuntas en registros médicos
- Límite de tamaño: 2MB por imagen
- Formatos soportados: JPG, PNG

### 8.2 Google reCAPTCHA
- Validación en registro de clientes
- Validación en inicio de sesión
- Prevención de bots y automatización

### 8.3 SMTP (Correo electrónico)
- Envío de códigos OTP (válidos por 3 minutos)
- Envío de enlaces de verificación de correo
- Envío de credenciales temporales a usuarios staff
- Envío de enlaces de recuperación de contraseña (válidos por 30 minutos)

---

## 9. Consideraciones de Usabilidad

### 9.1 Accesibilidad
- El sistema incluye opciones de tamaño de fuente (normal, grande, extra grande)
- El sistema incluye modo de alto contraste
- El sistema soporta navegación por teclado
- Los formularios muestran mensajes de error claros

### 9.2 Experiencia de Usuario
- El sistema muestra indicadores de carga durante operaciones
- El sistema muestra confirmaciones visuales de acciones exitosas
- Los formularios validan en tiempo real
- El sistema previene acciones accidentales con modales de confirmación
- El formulario de consulta médica guarda borradores automáticamente

### 9.3 Responsive Design
- El sistema funciona en dispositivos móviles, tablets y computadores
- La interfaz se adapta al tamaño de pantalla
- Los formularios son fáciles de completar en móvil

---

## 10. Limitaciones Conocidas

- El sistema no incluye facturación electrónica
- El sistema no soporta pagos en línea (solo efectivo)
- El sistema no incluye telemedicina
- El sistema no incluye gestión avanzada de inventario
- El sistema no incluye gestión de proveedores
- Los registros médicos no se pueden editar después de creados
- Las fotos tienen límite de 2MB por archivo

