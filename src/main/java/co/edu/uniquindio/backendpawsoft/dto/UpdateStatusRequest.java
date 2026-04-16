package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import lombok.Data;

/**
 * DTO para actualizar el estado de una hospitalización.
 */
@Data
public class UpdateStatusRequest {
    private HospitalizationStatus status;
}