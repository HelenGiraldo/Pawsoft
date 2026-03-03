package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para el listado de mascotas en el panel de administración.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@Data
@Builder
public class AdminPetResponse {
    private Long   id;
    private String name;
    private String species;
    private String breed;
    private String sex;
    private String birthDate;
    private String photoUrl;
    private String ownerName;
    private String ownerEmail;
}