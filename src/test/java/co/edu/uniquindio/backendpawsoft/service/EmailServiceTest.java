package co.edu.uniquindio.backendpawsoft.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://pawsoft.online");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Should send verification email")
    void shouldSendVerificationEmail() {
        emailService.sendVerificationEmail("test@example.com", "verify-token");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send 2FA code email")
    void shouldSend2FACodeEmail() {
        emailService.enviarCodigo2FA("test@example.com", "123456");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send temporary password email")
    void shouldSendTemporaryPasswordEmail() {
        emailService.sendTemporaryPassword("staff@example.com", "TempPass1!");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send password reset email")
    void shouldSendPasswordResetEmail() {
        emailService.sendPasswordResetEmail("test@example.com", "https://pawsoft.online/reset?token=abc");
        verify(mailSender).send(any(MimeMessage.class));
    }
}
