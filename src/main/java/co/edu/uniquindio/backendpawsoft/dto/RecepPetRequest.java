package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para crear o actualizar una mascota desde el panel de recepcionista.
 *
 * El campo birthDate se maneja como String (formato "yyyy-MM-dd") para coincidir
 * con el campo String en la entidad Pet.
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
public class RecepPetRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La especie es obligatoria")
    private String species;

    private String breed;

    @NotBlank(message = "El sexo es obligatorio")
    private String sex;

    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    private String birthDate;   // ← String, igual que Pet.birthDate

    private String photoUrl;

    @NotBlank(message = "El email del propietario es obligatorio")
    private String ownerEmail;
}