package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.CreatePetRequest;
import co.edu.uniquindio.backendpawsoft.dto.UpdatePetRequest;
import co.edu.uniquindio.backendpawsoft.exception.PetNotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedPetAccessException;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService Tests")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PetService petService;

    private User testUser;
    private Pet testPet;
    private CreatePetRequest createPetRequest;
    private UpdatePetRequest updatePetRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        testPet = TestDataBuilder.buildTestPet(testUser);
        
        createPetRequest = CreatePetRequest.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(3)
                .weight(25.5)
                .color("Golden")
                .build();

        updatePetRequest = UpdatePetRequest.builder()
                .name("Updated Buddy")
                .age(4)
                .weight(26.0)
                .build();
    }

    @Test
    @DisplayName("Should successfully create pet")
    void shouldCreatePet() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // When
        Pet result = petService.createPet(createPetRequest, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testPet.getName(), result.getName());
        assertEquals(testPet.getSpecies(), result.getSpecies());
        assertEquals(testPet.getOwner(), result.getOwner());

        verify(userRepository).findById(testUser.getId());
        verify(petRepository).save(any(Pet.class));
        verify(auditLogService).logPetCreated(testUser.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw exception when owner not found")
    void shouldThrowExceptionWhenOwnerNotFound() {
        // Given
        UUID ownerId = UUID.randomUUID();
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> petService.createPet(createPetRequest, ownerId));

        verify(userRepository).findById(ownerId);
        verifyNoInteractions(petRepository);
    }

    @Test
    @DisplayName("Should successfully get pet by ID")
    void shouldGetPetById() {
        // Given
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));

        // When
        Pet result = petService.getPetById(testPet.getId());

        // Then
        assertNotNull(result);
        assertEquals(testPet.getId(), result.getId());
        assertEquals(testPet.getName(), result.getName());

        verify(petRepository).findById(testPet.getId());
    }

    @Test
    @DisplayName("Should throw exception when pet not found by ID")
    void shouldThrowExceptionWhenPetNotFoundById() {
        // Given
        UUID petId = UUID.randomUUID();
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PetNotFoundException.class, () -> petService.getPetById(petId));

        verify(petRepository).findById(petId);
    }

    @Test
    @DisplayName("Should successfully get pets by owner")
    void shouldGetPetsByOwner() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = Arrays.asList(testPet);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, 1);
        
        when(petRepository.findByOwnerIdAndIsActiveTrue(testUser.getId(), pageable))
                .thenReturn(petPage);

        // When
        Page<Pet> result = petService.getPetsByOwner(testUser.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPet.getId(), result.getContent().get(0).getId());

        verify(petRepository).findByOwnerIdAndIsActiveTrue(testUser.getId(), pageable);
    }

    @Test
    @DisplayName("Should successfully update pet")
    void shouldUpdatePet() {
        // Given
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(petRepository.save(testPet)).thenReturn(testPet);

        // When
        Pet result = petService.updatePet(testPet.getId(), updatePetRequest, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(updatePetRequest.getName(), result.getName());
        assertEquals(updatePetRequest.getAge(), result.getAge());
        assertEquals(updatePetRequest.getWeight(), result.getWeight());

        verify(petRepository).findById(testPet.getId());
        verify(petRepository).save(testPet);
        verify(auditLogService).logPetUpdated(testUser.getId(), testPet.getId());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent pet")
    void shouldThrowExceptionWhenUpdatingNonExistentPet() {
        // Given
        UUID petId = UUID.randomUUID();
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PetNotFoundException.class, 
                () -> petService.updatePet(petId, updatePetRequest, testUser.getId()));

        verify(petRepository).findById(petId);
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should throw exception when user tries to update another user's pet")
    void shouldThrowExceptionWhenUserTriesToUpdateAnotherUsersPet() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));

        // When & Then
        assertThrows(UnauthorizedPetAccessException.class, 
                () -> petService.updatePet(testPet.getId(), updatePetRequest, anotherUserId));

        verify(petRepository).findById(testPet.getId());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should successfully delete pet")
    void shouldDeletePet() {
        // Given
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(petRepository.save(testPet)).thenReturn(testPet);

        // When
        petService.deletePet(testPet.getId(), testUser.getId());

        // Then
        assertFalse(testPet.isActive());

        verify(petRepository).findById(testPet.getId());
        verify(petRepository).save(testPet);
        verify(auditLogService).logPetDeleted(testUser.getId(), testPet.getId());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pet")
    void shouldThrowExceptionWhenDeletingNonExistentPet() {
        // Given
        UUID petId = UUID.randomUUID();
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PetNotFoundException.class, 
                () -> petService.deletePet(petId, testUser.getId()));

        verify(petRepository).findById(petId);
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should throw exception when user tries to delete another user's pet")
    void shouldThrowExceptionWhenUserTriesToDeleteAnotherUsersPet() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));

        // When & Then
        assertThrows(UnauthorizedPetAccessException.class, 
                () -> petService.deletePet(testPet.getId(), anotherUserId));

        verify(petRepository).findById(testPet.getId());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Should successfully search pets by name")
    void shouldSearchPetsByName() {
        // Given
        String searchTerm = "Buddy";
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = Arrays.asList(testPet);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, 1);
        
        when(petRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(searchTerm, pageable))
                .thenReturn(petPage);

        // When
        Page<Pet> result = petService.searchPetsByName(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPet.getId(), result.getContent().get(0).getId());

        verify(petRepository).findByNameContainingIgnoreCaseAndIsActiveTrue(searchTerm, pageable);
    }

    @Test
    @DisplayName("Should successfully get pets by species")
    void shouldGetPetsBySpecies() {
        // Given
        String species = "Dog";
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = Arrays.asList(testPet);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, 1);
        
        when(petRepository.findBySpeciesIgnoreCaseAndIsActiveTrue(species, pageable))
                .thenReturn(petPage);

        // When
        Page<Pet> result = petService.getPetsBySpecies(species, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPet.getId(), result.getContent().get(0).getId());

        verify(petRepository).findBySpeciesIgnoreCaseAndIsActiveTrue(species, pageable);
    }

    @Test
    @DisplayName("Should get pet statistics")
    void shouldGetPetStatistics() {
        // Given
        when(petRepository.countByIsActiveTrue()).thenReturn(100L);
        when(petRepository.countBySpeciesIgnoreCaseAndIsActiveTrue("Dog")).thenReturn(60L);
        when(petRepository.countBySpeciesIgnoreCaseAndIsActiveTrue("Cat")).thenReturn(30L);
        when(petRepository.countBySpeciesIgnoreCaseAndIsActiveTrue("Bird")).thenReturn(10L);

        // When
        var stats = petService.getPetStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.get("total"));
        assertEquals(60L, stats.get("dogs"));
        assertEquals(30L, stats.get("cats"));
        assertEquals(10L, stats.get("birds"));

        verify(petRepository).countByIsActiveTrue();
        verify(petRepository).countBySpeciesIgnoreCaseAndIsActiveTrue("Dog");
        verify(petRepository).countBySpeciesIgnoreCaseAndIsActiveTrue("Cat");
        verify(petRepository).countBySpeciesIgnoreCaseAndIsActiveTrue("Bird");
    }

    @Test
    @DisplayName("Should successfully get all pets with pagination")
    void shouldGetAllPetsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = Arrays.asList(testPet);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, 1);
        
        when(petRepository.findByIsActiveTrue(pageable)).thenReturn(petPage);

        // When
        Page<Pet> result = petService.getAllPets(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPet.getId(), result.getContent().get(0).getId());

        verify(petRepository).findByIsActiveTrue(pageable);
    }

    @Test
    @DisplayName("Should check if pet belongs to user")
    void shouldCheckIfPetBelongsToUser() {
        // Given
        when(petRepository.existsByIdAndOwnerId(testPet.getId(), testUser.getId())).thenReturn(true);

        // When
        boolean result = petService.isPetOwnedByUser(testPet.getId(), testUser.getId());

        // Then
        assertTrue(result);

        verify(petRepository).existsByIdAndOwnerId(testPet.getId(), testUser.getId());
    }

    @Test
    @DisplayName("Should return false when pet does not belong to user")
    void shouldReturnFalseWhenPetDoesNotBelongToUser() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        when(petRepository.existsByIdAndOwnerId(testPet.getId(), anotherUserId)).thenReturn(false);

        // When
        boolean result = petService.isPetOwnedByUser(testPet.getId(), anotherUserId);

        // Then
        assertFalse(result);

        verify(petRepository).existsByIdAndOwnerId(testPet.getId(), anotherUserId);
    }

    @Test
    @DisplayName("Should get pets by age range")
    void shouldGetPetsByAgeRange() {
        // Given
        Integer minAge = 2;
        Integer maxAge = 5;
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = Arrays.asList(testPet);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, 1);
        
        when(petRepository.findByAgeBetweenAndIsActiveTrue(minAge, maxAge, pageable))
                .thenReturn(petPage);

        // When
        Page<Pet> result = petService.getPetsByAgeRange(minAge, maxAge, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPet.getId(), result.getContent().get(0).getId());

        verify(petRepository).findByAgeBetweenAndIsActiveTrue(minAge, maxAge, pageable);
    }
}