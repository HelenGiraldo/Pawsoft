# Sistema de Precios para Medicamentos y Vacunas

Sistema completo de facturación detallada para medicamentos y vacunas aplicados durante procedimientos veterinarios, con capacidad de ajuste de montos y auditoría completa.

---

## Características Principales

### 1. Catálogos de Precios

**Medicamentos:**
- Nombre único del medicamento
- Descripción opcional
- Precio unitario en COP
- Unidad de medida (ml, tableta, ampolla, etc.)
- Estado activo/inactivo

**Vacunas:**
- Nombre único de la vacuna
- Descripción opcional
- Precio por dosis en COP
- Estado activo/inactivo

### 2. Facturación Detallada

Los pagos ahora incluyen ítems individuales:
- **SERVICE**: Servicios veterinarios (consulta, cirugía, etc.)
- **MEDICATION**: Medicamentos aplicados durante el procedimiento
- **VACCINE**: Vacunas aplicadas

Cada ítem incluye:
- Cantidad
- Unidad de medida
- Precio unitario
- Subtotal (cantidad × precio unitario)

### 3. Sistema de Ajustes con Auditoría

Las recepcionistas pueden ajustar el monto total de un pago con:
- **Motivo obligatorio** (mínimo 10 caracteres)
- **Auditoría automática**: quién, cuándo, monto original, monto ajustado, diferencia
- **Historial completo** visible para administrador

---

## Flujo de Trabajo

### Para Administradores

#### Gestión de Catálogos de Medicamentos

**Listar todos los medicamentos:**
```http
GET /api/admin/payments/medications
```

**Crear o actualizar medicamento:**
```http
POST /api/admin/payments/medications
Content-Type: application/json

{
  "name": "Amoxicilina 500mg",
  "description": "Antibiótico de amplio espectro",
  "price": 15000.00,
  "unit": "tableta",
  "active": true
}
```

**Eliminar medicamento:**
```http
DELETE /api/admin/payments/medications/{id}
```

#### Gestión de Catálogos de Vacunas

**Listar todas las vacunas:**
```http
GET /api/admin/payments/vaccines
```

**Crear o actualizar vacuna:**
```http
POST /api/admin/payments/vaccines
Content-Type: application/json

{
  "name": "Vacuna Séxtuple Canina",
  "description": "Protección contra 6 enfermedades",
  "price": 45000.00,
  "active": true
}
```

**Eliminar vacuna:**
```http
DELETE /api/admin/payments/vaccines/{id}
```

#### Ver Ajustes de Pagos

Los ajustes se incluyen automáticamente en la respuesta de cada pago:

```json
{
  "id": 123,
  "amount": 85000.00,
  "adjustments": [
    {
      "id": 1,
      "originalAmount": 100000.00,
      "adjustedAmount": 85000.00,
      "difference": -15000.00,
      "reason": "Descuento por cliente frecuente según política de fidelización",
      "adjustedBy": "recepcionista@pawsoft.com",
      "adjustedByName": "María García",
      "adjustedAt": "2026-04-11T14:30:00"
    }
  ]
}
```

---

### Para Recepcionistas

#### Consultar Catálogos

**Medicamentos activos:**
```http
GET /api/recepcionista/payments/medications
```

**Vacunas activas:**
```http
GET /api/recepcionista/payments/vaccines
```

#### Crear Pago con Ítems Detallados

```http
POST /api/recepcionista/payments
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "appointmentId": 456,
  "clientName": "Juan Pérez",
  "clientEmail": "juan@example.com",
  "petName": "Max",
  "vetName": "Dra. Ana López",
  "appointmentDate": "2026-04-11",
  "appointmentTime": "10:00",
  "concept": "Consulta general + medicamentos",
  "baseAmount": 50000.00,
  "amount": 95000.00,
  "notes": "Pago completo",
  "items": [
    {
      "itemType": "SERVICE",
      "itemName": "Consulta general",
      "description": "Consulta veterinaria de rutina",
      "quantity": 1,
      "unit": "servicio",
      "unitPrice": 50000.00
    },
    {
      "itemType": "MEDICATION",
      "itemName": "Amoxicilina 500mg",
      "description": "Antibiótico aplicado durante consulta",
      "quantity": 2,
      "unit": "tableta",
      "unitPrice": 15000.00
    },
    {
      "itemType": "VACCINE",
      "itemName": "Vacuna Antirrábica",
      "description": "Vacuna contra rabia",
      "quantity": 1,
      "unit": "dosis",
      "unitPrice": 35000.00
    }
  ]
}
```

**Cálculo del monto:**
- Consulta: 1 × 50,000 = 50,000
- Amoxicilina: 2 × 15,000 = 30,000
- Vacuna: 1 × 35,000 = 35,000
- **Total: 115,000 COP**

#### Ajustar Monto de Pago

```http
PUT /api/recepcionista/payments/{id}/adjust
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "adjustedAmount": 100000.00,
  "reason": "Descuento del 13% por cliente frecuente según política de fidelización aprobada por gerencia"
}
```

**Validaciones:**
- El motivo debe tener entre 10 y 500 caracteres
- El monto ajustado no puede ser negativo
- Se registra automáticamente quién hizo el ajuste (del JWT)

---

## Reglas de Negocio

### Medicamentos y Vacunas

1. **Solo se cobran los aplicados durante el procedimiento**
   - Los medicamentos recetados para casa NO se cobran aquí
   - Solo los medicamentos del campo `medicamentos` (no `medicamentosRecetados`)
   - Solo las vacunas del campo `vacunasAplicadas`

