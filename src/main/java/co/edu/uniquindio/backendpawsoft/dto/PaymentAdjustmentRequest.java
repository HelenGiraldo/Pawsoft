package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAdjustmentRequest {
    
    @NotNull(message = "El monto ajustado es obligatorio")
    @Positive(message = "El monto ajustado debe ser positivo")
    private BigDecimal adjustedAmount;
    
    @NotBlank(message = "El motivo del ajuste es obligatorio")
    @Size(min = 10, message = "El motivo debe tener al menos 10 caracteres")
    private String reason;
}
