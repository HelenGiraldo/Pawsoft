package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.CatalogService;
import co.edu.uniquindio.backendpawsoft.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de pagos para el rol de recepcionista.
 *
 * Flujo principal:
 * 1. GET /prices  → obtiene lista de servicios con precio base
 * 2. GET /price?serviceType=X → precio base de un servicio concreto
 * 3. POST /       → registra el pago de una cita (PENDING)
 * 4. PUT /{id}/pay → confirma el cobro en efectivo (PENDING → PAID)
 * 5. GET /appointment/{appointmentId} → consulta el pago de una cita
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
@RequestMapping("/api/recepcionista/payments")
@RequiredArgsConstructor
public class RecepcionistaPaymentController {

    private final PaymentService paymentService;
    private final CatalogService catalogService;

    /**
     * Lista los servicios activos con su precio base.
     * La recepcionista lo usa para autocompletar el concepto y monto al crear un pago.
     * GET /api/recepcionista/payments/prices
     */
    @GetMapping("/prices")
    public ResponseEntity<List<ServicePriceResponse>> getActivePrices() {
        return ResponseEntity.ok(paymentService.getActivePrices());
    }

    /**
     * Precio base de un servicio específico.
     * GET /api/recepcionista/payments/price?serviceType=Consulta general
     */
    @GetMapping("/price")
    public ResponseEntity<ServicePriceResponse> getPriceByService(
            @RequestParam String serviceType) {
        return paymentService.getActivePrices().stream()
                .filter(p -> p.getServiceType().equalsIgnoreCase(serviceType))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Registra un pago con estado PENDING.
     * El email de quien registra se toma del JWT automáticamente.
     * POST /api/recepcionista/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        String receivedBy = currentUser != null ? currentUser.getUsername() : "sistema";
        return ResponseEntity.ok(paymentService.createPayment(request, receivedBy));
    }

    /**
     * Confirma el cobro en efectivo → cambia PENDING a PAID.
     * PUT /api/recepcionista/payments/{id}/pay
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.markAsPaid(id));
    }

    /**
     * Consulta el pago asociado a una cita específica.
     * Útil para que la recepcionista sepa si una cita ya fue cobrada.
     * GET /api/recepcionista/payments/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PaymentResponse> getByAppointment(
            @PathVariable Long appointmentId) {
        return paymentService.getByAppointmentId(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los pagos (pendientes y pagados).
     * GET /api/recepcionista/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Ajusta el monto de un pago con auditoría.
     * PUT /api/recepcionista/payments/{id}/adjust
     */
    @PutMapping("/{id}/adjust")
    public ResponseEntity<PaymentResponse> adjustPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentAdjustmentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        String adjustedBy = currentUser != null ? currentUser.getUsername() : "sistema";
        return ResponseEntity.ok(paymentService.adjustPaymentAmount(id, request, adjustedBy));
    }

    /**
     * Lista medicamentos activos del catálogo.
     * GET /api/recepcionista/payments/medications
     */
    @GetMapping("/medications")
    public ResponseEntity<List<MedicationCatalogResponse>> getActiveMedications() {
        return ResponseEntity.ok(catalogService.getActiveMedications());
    }

    /**
     * Lista vacunas activas del catálogo.
     * GET /api/recepcionista/payments/vaccines
     */
    @GetMapping("/vaccines")
    public ResponseEntity<List<VaccineCatalogResponse>> getActiveVaccines() {
        return ResponseEntity.ok(catalogService.getActiveVaccines());
    }
}