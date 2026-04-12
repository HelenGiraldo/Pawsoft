-- Migración: Sistema de Precios para Medicamentos y Vacunas
-- Fecha: 2026-04-11
-- Descripción: Agrega catálogos de medicamentos y vacunas con precios,
--              ítems de pago detallados y sistema de ajustes con auditoría

-- ============================================================================
-- CATÁLOGO DE MEDICAMENTOS
-- ============================================================================
CREATE TABLE IF NOT EXISTS medication_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(12,2) NOT NULL,
    unit VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_medication_active (active),
    INDEX idx_medication_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- CATÁLOGO DE VACUNAS
-- ============================================================================
CREATE TABLE IF NOT EXISTS vaccine_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_vaccine_active (active),
    INDEX idx_vaccine_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ÍTEMS DE PAGO (FACTURACIÓN DETALLADA)
-- ============================================================================
CREATE TABLE IF NOT EXISTS payment_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50),
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    INDEX idx_payment_item_payment (payment_id),
    INDEX idx_payment_item_type (item_type),
    CONSTRAINT fk_payment_item_payment 
        FOREIGN KEY (payment_id) 
        REFERENCES payments(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AJUSTES DE PAGO (AUDITORÍA)
-- ============================================================================
CREATE TABLE IF NOT EXISTS payment_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    original_amount DECIMAL(12,2) NOT NULL,
    adjusted_amount DECIMAL(12,2) NOT NULL,
    difference DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    adjusted_by VARCHAR(120) NOT NULL,
    adjusted_by_name VARCHAR(120) NOT NULL,
    adjusted_at DATETIME NOT NULL,
    INDEX idx_payment_adjustment_payment (payment_id),
    INDEX idx_payment_adjustment_date (adjusted_at),
    INDEX idx_payment_adjustment_by (adjusted_by),
    CONSTRAINT fk_payment_adjustment_payment 
        FOREIGN KEY (payment_id) 
        REFERENCES payments(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- DATOS DE EJEMPLO (OPCIONAL)
-- ============================================================================

-- Medicamentos comunes
INSERT INTO medication_catalog (name, description, price, unit, active) VALUES
('Amoxicilina 500mg', 'Antibiótico de amplio espectro', 15000.00, 'tableta', TRUE),
('Meloxicam 2mg/ml', 'Antiinflamatorio no esteroideo', 25000.00, 'ml', TRUE),
('Dipirona 500mg/ml', 'Analgésico y antipirético', 18000.00, 'ml', TRUE),
('Metronidazol 250mg', 'Antibiótico y antiparasitario', 12000.00, 'tableta', TRUE),
('Prednisolona 5mg', 'Corticosteroide', 8000.00, 'tableta', TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Vacunas comunes
INSERT INTO vaccine_catalog (name, description, price, active) VALUES
('Vacuna Séxtuple Canina', 'Protección contra 6 enfermedades', 45000.00, TRUE),
('Vacuna Antirrábica', 'Prevención de rabia', 35000.00, TRUE),
('Vacuna Triple Felina', 'Protección contra 3 enfermedades', 40000.00, TRUE),
('Vacuna Leucemia Felina', 'Prevención de leucemia', 50000.00, TRUE),
('Vacuna Bordetella', 'Prevención de tos de las perreras', 38000.00, TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- ============================================================================
-- VERIFICACIÓN
-- ============================================================================
SELECT 'Migración completada exitosamente' AS status;
SELECT COUNT(*) AS total_medicamentos FROM medication_catalog;
SELECT COUNT(*) AS total_vacunas FROM vaccine_catalog;
