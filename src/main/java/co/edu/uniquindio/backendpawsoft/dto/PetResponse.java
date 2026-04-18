package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta con la información de una mascota.
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
@Data @Builder
public class PetResponse {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birthDate;
    private String sex;
    private String ownerEmail;
    private String photoUrl;
    private Boolean isDeceased;      // Indica si la mascota está fallecida
    private Boolean isHospitalized;  // Indica si la mascota está hospitalizada
}