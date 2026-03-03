package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo de Cita (Appointment).
 * 
 * Valida la correcta creación y manipulación de citas médicas,
 * incluyendo las relaciones con usuarios (cliente/veterinario) y mascotas.
 */
@DisplayName("Pruebas del Modelo Appointment")
class AppointmentTest {

    private Appointment testAppointment;
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
                .build();

        testVet = User.builder()
                .id(2L)
                .name("Dr. Veterinario")
                .email("vet@example.com")
                .role(Role.ROLE_VETERINARIO)
                .build();

        testPet = Pet.builder()
                .id(1L)
                .name("Firulais")
                .species("Perro")
                .ownerEmail("cliente@example.com")
                .build();

        testAppointment = Appointment.builder()
                .id(1L)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .reason("Consulta general")
                .status(AppointmentStatus.UPCOMING)
                .client(testClient)
                .vet(testVet)
                .pet(testPet)
                .build();
    }

    @Test
    @DisplayName("Appointment debe crearse con todos los campos correctos")
    void testCrearAppointment() {
        // Assert
        assertNotNull(testAppointment);
        assertEquals(1L, testAppointment.getId());
        assertEquals("Consulta general", testAppointment.getReason());
        assertEquals(AppointmentStatus.UPCOMING, testAppointment.getStatus());
        assertNotNull(testAppointment.getDate());
        assertNotNull(testAppointment.getTime());
    }

    @Test
    @DisplayName("Appointment debe tener relación con cliente")
    void testRelacionConCliente() {
        // Assert
        assertNotNull(testAppointment.getClient());
        assertEquals("Cliente Test", testAppointment.getClient().getName());
        assertEquals("cliente@example.com", testAppointment.getClient().getEmail());
    }

    @Test
    @DisplayName("Appointment debe tener relación con veterinario")
    void testRelacionConVeterinario() {
        // Assert
        assertNotNull(testAppointment.getVet());
        assertEquals("Dr. Veterinario", testAppointment.getVet().getName());
        assertEquals(Role.ROLE_VETERINARIO, testAppointment.getVet().getRole());
    }

    @Test
    @DisplayName("Appointment debe tener relación con mascota")
    void testRelacionConMascota() {
        // Assert
        assertNotNull(testAppointment.getPet());
        assertEquals("Firulais", testAppointment.getPet().getName());
        assertEquals("Perro", testAppointment.getPet().getSpecies());
    }

    @Test
    @DisplayName("Cambiar estado de cita debe actualizar correctamente")
    void testCambiarEstado() {
        // Act
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);

        // Assert
        assertEquals(AppointmentStatus.CONFIRMED, testAppointment.getStatus());
    }

    @Test
    @DisplayName("Appointment puede tener notas adicionales")
    void testNotasAdicionales() {
        // Act
        testAppointment.setNotes("Paciente con historial de alergias");

        // Assert
        assertEquals("Paciente con historial de alergias", testAppointment.getNotes());
    }

    @Test
    @DisplayName("Appointment cancelada puede tener razón de cancelación")
    void testRazonCancelacion() {
        // Act
        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        testAppointment.setCancelReason("Cliente no disponible");

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, testAppointment.getStatus());
        assertEquals("Cliente no disponible", testAppointment.getCancelReason());
    }

    @Test
    @DisplayName("Builder debe crear appointment con campos opcionales")
    void testBuilderConCamposOpcionales() {
        // Arrange & Act
        Appointment appointment = Appointment.builder()
                .date(LocalDate.now().plusDays(2))
                .time(LocalTime.of(14, 30))
                .reason("Vacunación")
                .notes("Primera dosis")
                .status(AppointmentStatus.UPCOMING)
                .client(testClient)
                .vet(testVet)
                .pet(testPet)
                .build();

        // Assert
        assertNotNull(appointment);
        assertEquals("Vacunación", appointment.getReason());
        assertEquals("Primera dosis", appointment.getNotes());
        assertEquals(LocalTime.of(14, 30), appointment.getTime());
    }

    @Test
    @DisplayName("Appointment debe permitir diferentes estados")
    void testDiferentesEstados() {
        // Act & Assert
        testAppointment.setStatus(AppointmentStatus.UPCOMING);
        assertEquals(AppointmentStatus.UPCOMING, testAppointment.getStatus());

        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        assertEquals(AppointmentStatus.CONFIRMED, testAppointment.getStatus());

        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        assertEquals(AppointmentStatus.COMPLETED, testAppointment.getStatus());

        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        assertEquals(AppointmentStatus.CANCELLED, testAppointment.getStatus());

        testAppointment.setStatus(AppointmentStatus.NO_SHOW);
        assertEquals(AppointmentStatus.NO_SHOW, testAppointment.getStatus());
    }
}
