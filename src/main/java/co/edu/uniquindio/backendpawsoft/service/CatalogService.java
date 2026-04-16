package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.MedicationCatalogResponse;
import co.edu.uniquindio.backendpawsoft.dto.VaccineCatalogResponse;
import co.edu.uniquindio.backendpawsoft.repository.MedicationCatalogRepository;
import co.edu.uniquindio.backendpawsoft.repository.VaccineCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {
    
    private final MedicationCatalogRepository medicationCatalogRepository;
    private final VaccineCatalogRepository vaccineCatalogRepository;
    
    /**
     * Obtiene todos los medicamentos activos del catálogo
     */
    public List<MedicationCatalogResponse> getActiveMedications() {
        return medicationCatalogRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(medication -> MedicationCatalogResponse.builder()
                        .id(medication.getId())
                        .name(medication.getName())
                        .description(medication.getDescription())
                        .price(medication.getPrice())
                        .unit(medication.getUnit())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las vacunas activas del catálogo
     */
    public List<VaccineCatalogResponse> getActiveVaccines() {
        return vaccineCatalogRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(vaccine -> VaccineCatalogResponse.builder()
                        .id(vaccine.getId())
                        .name(vaccine.getName())
                        .description(vaccine.getDescription())
                        .price(vaccine.getPrice())
                        .build())
                .collect(Collectors.toList());
    }
}
