package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de autenticación.
 * 
 * Valida el comportamiento del flujo de login en dos fases:
 * 1. Validación de credenciales y generación de código 2FA
 * 2. Verificación del código 2FA y emisión del token JWT
 * 
 * También prueba casos de error como credenciales inválidas,
 * cuentas bloqueadas y cambio de contraseña en primer acceso.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Autenticación")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        testUser = User.builder()
                .id(1L)
                .name("Usuario Test")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .failedAttempts(0)
                .primerAcceso(false)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Login exitoso debe generar código 2FA y enviarlo por correo")
    void testLoginExitoso() {
        // Arrange
        Codigo2FA codigo = new Codigo2FA();
        codigo.setCodigo("123456");
        
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(twoFactorService.crearCodigo(testUser)).thenReturn(codigo);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Código de verificación enviado al correo", response.getMessage());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertNull(response.getToken());
        verify(emailService, times(1)).enviarCodigo2FA(testUser.getEmail(), "123456");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Login con usuario inexistente debe lanzar NotFoundException")
    void testLoginUsuarioInexistente() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> authService.login(loginRequest));
        verify(twoFactorService, never()).crearCodigo(any());
        verify(emailService, never()).enviarCodigo2FA(anyString(), anyString());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe incrementar intentos fallidos")
    void testLoginContraseñaIncorrecta() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
        assertEquals(1, testUser.getFailedAttempts());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Login con cuenta bloqueada debe lanzar UnauthorizedException")
    void testLoginCuentaBloqueada() {
        // Arrange
        testUser.setLockTime(LocalDateTime.now());
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(loginRequest)
        );
        assertTrue(exception.getMessage().contains("bloqueada temporalmente"));
    }

    @Test
    @DisplayName("Verificación de código 2FA exitosa debe retornar token JWT")
    void testVerifyCodeExitoso() {
        // Arrange
        String codigo = "123456";
        String token = "jwt.token.here";
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        doNothing().when(twoFactorService).validarCodigo(testUser, codigo);
        when(jwtService.generateToken(testUser)).thenReturn(token);

        // Act
        LoginResponse response = authService.verifyCode(testUser.getEmail(), codigo);

        // Assert
        assertNotNull(response);
        assertEquals("Autenticación completa", response.getMessage());
        assertEquals(token, response.getToken());
        assertFalse(response.isMustChangePassword());
    }

    @Test
    @DisplayName("Cambio de contraseña en primer acceso debe actualizar usuario")
    void testChangePasswordFirstLogin() {
        // Arrange
        testUser.setPrimerAcceso(true);
        testUser.setRole(Role.ROLE_VETERINARIO);
        String newPassword = "NewPass123!";
        String token = "jwt.token.here";
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newHashedPassword");
        when(jwtService.generateToken(testUser)).thenReturn(token);

        // Act
        LoginResponse response = authService.changePasswordFirstLogin(testUser.getEmail(), newPassword);

        // Assert
        assertNotNull(response);
        assertEquals("Contraseña cambiada exitosamente", response.getMessage());
        assertEquals(token, response.getToken());
        assertFalse(testUser.isPrimerAcceso());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Cambio de contraseña con contraseña débil debe lanzar excepción")
    void testChangePasswordContraseñaDebil() {
        // Arrange
        testUser.setPrimerAcceso(true);
        String weakPassword = "123";
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.changePasswordFirstLogin(testUser.getEmail(), weakPassword)
        );
        assertTrue(exception.getMessage().contains("mínimo 8 caracteres"));
    }

    @Test
    @DisplayName("Reenvío de código 2FA debe generar nuevo código")
    void testResend2FACode() {
        // Arrange
        Codigo2FA codigo = new Codigo2FA();
        codigo.setCodigo("654321");
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(twoFactorService.crearCodigo(testUser)).thenReturn(codigo);

        // Act
        LoginResponse response = authService.resend2FACode(testUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals("Código de verificación reenviado al correo", response.getMessage());
        verify(emailService, times(1)).enviarCodigo2FA(testUser.getEmail(), "654321");
    }

    @Test
    @DisplayName("Login con usuario no verificado debe lanzar excepción")
    void testLoginUsuarioNoVerificado() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(loginRequest)
        );
        assertTrue(exception.getMessage().contains("verificar tu correo"));
    }
}