2. **Catálogos gestionados por administrador**
   - Solo el administrador puede crear/editar/eliminar medicamentos y vacunas
   - Recepcionistas solo pueden consultar los activos

3. **Precios al momento del cobro**
   - Los precios se copian del catálogo al crear el pago
   - Si el precio cambia después, no afecta pagos anteriores

### Ajustes de Pago

1. **Solo recepcionistas pueden ajustar**
   - Requiere autenticación con rol RECEPCIONISTA
   - El email y nombre se extraen del JWT token

2. **Motivo obligatorio**
   - Mínimo 10 caracteres
   - Máximo 500 caracteres
   - Debe explicar claramente la razón del ajuste

3. **Auditoría completa**
   - Se registra: quién, cuándo, monto original, monto ajustado, diferencia
   - El historial es inmutable
   - Visible para administrador en el detalle del pago

4. **Sin límite de ajustes**
   - Un pago puede tener múltiples ajustes
   - Cada ajuste se registra en el historial

---

## Migración de Base de Datos

Ejecutar el script de migración:

```bash
mysql -u root -p pawsoft < backendPawsoft/scripts/migration-medication-vaccine-pricing.sql
```

El script crea:
- Tabla `medication_catalog`
- Tabla `vaccine_catalog`
- Tabla `payment_items`
- Tabla `payment_adjustments`
- Datos de ejemplo (medicamentos y vacunas comunes)

---

## Ejemplos de Uso

### Ejemplo 1: Consulta Simple con Medicamento

**Escenario:** Consulta general + antibiótico aplicado

```json
{
  "concept": "Consulta + Amoxicilina",
  "baseAmount": 50000.00,
  "amount": 65000.00,
  "items": [
    {
      "itemType": "SERVICE",
      "itemName": "Consulta general",
      "quantity": 1,
      "unitPrice": 50000.00
    },
    {
      "itemType": "MEDICATION",
      "itemName": "Amoxicilina 500mg",
      "quantity": 1,
      "unit": "tableta",
      "unitPrice": 15000.00
    }
  ]
}
```

### Ejemplo 2: Vacunación Múltiple

**Escenario:** Aplicación de 2 vacunas

```json
{
  "concept": "Vacunación anual",
  "baseAmount": 0.00,
  "amount": 80000.00,
  "items": [
    {
      "itemType": "VACCINE",
      "itemName": "Vacuna Séxtuple Canina",
      "quantity": 1,
      "unit": "dosis",
      "unitPrice": 45000.00
    },
    {
      "itemType": "VACCINE",
      "itemName": "Vacuna Antirrábica",
      "quantity": 1,
      "unit": "dosis",
      "unitPrice": 35000.00
    }
  ]
}
```

### Ejemplo 3: Ajuste con Descuento

**Escenario:** Cliente frecuente recibe descuento

```http
PUT /api/recepcionista/payments/123/adjust

{
  "adjustedAmount": 85000.00,
  "reason": "Descuento del 15% por cliente frecuente (más de 10 visitas en el año) según política de fidelización"
}
```

**Resultado:**
- Monto original: 100,000 COP
- Monto ajustado: 85,000 COP
- Diferencia: -15,000 COP
- Registrado por: María García (recepcionista@pawsoft.com)

---

## Seguridad

### Validaciones

- **Precios:** No pueden ser negativos
- **Cantidades:** Deben ser mayores a 0
- **Motivo de ajuste:** Entre 10 y 500 caracteres
- **Nombres únicos:** Medicamentos y vacunas no pueden duplicarse

### Auditoría

Todas las operaciones se registran en `audit_logs`:
- `MEDICATION_UPSERT`: Creación/actualización de medicamento
- `MEDICATION_DELETE`: Eliminación de medicamento
- `VACCINE_UPSERT`: Creación/actualización de vacuna
- `VACCINE_DELETE`: Eliminación de vacuna
- `PAYMENT_CREATE`: Creación de pago con ítems
- `PAYMENT_ADJUSTED`: Ajuste de monto de pago

### Permisos

| Operación | Admin | Recepcionista | Veterinario | Cliente |
|-----------|-------|---------------|-------------|---------|
| Ver catálogos activos | ✓ | ✓ | ✗ | ✗ |
| Gestionar catálogos | ✓ | ✗ | ✗ | ✗ |
| Crear pago con ítems | ✗ | ✓ | ✗ | ✗ |
| Ajustar monto | ✗ | ✓ | ✗ | ✗ |
| Ver ajustes | ✓ | ✓ | ✗ | ✗ |

---

## Notas Técnicas

### Cascada de Eliminación

- Si se elimina un pago, sus ítems y ajustes se eliminan automáticamente
- Si se elimina un medicamento/vacuna del catálogo, NO afecta pagos anteriores (los ítems ya tienen el nombre copiado)

### Cálculo de Subtotales

Los subtotales se calculan automáticamente:
```java
subtotal = quantity × unitPrice
```

### Inmutabilidad

- Los ítems de pago son inmutables una vez creados
- Los ajustes son inmutables una vez registrados
- El historial de ajustes es permanente

---

## Proyecto

**Pawsoft — Software III**  
Universidad del Quindío  
Programa: Ingeniería de Sistemas y Computación

**Autoras:**
- Valentina Porras Salazar
- Helen Xiomara Giraldo Libreros

**Profesor:**
Raúl Yulbraynner Rivera Gálvez
