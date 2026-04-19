package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private TwoFactorService twoFactorService;
    @Mock private EmailService emailService;
    @Mock private RecaptchaService recaptchaService;
    @Mock private AuditLogService auditLogService;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private static final String IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        loginRequest = TestDataBuilder.buildLoginRequest();
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should send 2FA code on successful login")
    void shouldSend2FACodeOnSuccessfulLogin() {
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        Codigo2FA codigo = TestDataBuilder.buildTestCodigo2FA(testUser);
        when(twoFactorService.crearCodigo(testUser, IP)).thenReturn(codigo);

        LoginResponse response = authService.login(loginRequest, IP);

        assertNotNull(response);
        assertNull(response.getToken());
        verify(emailService).enviarCodigo2FA(testUser.getEmail(), codigo.getCodigo());
        verify(auditLogService).log(eq("USER_LOGIN"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when reCAPTCHA fails")
    void shouldThrowWhenRecaptchaFails() {
        when(recaptchaService.isValid(anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, IP));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.login(loginRequest, IP));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password is wrong")
    void shouldThrowWhenPasswordIsWrong() {
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, IP));
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    @Test
    @DisplayName("Should throw RuntimeException when email not verified")
    void shouldThrowWhenEmailNotVerified() {
        testUser.setEnabled(false);
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest, IP));
    }

    // ── verifyCode ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return JWT on valid 2FA code")
    void shouldReturnJwtOnValid2FACode() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        doNothing().when(twoFactorService).validarCodigo(testUser, "123456", IP);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(testUser.getEmail(), "web")).thenReturn("refresh-token");

        LoginResponse response = authService.verifyCode(testUser.getEmail(), "123456", IP);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(testUser.getRole().name(), response.getRole());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found on verify")
    void shouldThrowWhenUserNotFoundOnVerify() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.verifyCode("unknown@test.com", "123456", IP));
    }

    @Test
    @DisplayName("Should set mustChangePassword for staff on first access")
    void shouldSetMustChangePasswordForStaffFirstAccess() {
        User staffUser = TestDataBuilder.buildTestVet();
        staffUser.setPrimerAcceso(true);

        when(userRepository.findByEmail(staffUser.getEmail())).thenReturn(Optional.of(staffUser));
        doNothing().when(twoFactorService).validarCodigo(staffUser, "123456", IP);
        when(jwtService.generateToken(staffUser)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(staffUser.getEmail(), "web")).thenReturn("refresh-token");

        LoginResponse response = authService.verifyCode(staffUser.getEmail(), "123456", IP);

        assertTrue(response.isMustChangePassword());
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return new tokens on valid refresh token")
    void shouldReturnNewTokensOnValidRefreshToken() {
        when(refreshTokenService.validateRefreshToken("old-refresh")).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("new-jwt");
        when(refreshTokenService.createRefreshToken(testUser.getEmail(), "web")).thenReturn("new-refresh");

        LoginResponse response = authService.refreshToken("old-refresh");

        assertNotNull(response);
        assertEquals("new-jwt", response.getToken());
        assertEquals("new-refresh", response.getRefreshToken());
        verify(refreshTokenService).revokeRefreshToken("old-refresh");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void shouldRevokeRefreshTokenOnLogout() {
        authService.logout("some-refresh-token");
        verify(refreshTokenService).revokeRefreshToken("some-refresh-token");
    }

    // ── changePasswordFirstLogin ──────────────────────────────────────────────

    @Test
    @DisplayName("Should change password on first login")
    void shouldChangePasswordOnFirstLogin() {
        User staffUser = TestDataBuilder.buildTestVet();
        staffUser.setPrimerAcceso(true);

        when(userRepository.findByEmail(staffUser.getEmail())).thenReturn(Optional.of(staffUser));
        when(passwordEncoder.matches("NewPass1!", staffUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded-new");
        when(jwtService.generateToken(staffUser)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(staffUser.getEmail(), "web")).thenReturn("refresh-token");

        LoginResponse response = authService.changePasswordFirstLogin(staffUser.getEmail(), "NewPass1!");

        assertNotNull(response);
        assertFalse(response.isMustChangePassword());
        assertFalse(staffUser.isPrimerAcceso());
        verify(userRepository).save(staffUser);
    }

    @Test
    @DisplayName("Should throw when user already changed password")
    void shouldThrowWhenAlreadyChangedPassword() {
        testUser.setPrimerAcceso(false);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class,
                () -> authService.changePasswordFirstLogin(testUser.getEmail(), "NewPass1!"));
    }

    @Test
    @DisplayName("Should throw when new password is weak")
    void shouldThrowWhenNewPasswordIsWeak() {
        User staffUser = TestDataBuilder.buildTestVet();
        staffUser.setPrimerAcceso(true);

        when(userRepository.findByEmail(staffUser.getEmail())).thenReturn(Optional.of(staffUser));

        assertThrows(UnauthorizedException.class,
                () -> authService.changePasswordFirstLogin(staffUser.getEmail(), "weak"));
    }
}
