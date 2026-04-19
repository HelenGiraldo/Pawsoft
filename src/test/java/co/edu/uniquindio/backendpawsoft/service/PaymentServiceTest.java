package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.model.Payment;
import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import co.edu.uniquindio.backendpawsoft.repository.PaymentAdjustmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PaymentItemRepository;
import co.edu.uniquindio.backendpawsoft.repository.PaymentRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ServicePriceRepository servicePriceRepository;
    @Mock private PaymentItemRepository paymentItemRepository;
    @Mock private PaymentAdjustmentRepository paymentAdjustmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private ServicePrice servicePrice;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(1L);
        paymentRequest.setClientName("Juan Pérez");
        paymentRequest.setClientEmail("juan@test.com");
        paymentRequest.setPetName("Firulais");
        paymentRequest.setVetName("Dr. García");
        paymentRequest.setAppointmentDate("2024-03-15");
        paymentRequest.setAppointmentTime("10:00");
        paymentRequest.setConcept("Consulta general");
        paymentRequest.setBaseAmount(new BigDecimal("50000"));
        paymentRequest.setAmount(new BigDecimal("50000"));
        paymentRequest.setNotes("Pago en efectivo");

        payment = Payment.builder()
                .id(1L)
                .appointmentId(1L)
                .clientName("Juan Pérez")
                .clientEmail("juan@test.com")
                .petName("Firulais")
                .vetName("Dr. García")
                .appointmentDate(LocalDate.parse("2024-03-15"))
                .appointmentTime(LocalTime.parse("10:00"))
                .concept("Consulta general")
                .baseAmount(new BigDecimal("50000"))
                .amount(new BigDecimal("50000"))
                .status(PaymentStatus.PENDING)
                .receivedBy("recepcionista@test.com")
                .notes("Pago en efectivo")
                .createdAt(LocalDateTime.now())
                .build();

        servicePrice = ServicePrice.builder()
                .id(1L)
                .serviceType("Consulta general")
                .displayName("Consulta General")
                .price(new BigDecimal("50000"))
                .description("Consulta veterinaria general")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePaymentSuccessfully() {
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(userRepository.findByEmail("recepcionista@test.com"))
                .thenReturn(Optional.of(co.edu.uniquindio.backendpawsoft.model.User.builder()
                        .id(10L).name("Recep Test").email("recepcionista@test.com")
                        .role(co.edu.uniquindio.backendpawsoft.enums.Role.ROLE_RECEPCIONISTA)
                        .build()));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.createPayment(paymentRequest, "recepcionista@test.com");

        assertNotNull(response);
        assertEquals("Juan Pérez", response.getClientName());
        assertEquals(PaymentStatus.PENDING.name(), response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_CREATE"), anyString(), eq("PAYMENT"), anyInt());
    }

    @Test
    @DisplayName("Should throw when appointment already has a payment")
    void shouldThrowWhenAppointmentAlreadyHasPayment() {
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> paymentService.createPayment(paymentRequest, "recepcionista@test.com"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark payment as paid")
    void shouldMarkPaymentAsPaid() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.markAsPaid(1L);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_CONFIRMED"), anyString(), eq("PAYMENT"), eq(1));
    }

    @Test
    @DisplayName("Should throw when marking already paid payment")
    void shouldThrowWhenMarkingAlreadyPaidPayment() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> paymentService.markAsPaid(1L));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should revert payment to pending")
    void shouldRevertPaymentToPending() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.revertToPending(1L);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_REVERTED"), anyString(), eq("PAYMENT"), eq(1));
    }

    @Test
    @DisplayName("Should get all payments")
    void shouldGetAllPayments() {
        when(paymentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(payment));

        List<PaymentResponse> responses = paymentService.getAllPayments();

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Should get payment by appointment ID")
    void shouldGetPaymentByAppointmentId() {
        when(paymentRepository.findByAppointmentId(1L)).thenReturn(Optional.of(payment));

        Optional<PaymentResponse> response = paymentService.getByAppointmentId(1L);

        assertTrue(response.isPresent());
        assertEquals("Juan Pérez", response.get().getClientName());
    }

    @Test
    @DisplayName("Should get payments by client email")
    void shouldGetPaymentsByClientEmail() {
        when(paymentRepository.findByClientEmailOrderByCreatedAtDesc("juan@test.com"))
                .thenReturn(List.of(payment));

        List<PaymentResponse> responses = paymentService.getByClientEmail("juan@test.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Should get active prices")
    void shouldGetActivePrices() {
        when(servicePriceRepository.findByActiveTrueOrderByDisplayNameAsc())
                .thenReturn(List.of(servicePrice));

        List<ServicePriceResponse> responses = paymentService.getActivePrices();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Consulta general", responses.get(0).getServiceType());
    }

    @Test
    @DisplayName("Should upsert service price")
    void shouldUpsertServicePrice() {
        ServicePriceRequest req = new ServicePriceRequest();
        req.setServiceType("Consulta general");
        req.setDisplayName("Consulta General");
        req.setPrice(new BigDecimal("50000"));
        req.setDescription("Consulta veterinaria");
        req.setActive(true);

        when(servicePriceRepository.findByServiceType("Consulta general")).thenReturn(Optional.empty());
        when(servicePriceRepository.save(any(ServicePrice.class))).thenReturn(servicePrice);

        ServicePriceResponse response = paymentService.upsertPrice(req);

        assertNotNull(response);
        assertEquals("Consulta general", response.getServiceType());
        verify(auditLogService).log(eq("PRICE_UPSERT"), anyString(), eq("SERVICE_PRICE"), anyInt());
    }

    @Test
    @DisplayName("Should delete service price")
    void shouldDeleteServicePrice() {
        when(servicePriceRepository.existsById(1L)).thenReturn(true);

        paymentService.deletePrice(1L);

        verify(servicePriceRepository).deleteById(1L);
        verify(auditLogService).log(eq("PRICE_DELETE"), anyString(), eq("SERVICE_PRICE"), eq(1));
    }

    @Test
    @DisplayName("Should throw when deleting non-existent price")
    void shouldThrowWhenDeletingNonExistentPrice() {
        when(servicePriceRepository.existsById(1L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> paymentService.deletePrice(1L));
        verify(servicePriceRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get base price for service type")
    void shouldGetBasePriceForServiceType() {
        when(servicePriceRepository.findByServiceType("Consulta general"))
                .thenReturn(Optional.of(servicePrice));

        BigDecimal price = paymentService.getBasePrice("Consulta general");

        assertEquals(new BigDecimal("50000"), price);
    }

    @Test
    @DisplayName("Should return zero when service has no price configured")
    void shouldReturnZeroWhenServiceHasNoPrice() {
        when(servicePriceRepository.findByServiceType("Unknown")).thenReturn(Optional.empty());

        BigDecimal price = paymentService.getBasePrice("Unknown");

        assertEquals(BigDecimal.ZERO, price);
    }
}
