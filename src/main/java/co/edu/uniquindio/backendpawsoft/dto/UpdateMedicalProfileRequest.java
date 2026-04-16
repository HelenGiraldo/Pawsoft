package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO para actualizar el perfil médico después de una consulta.
 */
@Data
public class UpdateMedicalProfileRequest {
    private String allergiesFound;
    private String conditionsFound;
    private String surgicalNote;
    private String currentMeds;
    private String bloodType;
}