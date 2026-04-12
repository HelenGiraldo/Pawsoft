package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.CatalogService;
import co.edu.uniquindio.backendpawsoft.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de pagos y precios para el panel de administración.
 *
 * Responsabilidades del administrador:
 * - Ver todos los pagos y estadísticas financieras
 * - Revertir pagos confirmados (corrección de errores)
 * - Gestionar la tabla de precios de servicios (CRUD)
 * - Ver historial de pagos de un cliente específico
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
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final CatalogService catalogService;

    /* ════════════════════════════════════════════════════════════
       PAGOS
    ════════════════════════════════════════════════════════════ */

    /**
     * Lista todos los pagos del sistema.
     * GET /api/admin/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Estadísticas financieras: ingresos por período, por concepto, etc.
     * GET /api/admin/payments/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsResponse> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }

    /**
     * Historial de pagos de un cliente específico.
     * GET /api/admin/payments/client/{email}
     */
    @GetMapping("/client/{email}")
    public ResponseEntity<List<PaymentResponse>> getByClient(@PathVariable String email) {
        return ResponseEntity.ok(paymentService.getByClientEmail(email));
    }

    /**
     * Revierte un pago PAID a PENDING (corrección de error).
     * Solo disponible para el admin.
     * PUT /api/admin/payments/{id}/revert
     */
    @PutMapping("/{id}/revert")
    public ResponseEntity<PaymentResponse> revert(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.revertToPending(id));
    }

    /* ════════════════════════════════════════════════════════════
       PRECIOS DE SERVICIOS
    ════════════════════════════════════════════════════════════ */

    /**
     * Lista todos los precios configurados (activos e inactivos).
     * GET /api/admin/payments/prices
     */
    @GetMapping("/prices")
    public ResponseEntity<List<ServicePriceResponse>> getAllPrices() {
        return ResponseEntity.ok(paymentService.getAllPrices());
    }

    /**
     * Crea o actualiza un precio de servicio.
     * Si ya existe el serviceType, actualiza; si no, crea nuevo.
     * POST /api/admin/payments/prices
     */
    @PostMapping("/prices")
    public ResponseEntity<ServicePriceResponse> upsertPrice(
            @Valid @RequestBody ServicePriceRequest request) {
        return ResponseEntity.ok(paymentService.upsertPrice(request));
    }

    /**
     * Elimina un precio de servicio.
     * DELETE /api/admin/payments/prices/{id}
     */
    @DeleteMapping("/prices/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        paymentService.deletePrice(id);
        return ResponseEntity.noContent().build();
    }

    /* ════════════════════════════════════════════════════════════
       CATÁLOGO DE MEDICAMENTOS
    ════════════════════════════════════════════════════════════ */

    /**
     * Lista todos los medicamentos del catálogo.
     * GET /api/admin/payments/medications
     */
    @GetMapping("/medications")
    public ResponseEntity<List<MedicationCatalogResponse>> getAllMedications() {
        return ResponseEntity.ok(catalogService.getAllMedications());
    }

    /**
     * Crea o actualiza un medicamento en el catálogo.
     * POST /api/admin/payments/medications
     */
    @PostMapping("/medications")
    public ResponseEntity<MedicationCatalogResponse> upsertMedication(
            @Valid @RequestBody MedicationCatalogRequest request) {
        return ResponseEntity.ok(catalogService.createOrUpdateMedication(request));
    }

    /**
     * Elimina un medicamento del catálogo.
     * DELETE /api/admin/payments/medications/{id}
     */
    @DeleteMapping("/medications/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        catalogService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }

    /* ════════════════════════════════════════════════════════════
       CATÁLOGO DE VACUNAS
    ════════════════════════════════════════════════════════════ */

    /**
     * Lista todas las vacunas del catálogo.
     * GET /api/admin/payments/vaccines
     */
    @GetMapping("/vaccines")
    public ResponseEntity<List<VaccineCatalogResponse>> getAllVaccines() {
        return ResponseEntity.ok(catalogService.getAllVaccines());
    }

    /**
     * Crea o actualiza una vacuna en el catálogo.
     * POST /api/admin/payments/vaccines
     */
    @PostMapping("/vaccines")
    public ResponseEntity<VaccineCatalogResponse> upsertVaccine(
            @Valid @RequestBody VaccineCatalogRequest request) {
        return ResponseEntity.ok(catalogService.createOrUpdateVaccine(request));
    }

    /**
     * Elimina una vacuna del catálogo.
     * DELETE /api/admin/payments/vaccines/{id}
     */
    @DeleteMapping("/vaccines/{id}")
    public ResponseEntity<Void> deleteVaccine(@PathVariable Long id) {
        catalogService.deleteVaccine(id);
        return ResponseEntity.noContent().build();
    }
}