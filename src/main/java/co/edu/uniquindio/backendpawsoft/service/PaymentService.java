package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.model.Payment;
import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import co.edu.uniquindio.backendpawsoft.repository.PaymentRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
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
 * Servicio que centraliza toda la lógica de negocio de pagos.
 *
 * Responsabilidades:
 * - CRUD de pagos (crear, confirmar cobro, revertir)
 * - CRUD de precios de servicios (solo admin)
 * - Estadísticas de ingresos para el panel de admin
 * - Historial de pagos por cliente
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository      paymentRepository;
    private final ServicePriceRepository servicePriceRepository;

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

        Payment payment = Payment.builder()
                .appointmentId  (req.getAppointmentId())
                .clientName     (req.getClientName())
                .clientEmail    (req.getClientEmail())
                .petName        (req.getPetName())
                .vetName        (req.getVetName())
                .appointmentDate(LocalDate.parse(req.getAppointmentDate()))
                .appointmentTime(LocalTime.parse(req.getAppointmentTime()))
                .concept        (req.getConcept())
                .baseAmount     (req.getBaseAmount())
                .amount         (req.getAmount())
                .status         (PaymentStatus.PENDING)
                .receivedBy     (receivedBy)
                .notes          (req.getNotes())
                .createdAt      (LocalDateTime.now())
                .build();

        return toResponse(paymentRepository.save(payment));
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

        return toResponse(paymentRepository.save(payment));
    }

    /**
     * Revierte un pago a PENDING (operación de admin para corregir errores).
     */
    @Transactional
    public PaymentResponse revertToPending(Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(null);
        return toResponse(paymentRepository.save(payment));
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

        return toPriceResponse(servicePriceRepository.save(sp));
    }

    @Transactional
    public void deletePrice(Long id) {
        if (!servicePriceRepository.existsById(id))
            throw new NoSuchElementException("Precio de servicio no encontrado: " + id);
        servicePriceRepository.deleteById(id);
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