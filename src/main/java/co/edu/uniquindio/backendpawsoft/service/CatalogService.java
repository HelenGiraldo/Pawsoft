package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.model.MedicationCatalog;
import co.edu.uniquindio.backendpawsoft.model.VaccineCatalog;
import co.edu.uniquindio.backendpawsoft.repository.MedicationCatalogRepository;
import co.edu.uniquindio.backendpawsoft.repository.VaccineCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para gestionar catálogos de medicamentos y vacunas.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MedicationCatalogRepository medicationRepository;
    private final VaccineCatalogRepository vaccineRepository;
    private final AuditLogService auditLogService;

    /* ════════════════════════════════════════════════════════════
       MEDICAMENTOS
    ════════════════════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public List<MedicationCatalogResponse> getAllMedications() {
        return medicationRepository.findAll().stream()
                .map(this::toMedicationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicationCatalogResponse> getActiveMedications() {
        return medicationRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toMedicationResponse)
                .toList();
    }

    @Transactional
    public MedicationCatalogResponse createOrUpdateMedication(MedicationCatalogRequest req) {
        MedicationCatalog medication = medicationRepository
                .findByName(req.getName())
                .orElseGet(MedicationCatalog::new);

        medication.setName(req.getName());
        medication.setDescription(req.getDescription());
        medication.setPrice(req.getPrice());
        medication.setUnit(req.getUnit());
        medication.setActive(req.isActive());

        MedicationCatalog saved = medicationRepository.save(medication);

        auditLogService.log("MEDICATION_UPSERT", 
                "Medicamento creado/actualizado: " + req.getName(), 
                "MEDICATION", saved.getId().intValue());

        return toMedicationResponse(saved);
    }

    @Transactional
    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new NoSuchElementException("Medicamento no encontrado: " + id);
        }
        medicationRepository.deleteById(id);

        auditLogService.log("MEDICATION_DELETE", 
                "Medicamento eliminado", 
                "MEDICATION", id.intValue());
    }

    /* ════════════════════════════════════════════════════════════
       VACUNAS
    ════════════════════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public List<VaccineCatalogResponse> getAllVaccines() {
        return vaccineRepository.findAll().stream()
                .map(this::toVaccineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VaccineCatalogResponse> getActiveVaccines() {
        return vaccineRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toVaccineResponse)
                .toList();
    }

    @Transactional
    public VaccineCatalogResponse createOrUpdateVaccine(VaccineCatalogRequest req) {
        VaccineCatalog vaccine = vaccineRepository
                .findByName(req.getName())
                .orElseGet(VaccineCatalog::new);

        vaccine.setName(req.getName());
        vaccine.setDescription(req.getDescription());
        vaccine.setPrice(req.getPrice());
        vaccine.setActive(req.isActive());

        VaccineCatalog saved = vaccineRepository.save(vaccine);

        auditLogService.log("VACCINE_UPSERT", 
                "Vacuna creada/actualizada: " + req.getName(), 
                "VACCINE", saved.getId().intValue());

        return toVaccineResponse(saved);
    }

    @Transactional
    public void deleteVaccine(Long id) {
        if (!vaccineRepository.existsById(id)) {
            throw new NoSuchElementException("Vacuna no encontrada: " + id);
        }
        vaccineRepository.deleteById(id);

        auditLogService.log("VACCINE_DELETE", 
                "Vacuna eliminada", 
                "VACCINE", id.intValue());
    }

    /* ════════════════════════════════════════════════════════════
       MAPPERS
    ════════════════════════════════════════════════════════════ */

    private MedicationCatalogResponse toMedicationResponse(MedicationCatalog m) {
        return MedicationCatalogResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .unit(m.getUnit())
                .active(m.isActive())
                .build();
    }

    private VaccineCatalogResponse toVaccineResponse(VaccineCatalog v) {
        return VaccineCatalogResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .description(v.getDescription())
                .price(v.getPrice())
                .active(v.isActive())
                .build();
    }
}
