-- Script de inicialización de datos para catálogos de medicamentos y vacunas
-- Este script se ejecuta automáticamente al iniciar la aplicación si las tablas están vacías

-- Medicamentos con precios
INSERT INTO medication_catalog (name, description, price, unit, active, created_at, updated_at) 
SELECT 'Amoxicilina', 'Antibiótico de amplio espectro', 15000.00, 'ml', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM medication_catalog WHERE name = 'Amoxicilina');

INSERT INTO medication_catalog (name, description, price, unit, active, created_at, updated_at)
SELECT 'Meloxicam', 'Antiinflamatorio no esteroideo', 12000.00, 'ml', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM medication_catalog WHERE name = 'Meloxicam');

INSERT INTO medication_catalog (name, description, price, unit, active, created_at, updated_at)
SELECT 'Ivermectina', 'Antiparasitario', 8000.00, 'ml', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM medication_catalog WHERE name = 'Ivermectina');

INSERT INTO medication_catalog (name, description, price, unit, active, created_at, updated_at)
SELECT 'Dexametasona', 'Corticoide antiinflamatorio', 10000.00, 'ml', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM medication_catalog WHERE name = 'Dexametasona');

INSERT INTO medication_catalog (name, description, price, unit, active, created_at, updated_at)
SELECT 'Tramadol', 'Analgésico opioide', 18000.00, 'ml', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM medication_catalog WHERE name = 'Tramadol');

-- Vacunas con precios
INSERT INTO vaccine_catalog (name, description, price, active, created_at, updated_at)
SELECT 'Rabia', 'Vacuna antirrábica', 35000.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vaccine_catalog WHERE name = 'Rabia');

INSERT INTO vaccine_catalog (name, description, price, active, created_at, updated_at)
SELECT 'Parvovirus', 'Vacuna contra parvovirus canino', 40000.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vaccine_catalog WHERE name = 'Parvovirus');

INSERT INTO vaccine_catalog (name, description, price, active, created_at, updated_at)
SELECT 'Triple Felina', 'Vacuna contra rinotraqueítis, calicivirus y panleucopenia', 45000.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vaccine_catalog WHERE name = 'Triple Felina');

INSERT INTO vaccine_catalog (name, description, price, active, created_at, updated_at)
SELECT 'Sextuple Canina', 'Vacuna contra distemper, hepatitis, leptospirosis, parainfluenza, parvovirus y coronavirus', 50000.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vaccine_catalog WHERE name = 'Sextuple Canina');

INSERT INTO vaccine_catalog (name, description, price, active, created_at, updated_at)
SELECT 'Leucemia Felina', 'Vacuna contra el virus de la leucemia felina', 42000.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vaccine_catalog WHERE name = 'Leucemia Felina');
