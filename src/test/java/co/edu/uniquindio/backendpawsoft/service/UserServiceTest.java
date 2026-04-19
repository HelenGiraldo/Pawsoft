package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.EmailVerificationToken;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.EmailVerificationTokenRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private RecaptchaService recaptchaService;
    @Mock private AuditLogService auditLogService;
    @Mock private PetRepository petRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, TestDataBuilder.buildTestVet()));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return user by ID")
    void shouldReturnUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw when user not found by ID")
    void shouldThrowWhenUserNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        UserRequest req = buildUserRequest();
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(new EmailVerificationToken());

        UserResponse result = userService.createUser(req);

        assertNotNull(result);
        verify(emailService).sendVerificationEmail(anyString(), anyString());
        verify(auditLogService).log(eq("USER_CREATE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when reCAPTCHA fails on create")
    void shouldThrowWhenRecaptchaFailsOnCreate() {
        UserRequest req = buildUserRequest();
        when(recaptchaService.isValid(anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.createUser(req));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw when email already registered")
    void shouldThrowWhenEmailAlreadyRegistered() {
        UserRequest req = buildUserRequest();
        when(recaptchaService.isValid(anyString())).thenReturn(true);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(req));
        verify(userRepository, never()).save(any());
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUserById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        verify(auditLogService).log(eq("USER_DELETE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when deleting non-existent user")
    void shouldThrowWhenDeletingNonExistentUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(any());
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        UserRequest req = buildUserRequest();
        req.setEmail(testUser.getEmail()); // same email, no conflict

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser(1L, req);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw when updating with weak password")
    void shouldThrowWhenUpdatingWithWeakPassword() {
        UserRequest req = buildUserRequest();
        req.setPassword("weak");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, req));
    }

    // ── isPasswordStrong ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return true for strong password")
    void shouldReturnTrueForStrongPassword() {
        assertTrue(userService.isPasswordStrong("Password1!"));
    }

    @Test
    @DisplayName("Should return false for weak password")
    void shouldReturnFalseForWeakPassword() {
        assertFalse(userService.isPasswordStrong("weak"));
        assertFalse(userService.isPasswordStrong("alllowercase1!"));
        assertFalse(userService.isPasswordStrong("ALLUPPERCASE1!"));
        assertFalse(userService.isPasswordStrong("NoSpecial1"));
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should load user by username (email)")
    void shouldLoadUserByUsername() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        var result = userService.loadUserByUsername(testUser.getEmail());

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getUsername());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown@test.com"));
    }

    // ── createStaffUser ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create staff user and send temporary password")
    void shouldCreateStaffUserAndSendTemporaryPassword() {
        when(userRepository.findByEmail("vet@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(TestDataBuilder.buildTestVet());

        UserResponse result = userService.createStaffUser("Dr. Test Vet", "vet@example.com", Role.ROLE_VETERINARIO);

        assertNotNull(result);
        verify(emailService).sendTemporaryPassword(eq("vet@example.com"), anyString());
        verify(auditLogService).log(eq("STAFF_CREATE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when staff email already registered")
    void shouldThrowWhenStaffEmailAlreadyRegistered() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createStaffUser("Test", testUser.getEmail(), Role.ROLE_VETERINARIO));
    }

    // ── verifyEmail ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should verify email with valid token")
    void shouldVerifyEmailWithValidToken() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token("valid-token")
                .user(testUser)
                .expirationDate(LocalDateTime.now().plusHours(1))
                .build();

        when(emailVerificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.verifyEmail("valid-token");

        assertTrue(testUser.isEnabled());
        verify(emailVerificationTokenRepository).delete(token);
    }

    @Test
    @DisplayName("Should throw when verification token is invalid")
    void shouldThrowWhenVerificationTokenIsInvalid() {
        when(emailVerificationTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.verifyEmail("bad-token"));
    }

    @Test
    @DisplayName("Should throw when verification token is expired")
    void shouldThrowWhenVerificationTokenIsExpired() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token("expired-token")
                .user(testUser)
                .expirationDate(LocalDateTime.now().minusHours(1))
                .build();

        when(emailVerificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () -> userService.verifyEmail("expired-token"));
    }

    // ── reenviarVerificacion ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should resend verification email")
    void shouldResendVerificationEmail() {
        testUser.setEnabled(false);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(emailVerificationTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(new EmailVerificationToken());

        userService.reenviarVerificacion(testUser.getEmail());

        verify(emailService).sendVerificationEmail(eq(testUser.getEmail()), anyString());
    }

    @Test
    @DisplayName("Should do nothing when user not found on resend")
    void shouldDoNothingWhenUserNotFoundOnResend() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Should not throw — silent response
        assertDoesNotThrow(() -> userService.reenviarVerificacion("unknown@test.com"));
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should do nothing when user already verified")
    void shouldDoNothingWhenUserAlreadyVerified() {
        testUser.setEnabled(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        userService.reenviarVerificacion(testUser.getEmail());

        verifyNoInteractions(emailService);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserRequest buildUserRequest() {
        UserRequest req = new UserRequest();
        req.setName("Test User");
        req.setEmail("newuser@example.com");
        req.setPassword("Password1!");
        req.setPhone("3001234567");
        req.setRecaptchaToken("test-token");
        return req;
    }
}
