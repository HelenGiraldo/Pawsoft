package co.edu.uniquindio.backendpawsoft.dto;

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