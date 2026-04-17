package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para hospitalizaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalizationDTO {

    private Long id;

    private Long petId;

    private String petName;

    private String petSpecies;

    private String ownerEmail;

    private Long vetId;

    private String vetName;

    private Long appointmentId;

    private HospitalizationStatus status;

    private LocalDateTime admissionDate;

    private LocalDateTime dischargeDate;

    private String reason;

    private String initialObservations;

    private BigDecimal hourlyRate;

    private String causeOfDeath;

    private LocalDateTime createdAt;

    // Campos calculados
    private Long totalHours;

    private BigDecimal totalCost;

    // Relaciones
    private List<HospitalizationNoteDTO> notes;

    private List<MedicalAttachmentDTO> attachments;
}
