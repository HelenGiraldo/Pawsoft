package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.CreateMedicalRecordRequest;
import co.edu.uniquindio.backendpawsoft.dto.UpdateMedicalRecordRequest;
import co.edu.uniquindio.backendpawsoft.exception.MedicalRecordNotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedAccessException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.MedicalRecord;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.MedicalRecordRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService Tests")
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private User client;
    private User vet;
    private Pet pet;
    private Appointment appointment;
    private MedicalRecord medicalRecord;
    private CreateMedicalRecordRequest createRequest;
    private UpdateMedicalRecordRequest updateRequest;

    @BeforeEach
    void setUp() {
        client = TestDataBuilder.buildTestUser();
        vet = TestDataBuilder.buildTestVet();
        pet = TestDataBuilder.buildTestPet(client);
        appointment = TestDataBuilder.buildTestAppointment(client, vet, pet);
        medicalRecord = TestDataBuilder.buildTestMedicalRecord(pet, vet, appointment);

        createRequest = CreateMedicalRecordRequest.builder()
                .petId(pet.getId())
                .appointmentId(appointment.getId())
                .diagnosis("Healthy pet")
                .treatment("Regular vaccination")
                .medications("Vaccine XYZ")
                .notes("Pet is in excellent health")
                .build();

        updateRequest = UpdateMedicalRecordRequest.builder()
                .diagnosis("Updated diagnosis")
                .treatment("Updated treatment")
                .medications("Updated medications")
                .notes("Updated notes")
                .build();
    }

    @Test
    @DisplayName("Should successfully create medical record")
    void shouldCreateMedicalRecord() {
        // Given
        when(petRepository.findById(pet.getId())).thenReturn(Optional.of(pet));
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findById(vet.getId())).thenReturn(Optional.of(vet));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        // When
        MedicalRecord result = medicalRecordService.createMedicalRecord(createRequest, vet.getId());

        // Then
        assertNotNull(result);
        assertEquals(medicalRecord.getDiagnosis(), result.getDiagnosis());
        assertEquals(medicalRecord.getTreatment(), result.getTreatment());
        assertEquals(pet, result.getPet());
        assertEquals(vet, result.getVeterinarian());

        verify(petRepository).findById(pet.getId());
        verify(appointmentRepository).findById(appointment.getId());
        verify(userRepository).findById(vet.getId());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
        verify(auditLogService).logMedicalRecordCreated(vet.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw exception when pet not found")
    void shouldThrowExceptionWhenPetNotFound() {
        // Given
        when(petRepository.findById(pet.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PetNotFoundException.class, 
                () -> medicalRecordService.createMedicalRecord(createRequest, vet.getId()));

        verify(petRepository).findById(pet.getId());
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(medicalRecordRepository);
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        when(petRepository.findById(pet.getId())).thenReturn(Optional.of(pet));
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppointmentNotFoundException.class, 
                () -> medicalRecordService.createMedicalRecord(createRequest, vet.getId()));

        verify(petRepository).findById(pet.getId());
        verify(appointmentRepository).findById(appointment.getId());
        verifyNoInteractions(medicalRecordRepository);
    }

    @Test
    @DisplayName("Should throw exception when veterinarian not found")
    void shouldThrowExceptionWhenVeterinarianNotFound() {
        // Given
        UUID vetId = UUID.randomUUID();
        when(petRepository.findById(pet.getId())).thenReturn(Optional.of(pet));
        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findById(vetId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> medicalRecordService.createMedicalRecord(createRequest, vetId));

        verify(userRepository).findById(vetId);
        verifyNoInteractions(medicalRecordRepository);
    }

    @Test
    @DisplayName("Should successfully get medical record by ID")
    void shouldGetMedicalRecordById() {
        // Given
        when(medicalRecordRepository.findById(medicalRecord.getId())).thenReturn(Optional.of(medicalRecord));

        // When
        MedicalRecord result = medicalRecordService.getMedicalRecordById(medicalRecord.getId());

        // Then
        assertNotNull(result);
        assertEquals(medicalRecord.getId(), result.getId());
        assertEquals(medicalRecord.getDiagnosis(), result.getDiagnosis());

        verify(medicalRecordRepository).findById(medicalRecord.getId());
    }

    @Test
    @DisplayName("Should throw exception when medical record not found by ID")
    void shouldThrowExceptionWhenMedicalRecordNotFoundById() {
        // Given
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MedicalRecordNotFoundException.class, 
                () -> medicalRecordService.getMedicalRecordById(recordId));

        verify(medicalRecordRepository).findById(recordId);
    }

    @Test
    @DisplayName("Should successfully get medical records by pet")
    void shouldGetMedicalRecordsByPet() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        Page<MedicalRecord> recordPage = new PageImpl<>(records, pageable, 1);
        
        when(medicalRecordRepository.findByPetIdOrderByRecordDateDesc(pet.getId(), pageable))
                .thenReturn(recordPage);

        // When
        Page<MedicalRecord> result = medicalRecordService.getMedicalRecordsByPet(pet.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(medicalRecord.getId(), result.getContent().get(0).getId());

        verify(medicalRecordRepository).findByPetIdOrderByRecordDateDesc(pet.getId(), pageable);
    }

    @Test
    @DisplayName("Should successfully get medical records by veterinarian")
    void shouldGetMedicalRecordsByVeterinarian() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        Page<MedicalRecord> recordPage = new PageImpl<>(records, pageable, 1);
        
        when(medicalRecordRepository.findByVeterinarianIdOrderByRecordDateDesc(vet.getId(), pageable))
                .thenReturn(recordPage);

        // When
        Page<MedicalRecord> result = medicalRecordService.getMedicalRecordsByVeterinarian(vet.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(medicalRecord.getId(), result.getContent().get(0).getId());

        verify(medicalRecordRepository).findByVeterinarianIdOrderByRecordDateDesc(vet.getId(), pageable);
    }

    @Test
    @DisplayName("Should successfully update medical record")
    void shouldUpdateMedicalRecord() {
        // Given
        when(medicalRecordRepository.findById(medicalRecord.getId())).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordRepository.save(medicalRecord)).thenReturn(medicalRecord);

        // When
        MedicalRecord result = medicalRecordService.updateMedicalRecord(medicalRecord.getId(), updateRequest, vet.getId());

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getDiagnosis(), result.getDiagnosis());
        assertEquals(updateRequest.getTreatment(), result.getTreatment());
        assertEquals(updateRequest.getMedications(), result.getMedications());
        assertEquals(updateRequest.getNotes(), result.getNotes());

        verify(medicalRecordRepository).findById(medicalRecord.getId());
        verify(medicalRecordRepository).save(medicalRecord);
        verify(auditLogService).logMedicalRecordUpdated(vet.getId(), medicalRecord.getId());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent medical record")
    void shouldThrowExceptionWhenUpdatingNonExistentMedicalRecord() {
        // Given
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MedicalRecordNotFoundException.class, 
                () -> medicalRecordService.updateMedicalRecord(recordId, updateRequest, vet.getId()));

        verify(medicalRecordRepository).findById(recordId);
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when non-owner veterinarian tries to update medical record")
    void shouldThrowExceptionWhenNonOwnerVeterinarianTriesToUpdateMedicalRecord() {
        // Given
        UUID anotherVetId = UUID.randomUUID();
        when(medicalRecordRepository.findById(medicalRecord.getId())).thenReturn(Optional.of(medicalRecord));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, 
                () -> medicalRecordService.updateMedicalRecord(medicalRecord.getId(), updateRequest, anotherVetId));

        verify(medicalRecordRepository).findById(medicalRecord.getId());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Should successfully delete medical record")
    void shouldDeleteMedicalRecord() {
        // Given
        when(medicalRecordRepository.findById(medicalRecord.getId())).thenReturn(Optional.of(medicalRecord));

        // When
        medicalRecordService.deleteMedicalRecord(medicalRecord.getId(), vet.getId());

        // Then
        verify(medicalRecordRepository).findById(medicalRecord.getId());
        verify(medicalRecordRepository).delete(medicalRecord);
        verify(auditLogService).logMedicalRecordDeleted(vet.getId(), medicalRecord.getId());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent medical record")
    void shouldThrowExceptionWhenDeletingNonExistentMedicalRecord() {
        // Given
        UUID recordId = UUID.randomUUID();
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MedicalRecordNotFoundException.class, 
                () -> medicalRecordService.deleteMedicalRecord(recordId, vet.getId()));

        verify(medicalRecordRepository).findById(recordId);
        verify(medicalRecordRepository, never()).delete(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when non-owner veterinarian tries to delete medical record")
    void shouldThrowExceptionWhenNonOwnerVeterinarianTriesToDeleteMedicalRecord() {
        // Given
        UUID anotherVetId = UUID.randomUUID();
        when(medicalRecordRepository.findById(medicalRecord.getId())).thenReturn(Optional.of(medicalRecord));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, 
                () -> medicalRecordService.deleteMedicalRecord(medicalRecord.getId(), anotherVetId));

        verify(medicalRecordRepository).findById(medicalRecord.getId());
        verify(medicalRecordRepository, never()).delete(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Should successfully get medical records by date range")
    void shouldGetMedicalRecordsByDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        Page<MedicalRecord> recordPage = new PageImpl<>(records, pageable, 1);
        
        when(medicalRecordRepository.findByRecordDateBetweenOrderByRecordDateDesc(startDate, endDate, pageable))
                .thenReturn(recordPage);

        // When
        Page<MedicalRecord> result = medicalRecordService.getMedicalRecordsByDateRange(startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(medicalRecord.getId(), result.getContent().get(0).getId());

        verify(medicalRecordRepository).findByRecordDateBetweenOrderByRecordDateDesc(startDate, endDate, pageable);
    }

    @Test
    @DisplayName("Should successfully search medical records by diagnosis")
    void shouldSearchMedicalRecordsByDiagnosis() {
        // Given
        String searchTerm = "healthy";
        Pageable pageable = PageRequest.of(0, 10);
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        Page<MedicalRecord> recordPage = new PageImpl<>(records, pageable, 1);
        
        when(medicalRecordRepository.findByDiagnosisContainingIgnoreCaseOrderByRecordDateDesc(searchTerm, pageable))
                .thenReturn(recordPage);

        // When
        Page<MedicalRecord> result = medicalRecordService.searchMedicalRecordsByDiagnosis(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(medicalRecord.getId(), result.getContent().get(0).getId());

        verify(medicalRecordRepository).findByDiagnosisContainingIgnoreCaseOrderByRecordDateDesc(searchTerm, pageable);
    }

    @Test
    @DisplayName("Should get medical record statistics")
    void shouldGetMedicalRecordStatistics() {
        // Given
        when(medicalRecordRepository.count()).thenReturn(100L);
        when(medicalRecordRepository.countByRecordDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(10L);
        when(medicalRecordRepository.countByVeterinarianId(vet.getId())).thenReturn(25L);

        // When
        var stats = medicalRecordService.getMedicalRecordStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.get("total"));
        assertEquals(10L, stats.get("thisMonth"));

        verify(medicalRecordRepository).count();
        verify(medicalRecordRepository).countByRecordDateBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should successfully get medical records by pet and owner")
    void shouldGetMedicalRecordsByPetAndOwner() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        Page<MedicalRecord> recordPage = new PageImpl<>(records, pageable, 1);
        
        when(medicalRecordRepository.findByPetIdAndPetOwnerIdOrderByRecordDateDesc(pet.getId(), client.getId(), pageable))
                .thenReturn(recordPage);

        // When
        Page<MedicalRecord> result = medicalRecordService.getMedicalRecordsByPetAndOwner(pet.getId(), client.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(medicalRecord.getId(), result.getContent().get(0).getId());

        verify(medicalRecordRepository).findByPetIdAndPetOwnerIdOrderByRecordDateDesc(pet.getId(), client.getId(), pageable);
    }

    @Test
    @DisplayName("Should check if medical record exists for appointment")
    void shouldCheckIfMedicalRecordExistsForAppointment() {
        // Given
        when(medicalRecordRepository.existsByAppointmentId(appointment.getId())).thenReturn(true);

        // When
        boolean result = medicalRecordService.existsByAppointment(appointment.getId());

        // Then
        assertTrue(result);

        verify(medicalRecordRepository).existsByAppointmentId(appointment.getId());
    }

    @Test
    @DisplayName("Should return false when no medical record exists for appointment")
    void shouldReturnFalseWhenNoMedicalRecordExistsForAppointment() {
        // Given
        when(medicalRecordRepository.existsByAppointmentId(appointment.getId())).thenReturn(false);

        // When
        boolean result = medicalRecordService.existsByAppointment(appointment.getId());

        // Then
        assertFalse(result);

        verify(medicalRecordRepository).existsByAppointmentId(appointment.getId());
    }
}