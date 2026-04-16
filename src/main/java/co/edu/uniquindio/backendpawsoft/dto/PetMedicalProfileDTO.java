package co.edu.uniquindio.backendpawsoft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la hoja médica maestra de una mascota.
 * Se devuelve en el modal previo a iniciar atención.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetMedicalProfileDTO {

    private Long id;

    private Long petId;

    private String petName;

    private String petSpecies;

    private String petBreed;

    private Integer petAge;

    private String knownAllergies;

    private String chronicConditions;

    private String surgicalHistory;

    private String currentMedications;

    private String bloodType;

    private String lastUpdatedByName;

    private String lastUpdatedByRole;

    private LocalDateTime lastUpdatedAt;
}
