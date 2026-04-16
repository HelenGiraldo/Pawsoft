package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para crear una nueva hospitalización.
 */
@Data
public class CreateHospitalizationRequest {
    private Long petId;
    private Long appointmentId;
    private String reason;
    private String initialObservations;
    private BigDecimal hourlyRate;
}