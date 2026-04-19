package co.edu.uniquindio.backendpawsoft.utils;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {

    // User builders
    public static User buildTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .role(Role.CLIENT)
                .isActive(true)
                .isEmailVerified(true)
                .isTwoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User buildTestVet() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("vet@example.com")
                .password("$2a$10$encodedPassword")
                .firstName("Dr. Test")
                .lastName("Veterinarian")
                .phone("1234567891")
                .role(Role.VETERINARIAN)
                .isActive(true)
                .isEmailVerified(true)
                .isTwoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User buildTestAdmin() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .password("$2a$10$encodedPassword")
                .firstName("Admin")
                .lastName("User")
                .phone("1234567892")
                .role(Role.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .isTwoFactorEnabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Pet builders
    public static Pet buildTestPet(User owner) {
        return Pet.builder()
                .id(UUID.randomUUID())
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(3)
                .weight(25.5)
                .color("Golden")
                .owner(owner)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Appointment builders
    public static Appointment buildTestAppointment(User client, User vet, Pet pet) {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .client(client)
                .veterinarian(vet)
                .pet(pet)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .reason("Regular checkup")
                .status("SCHEDULED")
                .notes("Test appointment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // DTO builders
    public static LoginRequest buildLoginRequest() {
        return LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .recaptchaToken("test-recaptcha-token")
                .build();
    }

    public static RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .phone("1234567893")
                .recaptchaToken("test-recaptcha-token")
                .build();
    }

    public static CreateAppointmentRequest buildCreateAppointmentRequest(UUID petId) {
        return CreateAppointmentRequest.builder()
                .petId(petId)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .reason("Regular checkup")
                .notes("Test appointment creation")
                .build();
    }

    public static UpdateProfileRequest buildUpdateProfileRequest() {
        return UpdateProfileRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("9876543210")
                .build();
    }

    // Payment builders
    public static Payment buildTestPayment(User client, Appointment appointment) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .client(client)
                .appointment(appointment)
                .amount(100.0)
                .paymentMethod("CREDIT_CARD")
                .status("COMPLETED")
                .transactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8))
                .paymentDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Medical Record builders
    public static MedicalRecord buildTestMedicalRecord(Pet pet, User vet, Appointment appointment) {
        return MedicalRecord.builder()
                .id(UUID.randomUUID())
                .pet(pet)
                .veterinarian(vet)
                .appointment(appointment)
                .diagnosis("Healthy pet")
                .treatment("Regular vaccination")
                .medications("None")
                .notes("Pet is in good health")
                .recordDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Refresh Token builders
    public static RefreshToken buildTestRefreshToken(User user) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Two Factor Auth builders
    public static TwoFactorAuth buildTestTwoFactorAuth(User user) {
        return TwoFactorAuth.builder()
                .id(UUID.randomUUID())
                .user(user)
                .code("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}