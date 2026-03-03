package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.PetRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de gestión de mascotas.
 * 
 * Valida las operaciones CRUD sobre mascotas, incluyendo:
 * - Creación de mascotas asociadas a un dueño
 * - Consulta de mascotas por dueño
 * - Actualización y eliminación con validación de permisos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Mascotas")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    private Pet testPet;
    private PetRequest petRequest;
    private String ownerEmail;

    @BeforeEach
    void setUp() {
        ownerEmail = "owner@example.com";

        testPet = Pet.builder()
                .id(1L)
                .name("Firulais")
                .species("Perro")
                .breed("Labrador")
                .birthDate("2020-05-15")
                .sex("Macho")
                .ownerEmail(ownerEmail)
                .photoUrl("http://example.com/photo.jpg")
                .build();

        petRequest = new PetRequest();
        petRequest.setName("Max");
        petRequest.setSpecies("Gato");
        petRequest.setBreed("Siamés");
        petRequest.setBirthDate("2021-03-10");
        petRequest.setSex("Macho");
        petRequest.setPhotoUrl("http://example.com/max.jpg");
    }

    @Test
    @DisplayName("Obtener mascotas por dueño debe retornar lista correcta")
    void testGetByOwner() {
        // Arrange
        Pet pet2 = Pet.builder()
                .id(2L)
                .name("Luna")
                .species("Gato")
                .ownerEmail(ownerEmail)
                .build();

        when(petRepository.findByOwnerEmail(ownerEmail)).thenReturn(Arrays.asList(testPet, pet2));

        // Act
        List<PetResponse> pets = petService.getByOwner(ownerEmail);

        // Assert
        assertNotNull(pets);
        assertEquals(2, pets.size());
        assertEquals("Firulais", pets.get(0).getName());
        assertEquals("Luna", pets.get(1).getName());
    }

    @Test
    @DisplayName("Crear mascota debe asociarla al dueño correcto")
    void testCreate() {
        // Arrange
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setId(1L);
            return pet;
        });

        // Act
        PetResponse response = petService.create(petRequest, ownerEmail);

        // Assert
        assertNotNull(response);
        assertEquals("Max", response.getName());
        assertEquals("Gato", response.getSpecies());
        assertEquals(ownerEmail, response.getOwnerEmail());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    @DisplayName("Actualizar mascota propia debe modificar datos")
    void testUpdate() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        PetResponse response = petService.update(1L, petRequest, ownerEmail);

        // Assert
        assertNotNull(response);
        assertEquals("Max", testPet.getName());
        assertEquals("Gato", testPet.getSpecies());
        verify(petRepository, times(1)).save(testPet);
    }

    @Test
    @DisplayName("Actualizar mascota de otro dueño debe lanzar excepción")
    void testUpdateSinPermiso() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> petService.update(1L, petRequest, "otro@example.com")
        );
        assertTrue(exception.getMessage().contains("permiso"));
    }

    @Test
    @DisplayName("Eliminar mascota propia debe ser exitoso")
    void testDelete() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        doNothing().when(petRepository).delete(testPet);

        // Act
        petService.delete(1L, ownerEmail);

        // Assert
        verify(petRepository, times(1)).delete(testPet);
    }

    @Test
    @DisplayName("Eliminar mascota de otro dueño debe lanzar excepción")
    void testDeleteSinPermiso() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> petService.delete(1L, "otro@example.com")
        );
        assertTrue(exception.getMessage().contains("permiso"));
        verify(petRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Actualizar mascota inexistente debe lanzar excepción")
    void testUpdateMascotaInexistente() {
        // Arrange
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> petService.update(999L, petRequest, ownerEmail));
    }
}
