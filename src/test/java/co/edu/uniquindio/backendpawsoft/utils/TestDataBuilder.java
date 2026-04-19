package co.edu.uniquindio.backendpawsoft.utils;

import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.RegisterRequest;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.RefreshToken;
import co.edu.uniquindio.backendpawsoft.model.User;

import java.time.LocalDateTime;

/**
 * Utility class for building test data objects.
 */
public class TestDataBuilder {

    public static User buildTestUser() {
        return User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .primerAcceso(false)
                .failedAttempts(0)
                .build();
    }

    public static User buildTestVet() {
        return User.builder()
                .id(2L)
                .name("Dr. Test Vet")
                .email("vet@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.ROLE_VETERINARIO)
                .enabled(true)
                .primerAcceso(false)
                .failedAttempts(0)
                .build();
    }

    public static User buildTestAdmin() {
        return User.builder()
                .id(3L)
                .name("Admin User")
                .email("admin@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .primerAcceso(true)
                .failedAttempts(0)
                .build();
    }

    public static LoginRequest buildLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setRecaptchaToken("test-recaptcha-token");
        return req;
    }

    public static RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .password("Password1!")
                .phone("3001234567")
                .build();
    }

    public static Codigo2FA buildTestCodigo2FA(User user) {
        Codigo2FA codigo = new Codigo2FA();
        codigo.setId(1L);
        codigo.setUsuario(user);
        codigo.setCodigo("123456");
        codigo.setCreadoEn(LocalDateTime.now());
        codigo.setExpiraEn(LocalDateTime.now().plusMinutes(3));
        codigo.setUsado(false);
        codigo.setIntentosFallidos(0);
        codigo.setCantidadReenvios(0);
        codigo.setBloqueosAcumulados(0);
        codigo.setBloqueadoHasta(null);
        return codigo;
    }

    public static RefreshToken buildTestRefreshToken(User user) {
        return RefreshToken.builder()
                .id(1L)
                .tokenHash("hashed-token")
                .userEmail(user.getEmail())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }
}
