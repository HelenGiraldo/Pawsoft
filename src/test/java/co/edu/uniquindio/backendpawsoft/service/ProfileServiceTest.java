package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.ProfileUpdateRequest;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ProfileService.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Perfil")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

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

    @Test
    @DisplayName("Debería solicitar verificación y enviar código 2FA")
    void deberiaSolicitarVerificacionYEnviarCodigo() {
        // Arrange
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(twoFactorService.crearCodigo(any(User.class), isNull())).thenReturn(codigo2FA);

        // Act
        profileService.requestVerification("juan@test.com");

        // Assert
        verify(userRepository).findByEmail("juan@test.com");
        verify(twoFactorService).crearCodigo(user, null);
        verify(emailService).enviarCodigo2FA("juan@test.com", "123456");
    }

    @Test
    @DisplayName("No debería solicitar verificación si usuario no existe")
    void noDeberiaSolicitarVerificacionSiUsuarioNoExiste() {
        // Arrange
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                profileService.requestVerification("noexiste@test.com")
        );
        verify(twoFactorService, never()).crearCodigo(any(), any());
    }

    @Test
    @DisplayName("Debería actualizar email con código válido")
    void deberiaActualizarEmailConCodigoValido() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setCode("123456");
        request.setEmail("nuevo@test.com");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        doNothing().when(twoFactorService).validarCodigo(any(User.class), anyString(), isNull());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        profileService.verifyAndSave("juan@test.com", request);

        // Assert
        verify(twoFactorService).validarCodigo(user, "123456", null);
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq("PROFILE_UPDATE_EMAIL"), anyString(), eq("USER"), eq(1));
    }

    @Test
    @DisplayName("No debería actualizar email si ya está en uso")
    void noDeberiaActualizarEmailSiYaEstaEnUso() {
        // Arrange
        User otroUsuario = User.builder().id(2L).email("nuevo@test.com").build();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setCode("123456");
        request.setEmail("nuevo@test.com");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.of(otroUsuario));
        doNothing().when(twoFactorService).validarCodigo(any(User.class), anyString(), isNull());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                profileService.verifyAndSave("juan@test.com", request)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería actualizar teléfono con código válido")
    void deberiaActualizarTelefonoConCodigoValido() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setCode("123456");
        request.setPhone("3009876543");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(any(User.class), anyString(), isNull());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        profileService.verifyAndSave("juan@test.com", request);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq("PROFILE_UPDATE_PHONE"), anyString(), eq("USER"), eq(1));
    }

    @Test
    @DisplayName("Debería actualizar contraseña con código válido y contraseña fuerte")
    void deberiaActualizarContrasenaConCodigoValidoYContrasenaFuerte() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setCode("123456");
        request.setNewPassword("NuevaPass123!");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(any(User.class), anyString(), isNull());
        when(passwordEncoder.encode("NuevaPass123!")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        profileService.verifyAndSave("juan@test.com", request);

        // Assert
        verify(passwordEncoder).encode("NuevaPass123!");
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq("PROFILE_UPDATE_PASSWORD"), anyString(), eq("USER"), eq(1));
    }

    @Test
    @DisplayName("No debería actualizar contraseña si no es fuerte")
    void noDeberiaActualizarContrasenaSiNoEsFuerte() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setCode("123456");
        request.setNewPassword("debil");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        doNothing().when(twoFactorService).validarCodigo(any(User.class), anyString(), isNull());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                profileService.verifyAndSave("juan@test.com", request)
        );
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería obtener perfil del usuario")
    void deberiaObtenerPerfilDelUsuario() {
        // Arrange
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        // Act
        Map<String, String> profile = profileService.getProfile("juan@test.com");

        // Assert
        assertNotNull(profile);
        assertEquals("Juan Pérez", profile.get("name"));
        assertEquals("juan@test.com", profile.get("email"));
        assertEquals("3001234567", profile.get("phone"));
        verify(userRepository).findByEmail("juan@test.com");
    }

    @Test
    @DisplayName("Debería retornar teléfono vacío si es null")
    void deberiaRetornarTelefonoVacioSiEsNull() {
        // Arrange
        user.setPhone(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        // Act
        Map<String, String> profile = profileService.getProfile("juan@test.com");

        // Assert
        assertEquals("", profile.get("phone"));
    }

    @Test
    @DisplayName("No debería obtener perfil si usuario no existe")
    void noDeberiaObtenerPerfilSiUsuarioNoExiste() {
        // Arrange
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                profileService.getProfile("noexiste@test.com")
        );
    }
}
