package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.AppointmentResponse;
import co.edu.uniquindio.backendpawsoft.dto.CreateAppointmentRequest;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PetRepository petRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User client;
    private User vet;
    private Pet pet;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        client = TestDataBuilder.buildTestUser();
        vet = TestDataBuilder.buildTestVet();

        pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");
        pet.setOwnerEmail(client.getEmail());

        appointment = Appointment.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(2))
                .time(LocalTime.of(10, 0))
                .reason("Checkup")
                .status(AppointmentStatus.UPCOMING)
                .client(client)
                .vet(vet)
                .pet(pet)
                .build();
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void shouldCreateAppointmentSuccessfully() {
        // record: petId, date, time, vetId, reason
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 2L, "Checkup"
        );

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(userRepository.findById(vet.getId())).thenReturn(Optional.of(vet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(appointmentRepository.existsByVetIdAndDateAndTime(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse result = appointmentService.createAppointment(req, client.getEmail());
        assertNotNull(result);
        verify(appointmentRepository).save(any(Appointment.class));
        verify(auditLogService).log(eq("APPOINTMENT_CREATE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw NotFoundException when client not found")
    void shouldThrowWhenClientNotFound() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 2L, "Checkup"
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> appointmentService.createAppointment(req, "unknown@test.com"));
    }

    @Test
    @DisplayName("Should throw when slot is already taken")
    void shouldThrowWhenSlotAlreadyTaken() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 2L, "Checkup"
        );

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(userRepository.findById(vet.getId())).thenReturn(Optional.of(vet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(appointmentRepository.existsByVetIdAndDateAndTime(anyLong(), any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(req, client.getEmail()));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when appointment date is in the past")
    void shouldThrowWhenDateIsInPast() {
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                1L, LocalDate.now().minusDays(1), LocalTime.of(10, 0), 2L, "Checkup"
        );

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(userRepository.findById(vet.getId())).thenReturn(Optional.of(vet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(req, client.getEmail()));
    }

    @Test
    @DisplayName("Should get appointments by client email")
    void shouldGetAppointmentsByClientEmail() {
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(appointmentRepository.findByClientIdOrderByDateAscTimeAsc(client.getId()))
                .thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getAppointmentsByClient(client.getEmail());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should cancel appointment successfully")
    void shouldCancelAppointmentSuccessfully() {
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        appointmentService.cancelAppointment(1L, client.getEmail());

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Should throw when cancelling already cancelled appointment")
    void shouldThrowWhenCancellingAlreadyCancelled() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class,
                () -> appointmentService.cancelAppointment(1L, client.getEmail()));
    }

    @Test
    @DisplayName("Should start appointment successfully")
    void shouldStartAppointmentSuccessfully() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findByVetIdOrderByDateAscTimeAsc(vet.getId())).thenReturn(List.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        appointmentService.startAppointment(1L, vet.getEmail());

        assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
    }

    @Test
    @DisplayName("Should complete appointment successfully")
    void shouldCompleteAppointmentSuccessfully() {
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        when(userRepository.findByEmail(vet.getEmail())).thenReturn(Optional.of(vet));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        appointmentService.completeAppointment(1L, vet.getEmail());

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }
}
