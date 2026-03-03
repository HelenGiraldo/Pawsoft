package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.AppointmentResponse;
import co.edu.uniquindio.backendpawsoft.dto.CreateAppointmentRequest;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de gestión de citas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Citas")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User testClient;
    private User testVet;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        testClient = User.builder()
                .id(1L)
                .name("Cliente Test")
                .email("cliente@example.com")
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .build();

        testVet = User.builder()
                .id(2L)
                .name("Dr. Veterinario")
                .email("vet@example.com")
                .role(Role.ROLE_VETERINARIO)
                .enabled(true)
                .build();

        testPet = Pet.builder()
                .id(1L)
                .name("Firulais")
                .species("Perro")
                .breed("Labrador")
                .ownerEmail("cliente@example.com")
                .build();
    }

    @Test
    @DisplayName("Crear cita exitosamente debe retornar AppointmentResponse")
    void testCreateAppointmentExitoso() {
        // Arrange
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                2L,
                "Consulta general"
        );

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(testClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testVet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(appointmentRepository.existsByVetIdAndDateAndTime(anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });

        // Act
        AppointmentResponse response = appointmentService.createAppointment(request, "cliente@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("Consulta general", response.reason());
        assertEquals(AppointmentStatus.UPCOMING, response.status());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Crear cita con fecha pasada debe lanzar excepción")
    void testCreateAppointmentFechaPasada() {
        // Arrange
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                2L,
                "Consulta"
        );

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(testClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testVet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request, "cliente@example.com")
        );
        assertTrue(exception.getMessage().contains("fechas pasadas"));
    }

    @Test
    @DisplayName("Crear cita fuera de horario debe lanzar excepción")
    void testCreateAppointmentFueraDeHorario() {
        // Arrange
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(22, 0),
                2L,
                "Consulta"
        );

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(testClient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testVet));
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request, "cliente@example.com")
        );
        assertTrue(exception.getMessage().contains("horario de atención"));
    }

    @Test
    @DisplayName("Obtener citas por cliente debe retornar lista ordenada")
    void testGetAppointmentsByClient() {
        // Arrange
        Appointment apt1 = Appointment.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .reason("Consulta 1")
                .status(AppointmentStatus.UPCOMING)
                .client(testClient)
                .vet(testVet)
                .pet(testPet)
                .build();

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(testClient));
        when(appointmentRepository.findByClientIdOrderByDateAscTimeAsc(1L))
                .thenReturn(Arrays.asList(apt1));

        // Act
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByClient("cliente@example.com");

        // Assert
        assertNotNull(appointments);
        assertEquals(1, appointments.size());
        assertEquals("Consulta 1", appointments.get(0).reason());
    }

    @Test
    @DisplayName("Cancelar cita propia debe actualizar estado")
    void testCancelAppointment() {
        // Arrange
        Appointment apt = Appointment.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .status(AppointmentStatus.UPCOMING)
                .client(testClient)
                .vet(testVet)
                .pet(testPet)
                .build();

        when(userRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(testClient));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(apt);

        // Act
        appointmentService.cancelAppointment(1L, "cliente@example.com");

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, apt.getStatus());
        verify(appointmentRepository, times(1)).save(apt);
    }

    @Test
    @DisplayName("Cancelar cita de otro usuario debe lanzar excepción")
    void testCancelAppointmentSinPermiso() {
        // Arrange
        User otroCliente = User.builder()
                .id(3L)
                .email("otro@example.com")
                .build();

        Appointment apt = Appointment.builder()
                .id(1L)
                .client(testClient)
                .build();

        when(userRepository.findByEmail("otro@example.com")).thenReturn(Optional.of(otroCliente));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.cancelAppointment(1L, "otro@example.com")
        );
        assertTrue(exception.getMessage().contains("permisos"));
    }
}
