package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.PasswordResetToken;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PasswordResetTokenRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de recuperación de contraseña.
 * 
 * Valida el flujo completo de recuperación de contraseña:
 * - Solicitud de token de recuperación
 * - Envío de correo con enlace de recuperación
 * - Validación de token (existencia, expiración, uso)
 * - Cambio de contraseña con validación de fortaleza
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Recuperación de Contraseña")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("$2a$10$oldHashedPassword")
                .build();

        testToken = PasswordResetToken.builder()
                .token("valid-token-123")
                .user(testUser)
                .expirationDate(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
    }

    @Test
    @DisplayName("Solicitar recuperación con email válido debe enviar correo")
    void testRequestPasswordResetEmailValido() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // Act
        passwordResetService.requestPasswordReset("test@example.com");

        // Assert
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("Solicitar recuperación con email inexistente no debe lanzar excepción")
    void testRequestPasswordResetEmailInexistente() {
        // Arrange
        when(userRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        passwordResetService.requestPasswordReset("noexiste@example.com");

        // Assert
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Resetear contraseña con token válido debe actualizar usuario")
    void testResetPasswordTokenValido() {
        // Arrange
        String newPassword = "NewPassword123!";
        when(tokenRepository.findByTokenAndUsedFalse("valid-token-123"))
                .thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // Act
        passwordResetService.resetPassword("valid-token-123", newPassword);

        // Assert
        assertTrue(testToken.isUsed());
        verify(userRepository, times(1)).save(testUser);
        verify(tokenRepository, times(1)).save(testToken);
    }

    @Test
    @DisplayName("Resetear contraseña con token inválido debe lanzar excepción")
    void testResetPasswordTokenInvalido() {
        // Arrange
        when(tokenRepository.findByTokenAndUsedFalse("invalid-token"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> passwordResetService.resetPassword("invalid-token", "NewPassword123!")
        );
        assertTrue(exception.getMessage().contains("inválido"));
    }

    @Test
    @DisplayName("Resetear contraseña con token expirado debe lanzar excepción")
    void testResetPasswordTokenExpirado() {
        // Arrange
        testToken.setExpirationDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenAndUsedFalse("valid-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> passwordResetService.resetPassword("valid-token-123", "NewPassword123!")
        );
        assertTrue(exception.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("Resetear con contraseña débil debe lanzar excepción")
    void testResetPasswordContraseñaDebil() {
        // Arrange
        when(tokenRepository.findByTokenAndUsedFalse("valid-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> passwordResetService.resetPassword("valid-token-123", "weak")
        );
        assertTrue(exception.getMessage().contains("seguridad"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Contraseña fuerte debe cumplir todas las reglas")
    void testValidacionContraseñaFuerte() {
        // Arrange
        when(tokenRepository.findByTokenAndUsedFalse("valid-token-123"))
                .thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // Act & Assert - Contraseñas válidas
        assertDoesNotThrow(() -> passwordResetService.resetPassword("valid-token-123", "Password123!"));
    }
}
