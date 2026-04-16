package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para capturar información médica inicial al registrar una mascota.
 * Todos los campos son opcionales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMedicalProfileInitialRequest {

    /**
     * Tipo de sangre (si el dueño lo conoce).
     * Ejemplo: "DEA 1.1", "DEA 1.2", "DEA 7"
     */
    @Size(max = 10, message = "Tipo de sangre no puede exceder 10 caracteres")
    private String bloodType;

    /**
     * Alergias conocidas (si el dueño sabe).
     * Ejemplo: "Amoxicilina, Polen"
     */
    @Size(max = 1000, message = "Alergias no pueden exceder 1000 caracteres")
    private String knownAllergies;

    /**
     * Condiciones crónicas preexistentes.
     * Ejemplo: "Displasia de cadera, Diabetes"
     */
    @Size(max = 1000, message = "Condiciones no pueden exceder 1000 caracteres")
    private String chronicConditions;

    /**
     * Medicamentos que ya estaba tomando.
     * Ejemplo: "Meloxicam 10mg, Insulina"
     */
    @Size(max = 1000, message = "Medicamentos no pueden exceder 1000 caracteres")
    private String currentMedications;

    /**
     * Notas adicionales (viene de otra clínica, etc).
     */
    @Size(max = 500, message = "Notas no pueden exceder 500 caracteres")
    private String additionalNotes;
}
