package co.edu.uniquindio.backendpawsoft.dto;

/**
 * DTO de respuesta con los datos de un servicio veterinario y su precio.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServicePriceResponse {
    private Long       id;
    private String     serviceType;
    private String     displayName;
    private BigDecimal price;
    private String     description;
    private boolean    active;
}