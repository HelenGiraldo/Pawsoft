package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.PetRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.HospitalizationRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetMedicalProfileRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService Tests")
class PetServiceTest {

    @Mock private PetRepository petRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PetMedicalProfileRepository petMedicalProfileRepository;
    @Mock private HospitalizationRepository hospitalizationRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private PetService petService;

    private Pet pet;
    private final String ownerEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setBreed("Golden Retriever");
        pet.setBirthDate("2021-01-01");
        pet.setSex("M");
        pet.setOwnerEmail(ownerEmail);
    }

    @Test
    @DisplayName("Should get pets by owner email")
    void shouldGetPetsByOwnerEmail() {
        when(petRepository.findByOwnerEmail(ownerEmail)).thenReturn(List.of(pet));
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        List<PetResponse> result = petService.getByOwner(ownerEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Buddy", result.get(0).getName());
    }

    @Test
    @DisplayName("Should create pet successfully")
    void shouldCreatePetSuccessfully() {
        PetRequest req = buildPetRequest();
        when(petRepository.save(any(Pet.class))).thenReturn(pet);
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        PetResponse result = petService.create(req, ownerEmail);

        assertNotNull(result);
        verify(petRepository).save(any(Pet.class));
        verify(auditLogService).log(eq("PET_CREATE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should update pet successfully")
    void shouldUpdatePetSuccessfully() {
        PetRequest req = buildPetRequest();
        req.setName("Buddy Updated");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        PetResponse result = petService.update(1L, req, ownerEmail);

        assertNotNull(result);
        verify(petRepository).save(pet);
        verify(auditLogService).log(eq("PET_UPDATE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when updating pet not owned by user")
    void shouldThrowWhenUpdatingPetNotOwnedByUser() {
        PetRequest req = buildPetRequest();
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(RuntimeException.class,
                () -> petService.update(1L, req, "other@example.com"));
        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when updating non-existent pet")
    void shouldThrowWhenUpdatingNonExistentPet() {
        PetRequest req = buildPetRequest();
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> petService.update(99L, req, ownerEmail));
    }

    @Test
    @DisplayName("Should delete pet successfully")
    void shouldDeletePetSuccessfully() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        petService.delete(1L, ownerEmail);

        verify(appointmentRepository).deleteByPetId(1L);
        verify(petRepository).delete(pet);
        verify(auditLogService).log(eq("PET_DELETE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when deleting pet not owned by user")
    void shouldThrowWhenDeletingPetNotOwnedByUser() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(RuntimeException.class,
                () -> petService.delete(1L, "other@example.com"));
        verify(petRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw when deleting non-existent pet")
    void shouldThrowWhenDeletingNonExistentPet() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> petService.delete(99L, ownerEmail));
    }

    private PetRequest buildPetRequest() {
        PetRequest req = new PetRequest();
        req.setName("Buddy");
        req.setSpecies("Dog");
        req.setBreed("Golden Retriever");
        req.setBirthDate("2021-01-01");
        req.setSex("M");
        return req;
    }
}
