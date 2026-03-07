package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.PaymentResponse;
import co.edu.uniquindio.backendpawsoft.dto.ServicePriceResponse;
import co.edu.uniquindio.backendpawsoft.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de pagos para el cliente — Pawsoft.
 *
 * Expone únicamente las operaciones de solo lectura que un cliente
 * necesita en su dashboard:
 *
 *   GET /api/cliente/payments/my      → historial de pagos propios
 *   GET /api/cliente/payments/prices  → precios de servicios (referencial)
 *
 * El email del cliente se extrae del JWT (Authentication.getName()) —
 * no se acepta como parámetro para evitar que un cliente consulte
 * los pagos de otro usuario.
 *
 * Proyecto: Pawsoft — Universidad del Quindío — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 */
@RestController
@RequestMapping("/api/cliente/payments")
@RequiredArgsConstructor
public class ClientePaymentController {

    private final PaymentService paymentService;

    /**
     * Devuelve el historial de pagos del cliente autenticado.
     *
     * El email se extrae del JWT para garantizar que cada cliente
     * solo pueda ver sus propios pagos.
     *
     * @param authentication contexto de seguridad inyectado por Spring Security
     * @return lista de pagos del cliente (puede estar vacía)
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        String email = authentication.getName();
        List<PaymentResponse> payments = paymentService.getByClientEmail(email);
        return ResponseEntity.ok(payments);
    }

    /**
     * Devuelve la lista de servicios activos con su precio base.
     *
     * Se usa en el paso 5 del flujo de agendamiento para mostrar
     * el precio referencial antes de confirmar la cita.
     * Es solo lectura — no expone lógica financiera ni de admin.
     *
     * @return lista de servicios activos con precio
     */
    @GetMapping("/prices")
    public ResponseEntity<List<ServicePriceResponse>> getActivePrices() {
        List<ServicePriceResponse> prices = paymentService.getActivePrices();
        return ResponseEntity.ok(prices);
    }
}