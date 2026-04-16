package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.PaymentAdjustmentResponse;
import co.edu.uniquindio.backendpawsoft.repository.PaymentAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {
    
    private final PaymentAdjustmentRepository paymentAdjustmentRepository;
    
    /**
     * Obtiene todos los ajustes de pagos ordenados por fecha descendente
     */
    public List<PaymentAdjustmentResponse> getAllAdjustments() {
        return paymentAdjustmentRepository.findAllByOrderByAdjustedAtDesc()
                .stream()
                .map(adjustment -> PaymentAdjustmentResponse.builder()
                        .id(adjustment.getId())
                        .originalAmount(adjustment.getOriginalAmount())
                        .adjustedAmount(adjustment.getAdjustedAmount())
                        .difference(adjustment.getDifference())
                        .reason(adjustment.getReason())
                        .adjustedBy(adjustment.getAdjustedBy())
                        .adjustedByName(adjustment.getAdjustedByName())
                        .adjustedAt(adjustment.getAdjustedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
