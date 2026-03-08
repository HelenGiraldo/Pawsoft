package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.PetRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
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

    public void delete(Long id, String ownerEmail) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        if (!pet.getOwnerEmail().equals(ownerEmail))
            throw new RuntimeException("No tienes permiso para eliminar esta mascota");

        petRepository.delete(pet);

        auditLogService.log("PET_DELETE", "Cliente eliminó mascota", "PET", id.intValue());
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