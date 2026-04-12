package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentAdjustmentResponse {
    private Long id;
    private BigDecimal originalAmount;
    private BigDecimal adjustedAmount;
    private BigDecimal difference;
    private String reason;
    private String adjustedBy;
    private String adjustedByName;
    private LocalDateTime adjustedAt;
}
