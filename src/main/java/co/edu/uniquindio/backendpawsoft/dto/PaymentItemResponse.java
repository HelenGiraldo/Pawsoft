package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentItemResponse {
    private Long id;
    private String itemType;
    private String itemName;
    private String description;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
