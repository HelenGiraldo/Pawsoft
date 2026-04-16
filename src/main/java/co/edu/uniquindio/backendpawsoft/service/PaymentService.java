package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.model.Payment;
import co.edu.uniquindio.backendpawsoft.model.PaymentAdjustment;
import co.edu.uniquindio.backendpawsoft.model.PaymentItem;
import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import co.edu.uniquindio.backendpawsoft.repository.PaymentAdjustmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PaymentItemRepository;
import co.edu.uniquindio.backendpawsoft.repository.PaymentRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que centraliza toda la lógica de negocio relacionada con pagos.
 *
 * Responsabilidades:
 * - CRUD de pagos (crear, confirmar cobro, revertir)
 * - CRUD de precios de servicios (solo administrador)
 * - Estadísticas de ingresos para el panel de administrador
 * - Historial de pagos por cliente
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
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository      paymentRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final PaymentItemRepository  paymentItemRepository;
    private final PaymentAdjustmentRepository paymentAdjustmentRepository;
    private final UserRepository         userRepository;
    private final AuditLogService        auditLogService;

    /* ════════════════════════════════════════════════════════════
       PAGOS
    ════════════════════════════════════════════════════════════ */

    /**
     * Registra un nuevo pago en estado PENDING.
     * Solo se permite un pago por cita (unicidad garantizada).
     *
     * @param req        datos enviados por la recepcionista
     * @param receivedBy email de la recepcionista (del JWT)
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest req, String receivedBy) {

        if (paymentRepository.existsByAppointmentId(req.getAppointmentId())) {
            throw new IllegalStateException(
                    "La cita #" + req.getAppointmentId() + " ya tiene un pago registrado");
        }

        LocalDate appointmentDate;
        LocalTime appointmentTime;
        try {
            appointmentDate = LocalDate.parse(req.getAppointmentDate());
            appointmentTime = LocalTime.parse(req.getAppointmentTime());
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Formato de fecha u hora inválido. Use yyyy-MM-dd y HH:mm");
        }

        Payment payment = Payment.builder()
                .appointmentId  (req.getAppointmentId())
                .clientName     (req.getClientName())
                .clientEmail    (req.getClientEmail())
                .petName        (req.getPetName())
                .vetName        (req.getVetName())
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .concept        (req.getConcept())
                .baseAmount     (req.getBaseAmount())
                .amount         (req.getAmount())
                .status         (PaymentStatus.PENDING)
                .receivedBy     (receivedBy)
                .notes          (req.getNotes())
                .createdAt      (LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Guardar ítems si existen y recalcular amount automáticamente
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PaymentItemRequest itemReq : req.getItems()) {
                BigDecimal subtotal = itemReq.getQuantity().multiply(itemReq.getUnitPrice());
                PaymentItem item = PaymentItem.builder()
                        .payment(saved)
                        .itemType(itemReq.getItemType())
                        .itemName(itemReq.getItemName())
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unit(itemReq.getUnit())
                        .unitPrice(itemReq.getUnitPrice())
                        .subtotal(subtotal)
                        .build();
                saved.getItems().add(item);
                totalAmount = totalAmount.add(subtotal);
            }
            // Actualizar el amount con la suma de todos los subtotales
            saved.setAmount(totalAmount);
            saved = paymentRepository.save(saved);
        }

        auditLogService.log("PAYMENT_CREATE", "Pago registrado para cita #" + req.getAppointmentId(), "PAYMENT", saved.getId().intValue());

        return toResponse(saved);
    }

    /**
     * Confirma el cobro en efectivo → PENDING → PAID.
     * Registra la fecha/hora exacta del cobro.
     */
    @Transactional
    public PaymentResponse markAsPaid(Long paymentId) {
        Payment payment = findOrThrow(paymentId);

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("El pago #" + paymentId + " ya estaba confirmado");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());

        PaymentResponse response = toResponse(paymentRepository.save(payment));

        auditLogService.log("PAYMENT_CONFIRMED", "Pago confirmado como pagado", "PAYMENT", paymentId.intValue());

        return response;
    }

    /**
     * Revierte un pago a PENDING (operación de admin para corregir errores).
     */
    @Transactional
    public PaymentResponse revertToPending(Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(null);

        PaymentResponse response = toResponse(paymentRepository.save(payment));

        auditLogService.log("PAYMENT_REVERTED", "Pago revertido a pendiente", "PAYMENT", paymentId.intValue());

        return response;
    }

    /**
     * Ajusta el monto de un pago con auditoría completa.
     * Solo disponible para recepcionistas.
     *
     * @param paymentId ID del pago a ajustar
     * @param req datos del ajuste (nuevo monto y motivo)
     * @param adjustedBy email de la recepcionista
     */
    @Transactional
    public PaymentResponse adjustPaymentAmount(Long paymentId, PaymentAdjustmentRequest req, String adjustedBy) {
        Payment payment = findOrThrow(paymentId);

        // Obtener nombre de la recepcionista
        co.edu.uniquindio.backendpawsoft.model.User receptionist = userRepository.findByEmail(adjustedBy)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + adjustedBy));

        BigDecimal originalAmount = payment.getAmount();
        BigDecimal difference = req.getAdjustedAmount().subtract(originalAmount);

        // Crear registro de ajuste
        PaymentAdjustment adjustment = PaymentAdjustment.builder()
                .payment(payment)
                .originalAmount(originalAmount)
                .adjustedAmount(req.getAdjustedAmount())
                .difference(difference)
                .reason(req.getReason())
                .adjustedBy(adjustedBy)
                .adjustedByName(receptionist.getName())
                .adjustedAt(LocalDateTime.now())
                .build();

        payment.getAdjustments().add(adjustment);
        payment.setAmount(req.getAdjustedAmount());

        Payment saved = paymentRepository.save(payment);

        auditLogService.log("PAYMENT_ADJUSTED", 
                "Monto ajustado de " + originalAmount + " a " + req.getAdjustedAmount() + 
                ". Motivo: " + req.getReason(), 
                "PAYMENT", paymentId.intValue());

        return toResponse(saved);
    }

    /** Lista todos los pagos, más recientes primero. */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    /** Devuelve el pago asociado a una cita, o vacío si no existe. */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getByAppointmentId(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .map(this::toResponse);
    }

    /** Historial de pagos de un cliente específico (para el cliente). */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByClientEmail(String clientEmail) {
        return paymentRepository.findByClientEmailOrderByCreatedAtDesc(clientEmail)
                .stream().map(this::toResponse).toList();
    }

    /* ════════════════════════════════════════════════════════════
       PRECIOS DE SERVICIOS
    ════════════════════════════════════════════════════════════ */

    /** Solo los servicios activos (recepcionista al crear un pago). */
    @Transactional(readOnly = true)
    public List<ServicePriceResponse> getActivePrices() {
        return servicePriceRepository.findByActiveTrueOrderByDisplayNameAsc()
                .stream().map(this::toPriceResponse).toList();
    }

    /** Todos los servicios con su estado activo/inactivo (admin). */
    @Transactional(readOnly = true)
    public List<ServicePriceResponse> getAllPrices() {
        return servicePriceRepository.findAll(
                        org.springframework.data.domain.Sort.by("displayName"))
                .stream().map(this::toPriceResponse).toList();
    }

    /**
     * Crea o actualiza un precio.
     * Si ya existe el serviceType, actualiza; si no, crea nuevo.
     */
    @Transactional
    public ServicePriceResponse upsertPrice(ServicePriceRequest req) {
        ServicePrice sp = servicePriceRepository
                .findByServiceType(req.getServiceType())
                .orElseGet(ServicePrice::new);

        sp.setServiceType(req.getServiceType());
        sp.setDisplayName(req.getDisplayName());
        sp.setPrice(req.getPrice());
        sp.setDescription(req.getDescription());
        sp.setActive(req.isActive());

        ServicePriceResponse response = toPriceResponse(servicePriceRepository.save(sp));

        auditLogService.log("PRICE_UPSERT", "Precio de servicio creado/actualizado: " + req.getServiceType(), "SERVICE_PRICE", response.getId().intValue());

        return response;
    }

    @Transactional
    public void deletePrice(Long id) {
        if (!servicePriceRepository.existsById(id))
            throw new NoSuchElementException("Precio de servicio no encontrado: " + id);
        servicePriceRepository.deleteById(id);

        auditLogService.log("PRICE_DELETE", "Precio de servicio eliminado", "SERVICE_PRICE", id.intValue());
    }

    /**
     * Precio base para un tipo de servicio dado.
     * Devuelve BigDecimal.ZERO si el servicio no tiene precio configurado.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBasePrice(String serviceType) {
        return servicePriceRepository.findByServiceType(serviceType)
                .map(ServicePrice::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    /* ════════════════════════════════════════════════════════════
       ESTADÍSTICAS — ADMIN
    ════════════════════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public PaymentStatsResponse getStats() {

        LocalDate today   = LocalDate.now();
        LocalDate monday  = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday  = monday.plusDays(6);
        LocalDate firstOM = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastOM  = today.with(TemporalAdjusters.lastDayOfMonth());

        // Ingresos por concepto este mes (mapa ordenado por monto desc)
        List<Object[]> rawConcepts = paymentRepository.revenueByConceptBetween(firstOM, lastOM);
        Map<String, BigDecimal> byConceptMonth = new LinkedHashMap<>();
        for (Object[] row : rawConcepts) {
            byConceptMonth.put((String) row[0], (BigDecimal) row[1]);
        }

        // Últimos 10 pagos confirmados
        List<PaymentResponse> recent = paymentRepository
                .findByStatusOrderByCreatedAtDesc(PaymentStatus.PAID)
                .stream()
                .sorted(Comparator.comparing(
                        p -> p.getPaymentDate() != null ? p.getPaymentDate() : LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .limit(10)
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PaymentStatsResponse.builder()
                .totalPaid                (paymentRepository.countByStatus(PaymentStatus.PAID))
                .totalPending             (paymentRepository.countByStatus(PaymentStatus.PENDING))
                .revenueToday             (paymentRepository.sumPaidByDate(today))
                .revenueThisWeek          (paymentRepository.sumPaidBetween(monday, sunday))
                .revenueThisMonth         (paymentRepository.sumPaidBetween(firstOM, lastOM))
                .revenueAllTime           (paymentRepository.sumAllPaid())
                .revenueByConceptThisMonth(byConceptMonth)
                .recentPayments           (recent)
                .build();
    }

    /* ════════════════════════════════════════════════════════════
       HELPERS PRIVADOS
    ════════════════════════════════════════════════════════════ */

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado: " + id));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id             (p.getId())
                .appointmentId  (p.getAppointmentId())
                .clientName     (p.getClientName())
                .clientEmail    (p.getClientEmail())
                .petName        (p.getPetName())
                .vetName        (p.getVetName())
                .appointmentDate(p.getAppointmentDate())
                .appointmentTime(p.getAppointmentTime())
                .concept        (p.getConcept())
                .baseAmount     (p.getBaseAmount())
                .amount         (p.getAmount())
                .status         (p.getStatus().name())
                .paymentDate    (p.getPaymentDate())
                .receivedBy     (p.getReceivedBy())
                .notes          (p.getNotes())
                .createdAt      (p.getCreatedAt())
                .items          (p.getItems().stream().map(this::toItemResponse).toList())
                .adjustments    (p.getAdjustments().stream().map(this::toAdjustmentResponse).toList())
                .build();
    }

    private PaymentItemResponse toItemResponse(PaymentItem item) {
        return PaymentItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .itemName(item.getItemName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PaymentAdjustmentResponse toAdjustmentResponse(PaymentAdjustment adj) {
        return PaymentAdjustmentResponse.builder()
                .id(adj.getId())
                .originalAmount(adj.getOriginalAmount())
                .adjustedAmount(adj.getAdjustedAmount())
                .difference(adj.getDifference())
                .reason(adj.getReason())
                .adjustedBy(adj.getAdjustedBy())
                .adjustedByName(adj.getAdjustedByName())
                .adjustedAt(adj.getAdjustedAt())
                .build();
    }

    private ServicePriceResponse toPriceResponse(ServicePrice sp) {
        return ServicePriceResponse.builder()
                .id         (sp.getId())
                .serviceType(sp.getServiceType())
                .displayName(sp.getDisplayName())
                .price      (sp.getPrice())
                .description(sp.getDescription())
                .active     (sp.isActive())
                .build();
    }
}