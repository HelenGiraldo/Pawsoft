package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.model.Payment;
import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import co.edu.uniquindio.backendpawsoft.repository.PaymentRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PaymentService.
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
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Pagos")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ServicePriceRepository servicePriceRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private ServicePrice servicePrice;

    @BeforeEach
    void setUp() {
        // Arrange - Configurar datos de prueba
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
    @DisplayName("Debería crear un pago exitosamente")
    void deberiaCrearPagoExitosamente() {
        // Arrange
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.createPayment(paymentRequest, "recepcionista@test.com");

        // Assert
        assertNotNull(response);
        assertEquals("Juan Pérez", response.getClientName());
        assertEquals(PaymentStatus.PENDING.name(), response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_CREATE"), anyString(), eq("PAYMENT"), anyInt());
    }

    @Test
    @DisplayName("No debería crear pago si la cita ya tiene uno registrado")
    void noDeberiaCrearPagoSiCitaYaTienePago() {
        // Arrange
        when(paymentRepository.existsByAppointmentId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                paymentService.createPayment(paymentRequest, "recepcionista@test.com")
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Debería marcar pago como pagado")
    void deberiMarcarPagoComoPagado() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.markAsPaid(1L);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_CONFIRMED"), anyString(), eq("PAYMENT"), eq(1));
    }

    @Test
    @DisplayName("No debería marcar como pagado un pago que ya está pagado")
    void noDeberiaMarcarComoPagadoUnPagoYaPagado() {
        // Arrange
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                paymentService.markAsPaid(1L)
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Debería revertir pago a pendiente")
    void deberiaRevertirPagoAPendiente() {
        // Arrange
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.revertToPending(1L);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).log(eq("PAYMENT_REVERTED"), anyString(), eq("PAYMENT"), eq(1));
    }

    @Test
    @DisplayName("Debería obtener todos los pagos")
    void deberiaObtenerTodosLosPagos() {
        // Arrange
        when(paymentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(payment));

        // Act
        List<PaymentResponse> responses = paymentService.getAllPayments();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(paymentRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Debería obtener pago por ID de cita")
    void deberiaObtenerPagoPorIdDeCita() {
        // Arrange
        when(paymentRepository.findByAppointmentId(1L)).thenReturn(Optional.of(payment));

        // Act
        Optional<PaymentResponse> response = paymentService.getByAppointmentId(1L);

        // Assert
        assertTrue(response.isPresent());
        assertEquals("Juan Pérez", response.get().getClientName());
        verify(paymentRepository).findByAppointmentId(1L);
    }

    @Test
    @DisplayName("Debería obtener pagos por email de cliente")
    void deberiaObtenerPagosPorEmailDeCliente() {
        // Arrange
        when(paymentRepository.findByClientEmailOrderByCreatedAtDesc("juan@test.com"))
                .thenReturn(List.of(payment));

        // Act
        List<PaymentResponse> responses = paymentService.getByClientEmail("juan@test.com");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(paymentRepository).findByClientEmailOrderByCreatedAtDesc("juan@test.com");
    }

    @Test
    @DisplayName("Debería obtener precios activos")
    void deberiaObtenerPreciosActivos() {
        // Arrange
        when(servicePriceRepository.findByActiveTrueOrderByDisplayNameAsc())
                .thenReturn(List.of(servicePrice));

        // Act
        List<ServicePriceResponse> responses = paymentService.getActivePrices();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Consulta general", responses.get(0).getServiceType());
        verify(servicePriceRepository).findByActiveTrueOrderByDisplayNameAsc();
    }

    @Test
    @DisplayName("Debería crear o actualizar precio de servicio")
    void deberiaCrearOActualizarPrecioDeServicio() {
        // Arrange
        ServicePriceRequest request = new ServicePriceRequest();
        request.setServiceType("Consulta general");
        request.setDisplayName("Consulta General");
        request.setPrice(new BigDecimal("50000"));
        request.setDescription("Consulta veterinaria");
        request.setActive(true);

        when(servicePriceRepository.findByServiceType("Consulta general"))
                .thenReturn(Optional.empty());
        when(servicePriceRepository.save(any(ServicePrice.class))).thenReturn(servicePrice);

        // Act
        ServicePriceResponse response = paymentService.upsertPrice(request);

        // Assert
        assertNotNull(response);
        assertEquals("Consulta general", response.getServiceType());
        verify(servicePriceRepository).save(any(ServicePrice.class));
        verify(auditLogService).log(eq("PRICE_UPSERT"), anyString(), eq("SERVICE_PRICE"), anyInt());
    }

    @Test
    @DisplayName("Debería eliminar precio de servicio")
    void deberiaEliminarPrecioDeServicio() {
        // Arrange
        when(servicePriceRepository.existsById(1L)).thenReturn(true);

        // Act
        paymentService.deletePrice(1L);

        // Assert
        verify(servicePriceRepository).deleteById(1L);
        verify(auditLogService).log(eq("PRICE_DELETE"), anyString(), eq("SERVICE_PRICE"), eq(1));
    }

    @Test
    @DisplayName("No debería eliminar precio inexistente")
    void noDeberiaEliminarPrecioInexistente() {
        // Arrange
        when(servicePriceRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                paymentService.deletePrice(1L)
        );
        verify(servicePriceRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería obtener precio base por tipo de servicio")
    void deberiaObtenerPrecioBasePorTipoDeServicio() {
        // Arrange
        when(servicePriceRepository.findByServiceType("Consulta general"))
                .thenReturn(Optional.of(servicePrice));

        // Act
        BigDecimal price = paymentService.getBasePrice("Consulta general");

        // Assert
        assertEquals(new BigDecimal("50000"), price);
        verify(servicePriceRepository).findByServiceType("Consulta general");
    }

    @Test
    @DisplayName("Debería retornar cero si el servicio no tiene precio configurado")
    void deberiaRetornarCeroSiServicioNoTienePrecio() {
        // Arrange
        when(servicePriceRepository.findByServiceType("Servicio inexistente"))
                .thenReturn(Optional.empty());

        // Act
        BigDecimal price = paymentService.getBasePrice("Servicio inexistente");

        // Assert
        assertEquals(BigDecimal.ZERO, price);
    }
}
