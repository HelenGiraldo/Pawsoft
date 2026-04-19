package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la creación de mascotas.
 * 
 * Contiene toda la información necesaria para registrar una nueva mascota
 * en el sistema.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePetRequest {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @NotBlank(message = "La especie es obligatoria")
    private String species;

    @NotBlank(message = "La raza es obligatoria")
    private String breed;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate birthDate;

    @NotBlank(message = "El género es obligatorio")
    private String gender;

    @Positive(message = "El peso debe ser un valor positivo")
    private Double weight;

    private String color;

    private String microchipNumber;

    private String specialNeeds;

    private String allergies;

    private Boolean isNeutered;

    @NotNull(message = "El ID del propietario es obligatorio")
    private Long ownerId;
}