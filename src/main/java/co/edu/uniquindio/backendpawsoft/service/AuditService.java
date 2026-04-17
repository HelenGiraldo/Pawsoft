package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.PaymentAdjustmentResponse;
import co.edu.uniquindio.backendpawsoft.repository.PaymentAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar auditoría de ajustes de pagos.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {
    
    private final PaymentAdjustmentRepository paymentAdjustmentRepository;
    
    /**
     * Obtiene todos los ajustes de pagos realizados ordenados por fecha descendente.
     * 
     * @return Lista de ajustes de pagos con información de quién los realizó y cuándo
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
