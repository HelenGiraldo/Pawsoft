package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.PaymentAdjustmentRequest;
import co.edu.uniquindio.backendpawsoft.dto.PaymentAdjustmentResponse;
import co.edu.uniquindio.backendpawsoft.dto.PaymentResponse;
import co.edu.uniquindio.backendpawsoft.dto.PaymentStatsResponse;
import co.edu.uniquindio.backendpawsoft.service.AuditService;
import co.edu.uniquindio.backendpawsoft.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pagos desde el panel de administrador.
 *
 * Expone:
 * - Listado completo de pagos
 * - Estadísticas financieras
 * - Ajuste de monto con auditoría
 * - Reversión de pagos
 * - Historial de ajustes (auditoría)
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final AuditService   auditService;

    /**
     * GET /api/admin/payments
     * Lista todos los pagos del sistema, más recientes primero.
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * GET /api/admin/payments/stats
     * Estadísticas financieras completas para el dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsResponse> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }

    /**
     * PUT /api/admin/payments/{id}/adjust
     * Ajusta el monto de un pago con registro de auditoría.
     */
    @PutMapping("/{id}/adjust")
    public ResponseEntity<PaymentResponse> adjustPayment(
            @PathVariable Long id,
            @RequestBody PaymentAdjustmentRequest req,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        String adjustedBy = currentUser != null ? currentUser.getUsername() : "admin";
        return ResponseEntity.ok(paymentService.adjustPaymentAmount(id, req, adjustedBy));
    }

    /**
     * PUT /api/admin/payments/{id}/revert
     * Revierte un pago PAID → PENDING.
     */
    @PutMapping("/{id}/revert")
    public ResponseEntity<PaymentResponse> revertToPending(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.revertToPending(id));
    }

    /**
     * GET /api/admin/payments/adjustments
     * Historial de todos los ajustes de pagos (auditoría).
     */
    @GetMapping("/adjustments")
    public ResponseEntity<List<PaymentAdjustmentResponse>> getAllAdjustments() {
        return ResponseEntity.ok(auditService.getAllAdjustments());
    }

    /**
     * GET /api/admin/payments/client/{email}
     * Historial de pagos de un cliente específico.
     */
    @GetMapping("/client/{email}")
    public ResponseEntity<List<PaymentResponse>> getByClient(@PathVariable String email) {
        return ResponseEntity.ok(paymentService.getByClientEmail(email));
    }
}
