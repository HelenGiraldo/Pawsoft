package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la creación o actualización de una mascota.
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
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@Data
public class PetRequest {
    @NotBlank private String name;
    @NotBlank private String species;
    private String breed;
    private String birthDate;
    @NotBlank private String sex;
    private String ownerEmail;
    private String photoUrl; // URL que devuelve Cloudinary
}