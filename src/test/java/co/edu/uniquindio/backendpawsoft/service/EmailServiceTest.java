package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        testAppointment = TestDataBuilder.buildTestAppointment(testUser, TestDataBuilder.buildTestVet(), TestDataBuilder.buildTestPet(testUser));
        
        // Set email properties using reflection
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@pawsoft.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://pawsoft.com");
    }

    @Test
    @DisplayName("Should send verification email successfully")
    void shouldSendVerificationEmail() {
        // Given
        String email = "test@example.com";
        String token = "verification-token";

        // When
        emailService.sendVerificationEmail(email, token);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send two factor code email successfully")
    void shouldSendTwoFactorCodeEmail() {
        // Given
        String email = "test@example.com";
        String code = "123456";

        // When
        emailService.sendTwoFactorCode(email, code);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send password reset email successfully")
    void shouldSendPasswordResetEmail() {
        // Given
        String email = "test@example.com";
        String token = "reset-token";

        // When
        emailService.sendPasswordResetEmail(email, token);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send appointment confirmation email successfully")
    void shouldSendAppointmentConfirmationEmail() {
        // Given
        String email = "test@example.com";

        // When
        emailService.sendAppointmentConfirmation(email, testAppointment);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send appointment cancellation email successfully")
    void shouldSendAppointmentCancellationEmail() {
        // Given
        String email = "test@example.com";

        // When
        emailService.sendAppointmentCancellation(email, testAppointment);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send appointment reminder email successfully")
    void shouldSendAppointmentReminderEmail() {
        // Given
        String email = "test@example.com";

        // When
        emailService.sendAppointmentReminder(email, testAppointment);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void shouldHandleEmailSendingFailure() {
        // Given
        String email = "test@example.com";
        String token = "verification-token";
        
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then - Should not throw exception
        emailService.sendVerificationEmail(email, token);
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void shouldSendWelcomeEmail() {
        // Given
        String email = "test@example.com";
        String firstName = "Test";

        // When
        emailService.sendWelcomeEmail(email, firstName);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send payment confirmation email successfully")
    void shouldSendPaymentConfirmationEmail() {
        // Given
        String email = "test@example.com";
        String paymentId = "PAY123";
        Double amount = 100.0;

        // When
        emailService.sendPaymentConfirmation(email, paymentId, amount);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}