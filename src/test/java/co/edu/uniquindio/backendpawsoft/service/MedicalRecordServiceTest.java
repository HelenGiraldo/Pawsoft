package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordRequest;
import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordResponse;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.MedicalRecord;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.HospitalizationRepository;
import co.edu.uniquindio.backendpawsoft.repository.MedicalRecordRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService Tests")
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServicePriceRepository servicePriceRepository;
    @Mock private HospitalizationRepository hospitalizationRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private User vet;
    private User client;
    private Pet pet;
    private Appointment appointment;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        vet = TestDataBuilder.buildTestVet();
        client = TestDataBuilder.buildTestUser();

        pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");

        appointment = Appointment.builder()
                .id(1L)
                .date(LocalDate.now())
                .time(LocalTime.of(10, 0))
                .reason("Consulta general")
                .status(AppointmentStatus.IN_PROGRESS)
                .client(client)
                .vet(vet)
                .pet(pet)
                .build();

        medicalRecord = MedicalRecord.builder()
                .id(1L)
                .appointment(appointment)
                .pet(pet)
                .vet(vet)
                .diagnosticoPrincipal("Saludable")
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save medical record and complete appointment when cerrar=true")
    void shouldSaveAndCompleteAppointment() {
        MedicalRecordRequest req = buildRequest();

        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(servicePriceRepository.findByServiceType(anyString())).thenReturn(Optional.empty());
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        MedicalRecordResponse result = medicalRecordService.guardar(req, vet.getEmail(), true);

        assertNotNull(result);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
        verify(auditLogService).log(eq("APPOINTMENT_COMPLETE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should save medical record without completing when cerrar=false")
    void shouldSaveWithoutCompleting() {
        MedicalRecordRequest req = buildRequest();

        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(servicePriceRepository.findByServiceType(anyString())).thenReturn(Optional.empty());
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        medicalRecordService.guardar(req, vet.getEmail(), false);

        assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when vet not found")
    void shouldThrowWhenVetNotFound() {
        MedicalRecordRequest req = buildRequest();
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.guardar(req, "unknown@test.com", false));
    }

    @Test
    @DisplayName("Should throw NotFoundException when appointment not found")
    void shouldThrowWhenAppointmentNotFound() {
        MedicalRecordRequest req = buildRequest();
        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.guardar(req, vet.getEmail(), false));
    }

    @Test
    @DisplayName("Should get records by vet email")
    void shouldGetRecordsByVet() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(medicalRecordRepository.findByVetIdOrderByCreadoEnDesc(vet.getId()))
                .thenReturn(List.of(medicalRecord));
        when(servicePriceRepository.findByServiceType(anyString())).thenReturn(Optional.empty());
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        List<MedicalRecordResponse> result = medicalRecordService.getByVet(vet.getEmail());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get records by pet ID")
    void shouldGetRecordsByPet() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(medicalRecordRepository.findByPetIdOrderByCreadoEnDesc(1L))
                .thenReturn(List.of(medicalRecord));
        when(servicePriceRepository.findByServiceType(anyString())).thenReturn(Optional.empty());
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        List<MedicalRecordResponse> result = medicalRecordService.getByPet(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get record by appointment ID")
    void shouldGetRecordByAppointment() {
        when(medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.of(medicalRecord));
        when(servicePriceRepository.findByServiceType(anyString())).thenReturn(Optional.empty());
        when(hospitalizationRepository.findByPetIdAndStatus(anyLong(), any())).thenReturn(List.of());

        MedicalRecordResponse result = medicalRecordService.getByAppointment(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Should throw when record not found by appointment")
    void shouldThrowWhenRecordNotFoundByAppointment() {
        when(medicalRecordRepository.findByAppointmentId(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalRecordService.getByAppointment(99L));
    }

    private MedicalRecordRequest buildRequest() {
        return new MedicalRecordRequest(
                1L,           // appointmentId
                null,         // peso
                null,         // temperatura
                null,         // frecuenciaCardiaca
                null,         // frecuenciaRespiratoria
                null,         // observacionesGenerales
                "Saludable",  // diagnosticoPrincipal
                null,         // diagnosticoSecundario
                null,         // notasClinicas
                null,         // medicamentos
                null,         // indicaciones
                null,         // diagnosticoCliente
                null,         // medicamentosRecetados
                null,         // indicacionesCliente
                null,         // vacunasAplicadas
                null,         // proximoControlFecha
                null,         // proximoControlMotivo
                null,         // fotosAdjuntas
                BigDecimal.ZERO, // costoMedicamentos
                BigDecimal.ZERO  // costoTotal
        );
    }
}
