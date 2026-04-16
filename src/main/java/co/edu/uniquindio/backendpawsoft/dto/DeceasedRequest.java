package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO para registrar el fallecimiento de una mascota.
 */
@Data
public class DeceasedRequest {
    private String causeOfDeath;
}