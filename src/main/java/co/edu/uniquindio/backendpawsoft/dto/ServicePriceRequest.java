package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicePriceRequest {

    @NotBlank(message = "El tipo de servicio es obligatorio")
    private String serviceType;

    @NotBlank(message = "El nombre a mostrar es obligatorio")
    private String displayName;

    @NotNull
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal price;

    private String description;

    private boolean active = true;
}