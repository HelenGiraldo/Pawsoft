package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.PaymentAdjustmentResponse;
import co.edu.uniquindio.backendpawsoft.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para auditoría de pagos (solo administrador).
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final AuditService auditService;

    /**
     * Lista todos los ajustes de pagos ordenados por fecha descendente.
     * Solo accesible para administradores.
     * GET /api/admin/payments/adjustments
     */
    @GetMapping("/adjustments")
    public ResponseEntity<List<PaymentAdjustmentResponse>> getAllAdjustments() {
        return ResponseEntity.ok(auditService.getAllAdjustments());
    }
}
