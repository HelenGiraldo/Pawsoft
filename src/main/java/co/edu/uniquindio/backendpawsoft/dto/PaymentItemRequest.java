package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentItemRequest {

    @NotBlank(message = "El tipo de ítem es obligatorio")
    private String itemType; // "SERVICE", "MEDICATION", "VACCINE"

    @NotBlank(message = "El nombre del ítem es obligatorio")
    private String itemName;

    private String description;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    private String unit;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal unitPrice;
}
