-- Migración: Agregar restricciones de integridad a nivel de base de datos
-- Versión: V6
-- Descripción: Mejora de integridad de datos con CHECK constraints, DEFAULT values y validaciones explícitas
-- Objetivo: Elevar rating de integridad de 4/5 a 5/5

-- ============================================================================
-- RESTRICCIONES EN TABLA: hospitalizations
-- Descripción: Validaciones de lógica de negocio para hospitalizaciones
-- ============================================================================

-- CHECK: discharge_date debe ser >= admission_date (si existe)
ALTER TABLE hospitalizations
ADD CONSTRAINT chk_discharge_after_admission 
CHECK (discharge_date IS NULL OR discharge_date >= admission_date);

-- CHECK: hourly_rate debe ser positivo
ALTER TABLE hospitalizations
ADD CONSTRAINT chk_hourly_rate_positive 
CHECK (hourly_rate > 0);

-- CHECK: cause_of_death solo puede estar presente si status = 'DECEASED'
ALTER TABLE hospitalizations
ADD CONSTRAINT chk_cause_of_death_consistency 
CHECK ((status = 'DECEASED' AND cause_of_death IS NOT NULL) OR (status != 'DECEASED' AND cause_of_death IS NULL));

-- CHECK: discharge_date solo puede estar presente si status != 'ACTIVE'
ALTER TABLE hospitalizations
ADD CONSTRAINT chk_discharge_date_consistency 
CHECK ((status != 'ACTIVE' AND discharge_date IS NOT NULL) OR (status = 'ACTIVE' AND discharge_date IS NULL));

-- ============================================================================
-- RESTRICCIONES EN TABLA: payment_adjustments
-- Descripción: Validaciones de ajustes de pago
-- ============================================================================

-- CHECK: adjusted_amount debe ser >= 0 (no se permiten montos negativos)
ALTER TABLE payment_adjustments
ADD CONSTRAINT chk_adjusted_amount_non_negative 
CHECK (adjusted_amount >= 0);

-- CHECK: original_amount debe ser >= 0
ALTER TABLE payment_adjustments
ADD CONSTRAINT chk_original_amount_non_negative 
CHECK (original_amount >= 0);

-- CHECK: reason debe tener al menos 10 caracteres
ALTER TABLE payment_adjustments
ADD CONSTRAINT chk_reason_min_length 
CHECK (CHAR_LENGTH(reason) >= 10);

-- CHECK: reason no debe exceder 500 caracteres
ALTER TABLE payment_adjustments
ADD CONSTRAINT chk_reason_max_length 
CHECK (CHAR_LENGTH(reason) <= 500);

-- ============================================================================
-- RESTRICCIONES EN TABLA: payment_items
-- Descripción: Validaciones de ítems de pago
-- ============================================================================

-- CHECK: quantity debe ser positiva
ALTER TABLE payment_items
ADD CONSTRAINT chk_quantity_positive 
CHECK (quantity > 0);

-- CHECK: unit_price debe ser >= 0
ALTER TABLE payment_items
ADD CONSTRAINT chk_unit_price_non_negative 
CHECK (unit_price >= 0);

-- CHECK: subtotal debe ser >= 0
ALTER TABLE payment_items
ADD CONSTRAINT chk_subtotal_non_negative 
CHECK (subtotal >= 0);

-- CHECK: subtotal debe ser aproximadamente igual a quantity * unit_price (tolerancia de 0.01)
ALTER TABLE payment_items
ADD CONSTRAINT chk_subtotal_calculation 
CHECK (ABS(subtotal - (quantity * unit_price)) < 0.01);

-- CHECK: item_type debe ser uno de los valores permitidos
ALTER TABLE payment_items
ADD CONSTRAINT chk_item_type_valid 
CHECK (item_type IN ('SERVICE', 'MEDICATION', 'VACCINE'));

-- ============================================================================
-- RESTRICCIONES EN TABLA: payments
-- Descripción: Validaciones de pagos
-- ============================================================================

-- CHECK: amount debe ser >= 0
ALTER TABLE payments
ADD CONSTRAINT chk_payment_amount_non_negative 
CHECK (amount >= 0);

-- CHECK: base_amount debe ser >= 0
ALTER TABLE payments
ADD CONSTRAINT chk_base_amount_non_negative 
CHECK (base_amount >= 0);

