package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentAdjustmentRequest {

    @NotNull(message = "El monto ajustado es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    private BigDecimal adjustedAmount;

    @NotBlank(message = "El motivo del ajuste es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String reason;
}
