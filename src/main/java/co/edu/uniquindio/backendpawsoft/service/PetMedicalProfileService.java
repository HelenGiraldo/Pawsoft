package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.PetMedicalProfileDTO;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.PetMedicalProfile;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PetMedicalProfileRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la hoja médica maestra de mascotas.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PetMedicalProfileService {

    private final PetMedicalProfileRepository profileRepository;
    private final PetRepository petRepository;

    /**
     * Crea un perfil médico inicial con información proporcionada al registrar la mascota.
     * Este método es usado cuando se registra una mascota con información médica inicial.
     * 
     * @param petId ID de la mascota
     * @param bloodType Tipo de sangre
     * @param knownAllergies Alergias conocidas
     * @param chronicConditions Condiciones crónicas
     * @param currentMedications Medicamentos actuales
     * @param additionalNotes Notas adicionales
     * @param createdBy Usuario que crea el perfil
     */
    public void createInitialProfile(
            Long petId,
            String bloodType,
            String knownAllergies,
            String chronicConditions,
            String currentMedications,
            String additionalNotes,
            User createdBy
    ) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        
        // Verificar si ya existe un perfil (no debería existir, pero por seguridad)
        if (profileRepository.findByPetId(petId).isPresent()) {
            return; // Ya existe, no crear duplicado
        }
        
        PetMedicalProfile profile = PetMedicalProfile.builder()
                .pet(pet)
                .bloodType(bloodType != null && !bloodType.trim().isEmpty() ? bloodType.trim() : null)
                .knownAllergies(knownAllergies != null && !knownAllergies.trim().isEmpty() ? knownAllergies.trim() : null)
                .chronicConditions(chronicConditions != null && !chronicConditions.trim().isEmpty() ? chronicConditions.trim() : null)
                .currentMedications(currentMedications != null && !currentMedications.trim().isEmpty() ? currentMedications.trim() : null)
                .surgicalHistory(additionalNotes != null && !additionalNotes.trim().isEmpty() ? additionalNotes.trim() : null)
                .lastUpdatedBy(createdBy)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        
        profileRepository.save(profile);
    }

    /**
     * Obtiene o crea el perfil médico de una mascota.
     * Si no existe, crea uno vacío.
     * 
     * @param petId ID de la mascota
     * @return Perfil médico de la mascota
     */
    public PetMedicalProfile getOrCreateProfile(Long petId) {
        return profileRepository.findByPetId(petId)
                .orElseGet(() -> {
                    Pet pet = petRepository.findById(petId)
                            .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
                    
                    PetMedicalProfile profile = PetMedicalProfile.builder()
                            .pet(pet)
                            .build();
                    
                    return profileRepository.save(profile);
                });
    }

    /**
     * Obtiene el perfil médico como DTO con información calculada.
     * 
     * @param petId ID de la mascota
     * @return DTO con información del perfil médico
     */
    @Transactional(readOnly = true)
    public PetMedicalProfileDTO getProfileDTO(Long petId) {
        PetMedicalProfile profile = getOrCreateProfile(petId);
        Pet pet = profile.getPet();
        
        Integer age = null;
        if (pet.getBirthDate() != null && !pet.getBirthDate().isEmpty()) {
            try {
                age = Period.between(LocalDate.parse(pet.getBirthDate()), LocalDate.now()).getYears();
            } catch (Exception e) {
                // Si no se puede parsear la fecha, dejar age como null
            }
        }
        
        return PetMedicalProfileDTO.builder()
                .id(profile.getId())
                .petId(pet.getId())
                .petName(pet.getName())
                .petSpecies(pet.getSpecies())
                .petBreed(pet.getBreed())
                .petAge(age)
                .knownAllergies(profile.getKnownAllergies())
                .chronicConditions(profile.getChronicConditions())
                .surgicalHistory(profile.getSurgicalHistory())
                .currentMedications(profile.getCurrentMedications())
                .bloodType(profile.getBloodType())
                .lastUpdatedByName(profile.getLastUpdatedBy() != null ? 
                        profile.getLastUpdatedBy().getName() : null)
                .lastUpdatedByRole(profile.getLastUpdatedBy() != null ?
                        profile.getLastUpdatedBy().getRole().name() : null)
                .lastUpdatedAt(profile.getLastUpdatedAt())
                .build();
    }

    /**
     * Actualiza la hoja médica maestra después de una consulta.
     * 
     * Lógica de actualización:
     * - Alergias: se CONCATENAN (acumulativas, sin duplicados)
     * - Condiciones: se CONCATENAN (acumulativas, sin duplicados)
     * - Antecedentes quirúrgicos: se CONCATENAN (acumulativas)
     * - Medicamentos actuales: se REEMPLAZAN (cambian en cada consulta)
     * - Tipo de sangre: se REEMPLAZA (raro, pero posible)
     * 
     * @param petId ID de la mascota
     * @param allergiesFound Alergias encontradas en la consulta
     * @param conditionsFound Condiciones encontradas en la consulta
     * @param surgicalNote Nota quirúrgica si aplica
     * @param currentMeds Medicamentos actuales prescritos
     * @param bloodType Tipo de sangre (si se actualiza)
     * @param vet Veterinario que realiza la actualización
     */
    public void updateProfileAfterConsultation(
            Long petId,
            String allergiesFound,
            String conditionsFound,
            String surgicalNote,
            String currentMeds,
            String bloodType,
            User vet
    ) {
        PetMedicalProfile profile = getOrCreateProfile(petId);
        
        // Concatenar alergias con deduplicación
        if (allergiesFound != null && !allergiesFound.isBlank()) {
            Set<String> allAllergies = new HashSet<>();
            
            // Agregar alergias existentes
            if (profile.getKnownAllergies() != null && !profile.getKnownAllergies().isBlank()) {
                allAllergies.addAll(
                    Arrays.stream(profile.getKnownAllergies().split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet())
                );
            }
            
            // Agregar alergias nuevas
            allAllergies.addAll(
                Arrays.stream(allergiesFound.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
            );
            
            profile.setKnownAllergies(String.join("\n", allAllergies));
        }
        
        // Concatenar condiciones con deduplicación
        if (conditionsFound != null && !conditionsFound.isBlank()) {
            Set<String> allConditions = new HashSet<>();
            
            // Agregar condiciones existentes
            if (profile.getChronicConditions() != null && !profile.getChronicConditions().isBlank()) {
                allConditions.addAll(
                    Arrays.stream(profile.getChronicConditions().split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet())
                );
            }
            
            // Agregar condiciones nuevas
            allConditions.addAll(
                Arrays.stream(conditionsFound.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
            );
            
            profile.setChronicConditions(String.join("\n", allConditions));
        }
        
        // Concatenar antecedentes quirúrgicos
        if (surgicalNote != null && !surgicalNote.isBlank()) {
            if (profile.getSurgicalHistory() == null || profile.getSurgicalHistory().isBlank()) {
                profile.setSurgicalHistory(surgicalNote);
            } else {
                profile.setSurgicalHistory(profile.getSurgicalHistory() + "\n" + surgicalNote);
            }
        }
        
        // Reemplazar medicamentos actuales
        if (currentMeds != null && !currentMeds.isBlank()) {
            profile.setCurrentMedications(currentMeds);
        }
        
        // Reemplazar tipo de sangre (raro, pero posible)
        if (bloodType != null && !bloodType.isBlank()) {
            profile.setBloodType(bloodType);
        }
        
        // Registrar quién hizo la actualización
        profile.setLastUpdatedBy(vet);
        profile.setLastUpdatedAt(LocalDateTime.now());
        
        profileRepository.save(profile);
    }

    /**
     * Actualiza el tipo de sangre de la mascota.
     * 
     * @param petId ID de la mascota
     * @param bloodType Nuevo tipo de sangre
     * @param vet Veterinario que realiza la actualización
     */
    public void updateBloodType(Long petId, String bloodType, User vet) {
        PetMedicalProfile profile = getOrCreateProfile(petId);
        profile.setBloodType(bloodType);
        profile.setLastUpdatedBy(vet);
        profile.setLastUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }
}