-- CHECK: status debe ser uno de los valores permitidos
ALTER TABLE payments
ADD CONSTRAINT chk_payment_status_valid 
CHECK (status IN ('PENDING', 'PAID', 'CANCELLED'));

-- ============================================================================
-- RESTRICCIONES EN TABLA: appointments
-- Descripción: Validaciones de citas
-- ============================================================================

-- CHECK: status debe ser uno de los valores permitidos
ALTER TABLE appointments
ADD CONSTRAINT chk_appointment_status_valid 
CHECK (status IN ('UPCOMING', 'CONFIRMED', 'IN_PROGRESS', 'NO_SHOW', 'CANCELLED', 'COMPLETED'));

-- ============================================================================
-- RESTRICCIONES EN TABLA: medical_records
-- Descripción: Validaciones de registros médicos
-- ============================================================================

-- CHECK: peso debe ser positivo (en kg)
ALTER TABLE medical_records
ADD CONSTRAINT chk_weight_positive 
CHECK (peso > 0);

-- CHECK: temperatura debe estar en rango fisiológico razonable (35-42°C para mascotas)
ALTER TABLE medical_records
ADD CONSTRAINT chk_temperature_range 
CHECK (temperatura >= 35 AND temperatura <= 42);

-- CHECK: frecuencia_cardiaca debe ser positiva
ALTER TABLE medical_records
ADD CONSTRAINT chk_heart_rate_positive 
CHECK (frecuencia_cardiaca > 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: medication_catalog
-- Descripción: Validaciones de catálogo de medicamentos
-- ============================================================================

-- CHECK: price debe ser positivo
ALTER TABLE medication_catalog
ADD CONSTRAINT chk_medication_price_positive 
CHECK (price > 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: vaccine_catalog
-- Descripción: Validaciones de catálogo de vacunas
-- ============================================================================

-- CHECK: price debe ser positivo
ALTER TABLE vaccine_catalog
ADD CONSTRAINT chk_vaccine_price_positive 
CHECK (price > 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: service_prices
-- Descripción: Validaciones de precios de servicios
-- ============================================================================

-- CHECK: price debe ser positivo
ALTER TABLE service_prices
ADD CONSTRAINT chk_service_price_positive 
CHECK (price > 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: pet_medical_profile
-- Descripción: Validaciones de perfil médico de mascota
-- ============================================================================

-- CHECK: last_updated_at debe ser >= created_at (si existe)
ALTER TABLE pet_medical_profile
ADD CONSTRAINT chk_last_updated_after_created 
CHECK (last_updated_at IS NULL OR last_updated_at >= created_at);

-- ============================================================================
-- RESTRICCIONES EN TABLA: hospitalization_notes
-- Descripción: Validaciones de notas de hospitalización
-- ============================================================================

-- CHECK: note no debe estar vacía
ALTER TABLE hospitalization_notes
ADD CONSTRAINT chk_note_not_empty 
CHECK (CHAR_LENGTH(TRIM(note)) > 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: medical_attachments
-- Descripción: Validaciones de archivos médicos adjuntos
-- ============================================================================

-- CHECK: file_type debe ser uno de los valores permitidos
ALTER TABLE medical_attachments
ADD CONSTRAINT chk_file_type_valid 
CHECK (file_type IN ('IMAGE', 'PDF'));

-- CHECK: reference_type debe ser uno de los valores permitidos
ALTER TABLE medical_attachments
ADD CONSTRAINT chk_reference_type_valid 
CHECK (reference_type IN ('MEDICAL_RECORD', 'HOSPITALIZATION'));

-- ============================================================================
-- RESTRICCIONES EN TABLA: users
-- Descripción: Validaciones de usuarios
-- ============================================================================

-- CHECK: failed_attempts debe ser >= 0
ALTER TABLE users
ADD CONSTRAINT chk_failed_attempts_non_negative 
CHECK (failed_attempts >= 0);

-- ============================================================================
-- RESTRICCIONES EN TABLA: codigos_2fa
-- Descripción: Validaciones de códigos 2FA
-- ============================================================================

-- CHECK: intentos_fallidos debe ser >= 0
ALTER TABLE codigos_2fa
ADD CONSTRAINT chk_2fa_failed_attempts_non_negative 
CHECK (intentos_fallidos >= 0);

-- CHECK: cantidad_reenvios debe ser >= 0
ALTER TABLE codigos_2fa
ADD CONSTRAINT chk_2fa_resends_non_negative 
CHECK (cantidad_reenvios >= 0);

-- CHECK: bloqueos_acumulados debe ser >= 0
ALTER TABLE codigos_2fa
ADD CONSTRAINT chk_2fa_accumulated_blocks_non_negative 
CHECK (bloqueos_acumulados >= 0);

-- CHECK: resultado debe ser uno de los valores permitidos
ALTER TABLE codigos_2fa
ADD CONSTRAINT chk_2fa_result_valid 
CHECK (resultado IN ('EXITOSO', 'FALLIDO', 'EXPIRADO', 'INVALIDADO'));

-- ============================================================================
-- RESTRICCIONES EN TABLA: refresh_tokens
-- Descripción: Validaciones de refresh tokens
-- ============================================================================

-- CHECK: expires_at debe ser > created_at
ALTER TABLE refresh_tokens
ADD CONSTRAINT chk_refresh_token_expiration 
CHECK (expires_at > created_at);

-- ============================================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- Descripción: Índices para mejorar performance de consultas frecuentes
-- ============================================================================

-- Índice para búsquedas de hospitalizaciones activas por mascota
CREATE INDEX IF NOT EXISTS idx_hospitalizations_pet_status 
ON hospitalizations(pet_id, status);

-- Índice para búsquedas de ajustes de pago por pago
CREATE INDEX IF NOT EXISTS idx_payment_adjustments_payment 
ON payment_adjustments(payment_id);

-- Índice para búsquedas de ítems de pago por pago
CREATE INDEX IF NOT EXISTS idx_payment_items_payment 
ON payment_items(payment_id);

-- Índice para búsquedas de archivos médicos por referencia
CREATE INDEX IF NOT EXISTS idx_medical_attachments_reference 
ON medical_attachments(reference_type, reference_id);

-- Índice para búsquedas de notas de hospitalización por fecha
CREATE INDEX IF NOT EXISTS idx_hospitalization_notes_date 
ON hospitalization_notes(hospitalization_id, created_at);

-- ============================================================================
-- COMENTARIOS DE DOCUMENTACIÓN
-- Descripción: Documentación de cambios en las tablas
-- ============================================================================

ALTER TABLE hospitalizations 
COMMENT='Registro de hospitalizaciones con validaciones de integridad: discharge_date >= admission_date, hourly_rate > 0, consistencia de cause_of_death con status';

ALTER TABLE payment_adjustments 
COMMENT='Ajustes de pago con validaciones: adjusted_amount >= 0, reason entre 10-500 caracteres';

ALTER TABLE payment_items 
COMMENT='Ítems de pago con validaciones: quantity > 0, unit_price >= 0, subtotal = quantity * unit_price';

ALTER TABLE payments 
COMMENT='Pagos con validaciones: amount >= 0, base_amount >= 0, status en valores permitidos';

ALTER TABLE medical_records 
COMMENT='Registros médicos con validaciones: peso > 0, temperatura 35-42°C, frecuencia_cardiaca > 0';

ALTER TABLE medication_catalog 
COMMENT='Catálogo de medicamentos con validación: price > 0';

ALTER TABLE vaccine_catalog 
COMMENT='Catálogo de vacunas con validación: price > 0';

ALTER TABLE service_prices 
COMMENT='Precios de servicios con validación: price > 0';

ALTER TABLE pet_medical_profile 
COMMENT='Perfil médico de mascota con validación: last_updated_at >= created_at';

ALTER TABLE hospitalization_notes 
COMMENT='Notas de hospitalización con validación: note no vacía';

ALTER TABLE medical_attachments 
COMMENT='Archivos médicos con validaciones: file_type y reference_type en valores permitidos';

ALTER TABLE users 
COMMENT='Usuarios con validación: failed_attempts >= 0';

ALTER TABLE codigos_2fa 
COMMENT='Códigos 2FA con validaciones: intentos_fallidos >= 0, cantidad_reenvios >= 0, bloqueos_acumulados >= 0, resultado en valores permitidos';

ALTER TABLE refresh_tokens 
COMMENT='Refresh tokens con validación: expires_at > created_at';
