package co.edu.uniquindio.backendpawsoft.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo de Mascota (Pet).
 * 
 * Valida la correcta creación y manipulación de datos de mascotas,
 * incluyendo información básica y relación con el dueño.
 */
@DisplayName("Pruebas del Modelo Pet")
class PetTest {

    private Pet testPet;

    @BeforeEach
    void setUp() {
        testPet = Pet.builder()
                .id(1L)
                .name("Firulais")
                .species("Perro")
                .breed("Labrador")
                .birthDate("2020-05-15")
                .sex("Macho")
                .ownerEmail("owner@example.com")
                .photoUrl("http://example.com/firulais.jpg")
                .build();
    }

    @Test
    @DisplayName("Pet debe crearse con todos los campos correctos")
    void testCrearPet() {
        // Assert
        assertNotNull(testPet);
        assertEquals(1L, testPet.getId());
        assertEquals("Firulais", testPet.getName());
        assertEquals("Perro", testPet.getSpecies());
        assertEquals("Labrador", testPet.getBreed());
        assertEquals("Macho", testPet.getSex());
        assertEquals("owner@example.com", testPet.getOwnerEmail());
    }

    @Test
    @DisplayName("Pet debe tener fecha de nacimiento válida")
    void testFechaNacimiento() {
        // Assert
        assertNotNull(testPet.getBirthDate());
        assertEquals("2020-05-15", testPet.getBirthDate());
    }

    @Test
    @DisplayName("Pet debe tener URL de foto")
    void testPhotoUrl() {
        // Assert
        assertNotNull(testPet.getPhotoUrl());
        assertEquals("http://example.com/firulais.jpg", testPet.getPhotoUrl());
    }

    @Test
    @DisplayName("Pet debe estar asociado a un dueño por email")
    void testOwnerEmail() {
        // Assert
        assertNotNull(testPet.getOwnerEmail());
        assertEquals("owner@example.com", testPet.getOwnerEmail());
    }

    @Test
    @DisplayName("Actualizar información de Pet debe funcionar correctamente")
    void testActualizarPet() {
        // Act
        testPet.setName("Max");
        testPet.setBreed("Golden Retriever");
        testPet.setPhotoUrl("http://example.com/max.jpg");

        // Assert
        assertEquals("Max", testPet.getName());
        assertEquals("Golden Retriever", testPet.getBreed());
        assertEquals("http://example.com/max.jpg", testPet.getPhotoUrl());
    }

    @Test
    @DisplayName("Builder debe crear Pet con campos mínimos")
    void testBuilderCamposMinimos() {
        // Arrange & Act
        Pet pet = Pet.builder()
                .name("Luna")
                .species("Gato")
                .ownerEmail("owner@example.com")
                .build();

        // Assert
        assertNotNull(pet);
        assertEquals("Luna", pet.getName());
        assertEquals("Gato", pet.getSpecies());
        assertNull(pet.getBreed());
        assertNull(pet.getBirthDate());
    }

    @Test
    @DisplayName("Pet puede ser de diferentes especies")
    void testDiferentesEspecies() {
        // Arrange
        Pet perro = Pet.builder().species("Perro").build();
        Pet gato = Pet.builder().species("Gato").build();
        Pet ave = Pet.builder().species("Ave").build();
        Pet reptil = Pet.builder().species("Reptil").build();

        // Assert
        assertEquals("Perro", perro.getSpecies());
        assertEquals("Gato", gato.getSpecies());
        assertEquals("Ave", ave.getSpecies());
        assertEquals("Reptil", reptil.getSpecies());
    }

    @Test
    @DisplayName("Pet puede tener diferentes sexos")
    void testDiferentesSexos() {
        // Act
        testPet.setSex("Hembra");

        // Assert
        assertEquals("Hembra", testPet.getSex());
    }
}
