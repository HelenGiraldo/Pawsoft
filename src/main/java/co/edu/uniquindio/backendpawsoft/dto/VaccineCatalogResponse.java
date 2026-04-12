package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VaccineCatalogResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;
}
