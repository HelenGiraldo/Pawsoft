package co.edu.uniquindio.backendpawsoft.service;

/**
 * Servicio para la gestión de mascotas del cliente autenticado.
 *
 * Permite listar, crear, actualizar y eliminar mascotas. Todas las operaciones
 * validan que el cliente autenticado sea el propietario de la mascota antes
 * de aplicar cambios. Cada operación queda registrada en auditoría.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.CreateMedicalProfileInitialRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.PetMedicalProfile;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetMedicalProfileRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final PetMedicalProfileRepository petMedicalProfileRepository;
    private final AuditLogService auditLogService;

    public List<PetResponse> getByOwner(String email) {
        return petRepository.findByOwnerEmail(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PetResponse create(PetRequest req, String ownerEmail) {
        Pet pet = Pet.builder()
                .name(req.getName())
                .species(req.getSpecies())
                .breed(req.getBreed())
                .birthDate(req.getBirthDate())
                .sex(req.getSex())
                .ownerEmail(ownerEmail)
                .photoUrl(req.getPhotoUrl())
                .build();

        Pet saved = petRepository.save(pet);

        auditLogService.log("PET_CREATE", "Cliente registró nueva mascota", "PET", saved.getId().intValue());

        return toResponse(saved);
    }

    public PetResponse update(Long id, PetRequest req, String ownerEmail) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        if (!pet.getOwnerEmail().equals(ownerEmail))
            throw new RuntimeException("No tienes permiso");
        pet.setName(req.getName());
        pet.setSpecies(req.getSpecies());
        pet.setBreed(req.getBreed());
        pet.setBirthDate(req.getBirthDate());
        pet.setSex(req.getSex());
        if (req.getPhotoUrl() != null && !req.getPhotoUrl().isBlank())
            pet.setPhotoUrl(req.getPhotoUrl());

        Pet saved = petRepository.save(pet);

        auditLogService.log("PET_UPDATE", "Cliente actualizó datos de mascota", "PET", id.intValue());

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, String ownerEmail) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        if (!pet.getOwnerEmail().equals(ownerEmail))
            throw new RuntimeException("No tienes permiso para eliminar esta mascota");

        // Eliminar citas asociadas antes de borrar la mascota (evita FK constraint)
        appointmentRepository.deleteByPetId(id);

        petRepository.delete(pet);

        auditLogService.log("PET_DELETE", "Cliente eliminó mascota", "PET", id.intValue());
    }

    /**
     * Crea una mascota con información médica inicial.
     * La hoja médica maestra se crea automáticamente con los datos proporcionados.
     * 
     * @param req Datos de la mascota
     * @param medicalProfileRequest Información médica inicial (opcional)
     * @param ownerEmail Email del propietario
     * @return Mascota creada
     */
    @Transactional
    public PetResponse createWithMedicalProfile(
            PetRequest req,
            CreateMedicalProfileInitialRequest medicalProfileRequest,
            String ownerEmail
    ) {
        // 1. Crear mascota
        Pet pet = Pet.builder()
                .name(req.getName())
                .species(req.getSpecies())
                .breed(req.getBreed())
                .birthDate(req.getBirthDate())
                .sex(req.getSex())
                .ownerEmail(ownerEmail)
                .photoUrl(req.getPhotoUrl())
                .build();
        
        Pet savedPet = petRepository.save(pet);
        
        // 2. Crear hoja médica maestra con datos iniciales
        if (medicalProfileRequest != null) {
            PetMedicalProfile profile = PetMedicalProfile.builder()
                    .pet(savedPet)
                    .bloodType(medicalProfileRequest.getBloodType())
                    .knownAllergies(medicalProfileRequest.getKnownAllergies())
                    .chronicConditions(medicalProfileRequest.getChronicConditions())
                    .currentMedications(medicalProfileRequest.getCurrentMedications())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            petMedicalProfileRepository.save(profile);
        }
        
        // 3. Registrar en auditoría
        auditLogService.log("PET_CREATE_WITH_MEDICAL_PROFILE", 
                "Cliente registró nueva mascota con información médica inicial", 
                "PET", savedPet.getId().intValue());
        
        return toResponse(savedPet);
    }

    private PetResponse toResponse(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .sex(pet.getSex())
                .ownerEmail(pet.getOwnerEmail())
                .photoUrl(pet.getPhotoUrl())
                .build();
    }
}