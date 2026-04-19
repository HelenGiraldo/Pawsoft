package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la actualización de información de mascotas.
 * 
 * Contiene los campos que pueden ser modificados en el perfil de una mascota.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
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
public class UpdatePetRequest {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    private String species;

    private String breed;

    private LocalDate birthDate;

    private String gender;

    @Positive(message = "El peso debe ser un valor positivo")
    private Double weight;

    private String color;

    private String microchipNumber;

    private String specialNeeds;

    private String allergies;

    private Boolean isNeutered;
}