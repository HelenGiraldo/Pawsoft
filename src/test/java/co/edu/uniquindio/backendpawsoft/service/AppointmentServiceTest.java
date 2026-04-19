package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.CreateAppointmentRequest;
import co.edu.uniquindio.backendpawsoft.dto.UpdateAppointmentRequest;
import co.edu.uniquindio.backendpawsoft.exception.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User client;
    private User vet;
    private Pet pet;
    private Appointment appointment;
    private CreateAppointmentRequest createRequest;

    @BeforeEach
    void setUp() {
        client = TestDataBuilder.buildTestUser();
        vet = TestDataBuilder.buildTestVet();
        pet = TestDataBuilder.buildTestPet(client);
        appointment = TestDataBuilder.buildTestAppointment(client, vet, pet);
        createRequest = TestDataBuilder.buildCreateAppointmentRequest(pet.getId());
    }

    @Test
    @DisplayName("Should successfully create appointment")
    void shouldCreateAppointment() {
        // Given
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.of(pet));
        when(userRepository.findAvailableVeterinarian(any(LocalDateTime.class))).thenReturn(Optional.of(vet));
        when(appointmentRepository.existsByVeterinarianAndAppointmentDate(vet, createRequest.getAppointmentDate()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        Appointment result = appointmentService.createAppointment(createRequest, client.getId());

        // Then
        assertNotNull(result);
        assertEquals(appointment.getId(), result.getId());
        assertEquals(client, result.getClient());
        assertEquals(vet, result.getVeterinarian());
        assertEquals(pet, result.getPet());
        assertEquals("SCHEDULED", result.getStatus());

        verify(petRepository).findById(createRequest.getPetId());
        verify(userRepository).findAvailableVeterinarian(createRequest.getAppointmentDate());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(emailService).sendAppointmentConfirmation(eq(client.getEmail()), any(Appointment.class));
        verify(auditLogService).logAppointmentCreated(client.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw exception when pet not found")
    void shouldThrowExceptionWhenPetNotFound() {
        // Given
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PetNotFoundException.class, 
                () -> appointmentService.createAppointment(createRequest, client.getId()));

        verify(petRepository).findById(createRequest.getPetId());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should throw exception when pet doesn't belong to client")
    void shouldThrowExceptionWhenPetDoesntBelongToClient() {
        // Given
        User anotherClient = TestDataBuilder.buildTestUser();
        anotherClient.setId(UUID.randomUUID());
        pet.setOwner(anotherClient);
        
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.of(pet));

        // When & Then
        assertThrows(UnauthorizedPetAccessException.class, 
                () -> appointmentService.createAppointment(createRequest, client.getId()));

        verify(petRepository).findById(createRequest.getPetId());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should throw exception when no veterinarian available")
    void shouldThrowExceptionWhenNoVeterinarianAvailable() {
        // Given
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.of(pet));
        when(userRepository.findAvailableVeterinarian(any(LocalDateTime.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoVeterinarianAvailableException.class, 
                () -> appointmentService.createAppointment(createRequest, client.getId()));

        verify(petRepository).findById(createRequest.getPetId());
        verify(userRepository).findAvailableVeterinarian(createRequest.getAppointmentDate());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should throw exception when veterinarian is not available at requested time")
    void shouldThrowExceptionWhenVeterinarianNotAvailableAtTime() {
        // Given
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.of(pet));
        when(userRepository.findAvailableVeterinarian(any(LocalDateTime.class))).thenReturn(Optional.of(vet));
        when(appointmentRepository.existsByVeterinarianAndAppointmentDate(vet, createRequest.getAppointmentDate()))
                .thenReturn(true);

        // When & Then
        assertThrows(VeterinarianNotAvailableException.class, 
                () -> appointmentService.createAppointment(createRequest, client.getId()));

        verify(appointmentRepository).existsByVeterinarianAndAppointmentDate(vet, createRequest.getAppointmentDate());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when appointment date is in the past")
    void shouldThrowExceptionWhenAppointmentDateIsInPast() {
        // Given
        createRequest.setAppointmentDate(LocalDateTime.now().minusDays(1));
        when(petRepository.findById(createRequest.getPetId())).thenReturn(Optional.of(pet));

        // When & Then
        assertThrows(InvalidAppointmentDateException.class, 
                () -> appointmentService.createAppointment(createRequest, client.getId()));

        verify(petRepository).findById(createRequest.getPetId());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should successfully get appointments by client")
    void shouldGetAppointmentsByClient() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Appointment> appointments = Arrays.asList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments, pageable, 1);
        
        when(appointmentRepository.findByClientIdOrderByAppointmentDateDesc(client.getId(), pageable))
                .thenReturn(appointmentPage);

        // When
        Page<Appointment> result = appointmentService.getAppointmentsByClient(client.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(appointment.getId(), result.getContent().get(0).getId());

        verify(appointmentRepository).findByClientIdOrderByAppointmentDateDesc(client.getId(), pageable);
    }

    @Test
    @DisplayName("Should successfully get appointments by veterinarian")
    void shouldGetAppointmentsByVeterinarian() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Appointment> appointments = Arrays.asList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments, pageable, 1);
        
        when(appointmentRepository.findByVeterinarianIdOrderByAppointmentDateDesc(vet.getId(), pageable))
                .thenReturn(appointmentPage);

        // When
        Page<Appointment> result = appointmentService.getAppointmentsByVeterinarian(vet.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(appointment.getId(), result.getContent().get(0).getId());

        verify(appointmentRepository).findByVeterinarianIdOrderByAppointmentDateDesc(vet.getId(), pageable);
    }

    @Test
    @DisplayName("Should successfully update appointment")
    void shouldUpdateAppointment() {
        // Given
        UpdateAppointmentRequest updateRequest = UpdateAppointmentRequest.builder()
                .reason("Updated reason")
                .notes("Updated notes")
                .build();

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        // When
        Appointment result = appointmentService.updateAppointment(appointment.getId(), updateRequest, client.getId());

        // Then
        assertNotNull(result);
        assertEquals("Updated reason", result.getReason());
        assertEquals("Updated notes", result.getNotes());

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository).save(appointment);
        verify(auditLogService).logAppointmentUpdated(client.getId(), appointment.getId());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent appointment")
    void shouldThrowExceptionWhenUpdatingNonExistentAppointment() {
        // Given
        UUID appointmentId = UUID.randomUUID();
        UpdateAppointmentRequest updateRequest = UpdateAppointmentRequest.builder()
                .reason("Updated reason")
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppointmentNotFoundException.class, 
                () -> appointmentService.updateAppointment(appointmentId, updateRequest, client.getId()));

        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when client tries to update another client's appointment")
    void shouldThrowExceptionWhenClientTriesToUpdateAnotherClientsAppointment() {
        // Given
        UUID anotherClientId = UUID.randomUUID();
        UpdateAppointmentRequest updateRequest = UpdateAppointmentRequest.builder()
                .reason("Updated reason")
                .build();

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(UnauthorizedAppointmentAccessException.class, 
                () -> appointmentService.updateAppointment(appointment.getId(), updateRequest, anotherClientId));

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should successfully cancel appointment")
    void shouldCancelAppointment() {
        // Given
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        // When
        appointmentService.cancelAppointment(appointment.getId(), client.getId());

        // Then
        assertEquals("CANCELLED", appointment.getStatus());

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository).save(appointment);
        verify(emailService).sendAppointmentCancellation(eq(client.getEmail()), any(Appointment.class));
        verify(emailService).sendAppointmentCancellation(eq(vet.getEmail()), any(Appointment.class));
        verify(auditLogService).logAppointmentCancelled(client.getId(), appointment.getId());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled appointment")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledAppointment() {
        // Given
        appointment.setStatus("CANCELLED");
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(AppointmentAlreadyCancelledException.class, 
                () -> appointmentService.cancelAppointment(appointment.getId(), client.getId()));

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should successfully complete appointment")
    void shouldCompleteAppointment() {
        // Given
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        // When
        appointmentService.completeAppointment(appointment.getId(), vet.getId());

        // Then
        assertEquals("COMPLETED", appointment.getStatus());

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository).save(appointment);
        verify(auditLogService).logAppointmentCompleted(vet.getId(), appointment.getId());
    }

    @Test
    @DisplayName("Should throw exception when non-assigned veterinarian tries to complete appointment")
    void shouldThrowExceptionWhenNonAssignedVeterinarianTriesToCompleteAppointment() {
        // Given
        UUID anotherVetId = UUID.randomUUID();
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(UnauthorizedAppointmentAccessException.class, 
                () -> appointmentService.completeAppointment(appointment.getId(), anotherVetId));

        verify(appointmentRepository).findById(appointment.getId());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should get appointment statistics")
    void shouldGetAppointmentStatistics() {
        // Given
        when(appointmentRepository.countByStatus("SCHEDULED")).thenReturn(5L);
        when(appointmentRepository.countByStatus("COMPLETED")).thenReturn(10L);
        when(appointmentRepository.countByStatus("CANCELLED")).thenReturn(2L);
        when(appointmentRepository.countByAppointmentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);

        // When
        var stats = appointmentService.getAppointmentStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(5L, stats.get("scheduled"));
        assertEquals(10L, stats.get("completed"));
        assertEquals(2L, stats.get("cancelled"));
        assertEquals(3L, stats.get("today"));

        verify(appointmentRepository).countByStatus("SCHEDULED");
        verify(appointmentRepository).countByStatus("COMPLETED");
        verify(appointmentRepository).countByStatus("CANCELLED");
        verify(appointmentRepository).countByAppointmentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}