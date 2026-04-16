package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemRequest {
    
    @NotBlank(message = "El tipo de ítem es obligatorio")
    private String itemType; // "SERVICE", "MEDICATION", "VACCINE"
    
    @NotBlank(message = "El nombre del ítem es obligatorio")
    private String itemName;
    
    private String description;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal quantity;
    
    private String unit;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal unitPrice;
}
