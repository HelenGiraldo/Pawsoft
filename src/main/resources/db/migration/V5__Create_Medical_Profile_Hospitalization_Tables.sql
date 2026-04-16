-- Migración: Crear tablas para hoja médica maestra, hospitalizaciones y archivos adjuntos
-- Versión: V5
-- Descripción: Agrega funcionalidad de expediente clínico acumulativo y hospitalizaciones

-- ============================================================================
-- Tabla: pet_medical_profile
-- Descripción: Hoja médica maestra de la mascota (expediente clínico acumulativo)
-- ============================================================================
CREATE TABLE IF NOT EXISTS pet_medical_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pet_id BIGINT NOT NULL UNIQUE,
    known_allergies LONGTEXT NULL COMMENT 'Alergias acumuladas entre consultas',
    chronic_conditions LONGTEXT NULL COMMENT 'Condiciones crónicas acumuladas',
    surgical_history LONGTEXT NULL COMMENT 'Antecedentes quirúrgicos acumulados',
    current_medications LONGTEXT NULL COMMENT 'Medicamentos actuales (se reemplaza en cada consulta)',
    blood_type VARCHAR(10) NULL COMMENT 'Tipo de sangre',
    last_updated_by BIGINT NULL COMMENT 'Veterinario que hizo la última actualización',
    last_updated_at DATETIME NULL COMMENT 'Fecha de última actualización',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación',
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (last_updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_pet_id (pet_id),
    INDEX idx_last_updated_at (last_updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Hoja médica maestra de mascotas - expediente clínico acumulativo';

-- ============================================================================
-- Tabla: hospitalizations
-- Descripción: Registro de hospitalizaciones de mascotas
-- ============================================================================
CREATE TABLE IF NOT EXISTS hospitalizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    vet_id BIGINT NOT NULL COMMENT 'Veterinario responsable',
    appointment_id BIGINT NULL COMMENT 'Cita asociada (opcional)',
    status ENUM('ACTIVE', 'DISCHARGED', 'DECEASED') NOT NULL DEFAULT 'ACTIVE',
    admission_date DATETIME NOT NULL COMMENT 'Fecha y hora de admisión',
    discharge_date DATETIME NULL COMMENT 'Fecha y hora de alta',
    reason LONGTEXT NOT NULL COMMENT 'Motivo de la hospitalización',
    initial_observations LONGTEXT NULL COMMENT 'Observaciones iniciales',
    hourly_rate DECIMAL(10, 2) NOT NULL COMMENT 'Tarifa por hora',
    cause_of_death LONGTEXT NULL COMMENT 'Causa del fallecimiento (si aplica)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (vet_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    INDEX idx_pet_id (pet_id),
    INDEX idx_vet_id (vet_id),
    INDEX idx_status (status),
    INDEX idx_admission_date (admission_date),
    INDEX idx_discharge_date (discharge_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de hospitalizaciones de mascotas';

-- ============================================================================
-- Tabla: hospitalization_notes
-- Descripción: Notas de evolución durante hospitalizaciones
-- ============================================================================
CREATE TABLE IF NOT EXISTS hospitalization_notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hospitalization_id BIGINT NOT NULL,
    vet_id BIGINT NOT NULL COMMENT 'Veterinario que escribió la nota',
    note LONGTEXT NOT NULL COMMENT 'Contenido de la nota',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospitalization_id) REFERENCES hospitalizations(id) ON DELETE CASCADE,
    FOREIGN KEY (vet_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_hospitalization_id (hospitalization_id),
    INDEX idx_vet_id (vet_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Notas de evolución durante hospitalizaciones';

-- ============================================================================
-- Tabla: medical_attachments
-- Descripción: Archivos adjuntos a registros médicos o hospitalizaciones
-- ============================================================================
CREATE TABLE IF NOT EXISTS medical_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference_type ENUM('MEDICAL_RECORD', 'HOSPITALIZATION') NOT NULL COMMENT 'Tipo de referencia',
    reference_id BIGINT NOT NULL COMMENT 'ID de la referencia (medical_record_id o hospitalization_id)',
    file_url VARCHAR(500) NOT NULL COMMENT 'URL en Cloudinary',
    file_type ENUM('IMAGE', 'PDF') NOT NULL COMMENT 'Tipo de archivo',
    file_name VARCHAR(255) NOT NULL COMMENT 'Nombre original del archivo',
    uploaded_by BIGINT NOT NULL COMMENT 'Veterinario que subió el archivo',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_reference (reference_type, reference_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_uploaded_at (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Archivos adjuntos a registros médicos o hospitalizaciones';

-- ============================================================================
-- Alteraciones a tabla medical_records
-- Descripción: Agregar campos para antecedentes y comportamiento
-- ============================================================================
ALTER TABLE medical_records
ADD COLUMN IF NOT EXISTS allergies_found LONGTEXT NULL COMMENT 'Alergias detectadas en ESTA consulta',
ADD COLUMN IF NOT EXISTS conditions_found LONGTEXT NULL COMMENT 'Condiciones detectadas en ESTA consulta',
ADD COLUMN IF NOT EXISTS surgical_note LONGTEXT NULL COMMENT 'Antecedentes quirúrgicos relevantes',
ADD COLUMN IF NOT EXISTS current_meds_note LONGTEXT NULL COMMENT 'Medicamentos activos al momento',
ADD COLUMN IF NOT EXISTS mood_behavior VARCHAR(255) NULL COMMENT 'Estado de ánimo/comportamiento';
