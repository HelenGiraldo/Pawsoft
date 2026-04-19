package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.ProfileUpdateRequest;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private TwoFactorService twoFactorService;
    @Mock private EmailService emailService;
    @Mock private AuditLogService auditLogService;
    @Mock private PetRepository petRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private Codigo2FA codigo2FA;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Juan Pérez")
                .email("juan@test.com")
                .phone("3001234567")
                .password("$2a$10$hashedPassword")
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .build();

        codigo2FA = new Codigo2FA();
        codigo2FA.setId(1L);
        codigo2FA.setCodigo("123456");
        codigo2FA.setUsuario(user);
    }

    // ── requestVerification ───────────────────────────────────────────────────

    @Test
    @DisplayName("Should request verification and send 2FA code")
    void shouldRequestVerificationAndSendCode() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(twoFactorService.crearCodigo(user, null)).thenReturn(codigo2FA);

        profileService.requestVerification("juan@test.com");

        verify(twoFactorService).crearCodigo(user, null);
        verify(emailService).enviarCodigo2FA("juan@test.com", "123456");
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found on requestVerification")
    void shouldThrowWhenUserNotFoundOnRequest() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> profileService.requestVerification("unknown@test.com"));
        verifyNoInteractions(twoFactorService);
    }

    // ── verifyAndSave ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update phone with valid code")
    void shouldUpdatePhoneWithValidCode() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setCode("123456");
        req.setPhone("3009876543");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(user, "123456", null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.verifyAndSave("juan@test.com", req);

        verify(userRepository).save(user);
        verify(auditLogService).log(eq("PROFILE_UPDATE_PHONE"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should update email with valid code when new email is free")
    void shouldUpdateEmailWithValidCode() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setCode("123456");
        req.setEmail("nuevo@test.com");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        doNothing().when(twoFactorService).validarCodigo(user, "123456", null);
        when(petRepository.findByOwnerEmail("juan@test.com")).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.verifyAndSave("juan@test.com", req);

        verify(userRepository).save(user);
        verify(auditLogService).log(eq("PROFILE_UPDATE_EMAIL"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when new email is already in use")
    void shouldThrowWhenNewEmailAlreadyInUse() {
        User otherUser = User.builder().id(2L).email("nuevo@test.com").build();
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setCode("123456");
        req.setEmail("nuevo@test.com");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.of(otherUser));
        doNothing().when(twoFactorService).validarCodigo(user, "123456", null);

        assertThrows(UnauthorizedException.class,
                () -> profileService.verifyAndSave("juan@test.com", req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update password with valid code and strong password")
    void shouldUpdatePasswordWithValidCode() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setCode("123456");
        req.setCurrentPassword("OldPass1!");
        req.setNewPassword("NuevaPass123!");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(user, "123456", null);
        when(passwordEncoder.matches("OldPass1!", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NuevaPass123!")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.verifyAndSave("juan@test.com", req);

        verify(passwordEncoder).encode("NuevaPass123!");
        verify(auditLogService).log(eq("PROFILE_UPDATE_PASSWORD"), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw when new password is weak")
    void shouldThrowWhenNewPasswordIsWeak() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setCode("123456");
        req.setCurrentPassword("OldPass1!");
        req.setNewPassword("weak");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(user, "123456", null);
        when(passwordEncoder.matches("OldPass1!", user.getPassword())).thenReturn(true);

        assertThrows(UnauthorizedException.class,
                () -> profileService.verifyAndSave("juan@test.com", req));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return profile data")
    void shouldReturnProfileData() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        Map<String, String> profile = profileService.getProfile("juan@test.com");

        assertNotNull(profile);
        assertEquals("Juan Pérez", profile.get("name"));
        assertEquals("juan@test.com", profile.get("email"));
        assertEquals("3001234567", profile.get("phone"));
    }

    @Test
    @DisplayName("Should return empty phone when null")
    void shouldReturnEmptyPhoneWhenNull() {
        user.setPhone(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        Map<String, String> profile = profileService.getProfile("juan@test.com");

        assertEquals("", profile.get("phone"));
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found on getProfile")
    void shouldThrowWhenUserNotFoundOnGetProfile() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> profileService.getProfile("unknown@test.com"));
    }
}
