package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de autenticación de dos factores.
 * 
 * Valida la generación, validación y políticas de seguridad de códigos 2FA:
 * - Generación de códigos aleatorios de 6 dígitos
 * - Validación de códigos con control de intentos fallidos
 * - Bloqueo temporal tras múltiples intentos incorrectos
 * - Expiración de códigos después del tiempo configurado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Autenticación 2FA")
class TwoFactorServiceTest {

    @Mock
    private Codigo2FARepository codigo2FARepository;

    @InjectMocks
    private TwoFactorService twoFactorService;

    private User testUser;
    private Codigo2FA testCodigo;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testCodigo = new Codigo2FA();
        testCodigo.setId(1L);
        testCodigo.setUsuario(testUser);
        testCodigo.setCodigo("123456");
        testCodigo.setCreadoEn(LocalDateTime.now());
        testCodigo.setExpiraEn(LocalDateTime.now().plusMinutes(10));
        testCodigo.setUsado(false);
        testCodigo.setIntentosFallidos(0);
    }

    @Test
    @DisplayName("Generar código debe retornar 6 dígitos")
    void testGenerateCode() {
        // Act
        String codigo = twoFactorService.generateCode();

        // Assert
        assertNotNull(codigo);
        assertEquals(6, codigo.length());
        assertTrue(codigo.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Crear código debe invalidar códigos anteriores")
    void testCrearCodigo() {
        // Arrange
        Codigo2FA codigoAnterior = new Codigo2FA();
        codigoAnterior.setUsado(false);
        
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(codigoAnterior));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Codigo2FA nuevoCodigo = twoFactorService.crearCodigo(testUser);

        // Assert
        assertNotNull(nuevoCodigo);
        assertNotNull(nuevoCodigo.getCodigo());
        assertEquals(6, nuevoCodigo.getCodigo().length());
        assertFalse(nuevoCodigo.isUsado());
        assertEquals(0, nuevoCodigo.getIntentosFallidos());
        verify(codigo2FARepository, times(2)).save(any(Codigo2FA.class));
    }

    @Test
    @DisplayName("Validar código correcto debe marcarlo como usado")
    void testValidarCodigoCorrecto() {
        // Arrange
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        // Act
        twoFactorService.validarCodigo(testUser, "123456");

        // Assert
        assertTrue(testCodigo.isUsado());
        verify(codigo2FARepository, times(1)).save(testCodigo);
    }

    @Test
    @DisplayName("Validar código incorrecto debe incrementar intentos fallidos")
    void testValidarCodigoIncorrecto() {
        // Arrange
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "999999")
        );
        
        assertEquals(1, testCodigo.getIntentosFallidos());
        assertTrue(exception.getMessage().contains("Intentos restantes: 2"));
        verify(codigo2FARepository, times(1)).save(testCodigo);
    }

    @Test
    @DisplayName("Validar código con máximos intentos debe bloquear")
    void testValidarCodigoMaximosIntentos() {
        // Arrange
        testCodigo.setIntentosFallidos(2);
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "999999")
        );
        
        assertEquals(3, testCodigo.getIntentosFallidos());
        assertNotNull(testCodigo.getBloqueadoHasta());
        assertTrue(exception.getMessage().contains("Demasiados intentos"));
    }

    @Test
    @DisplayName("Validar código expirado debe lanzar excepción")
    void testValidarCodigoExpirado() {
        // Arrange
        testCodigo.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456")
        );
        
        assertTrue(exception.getMessage().contains("expirado"));
        assertTrue(testCodigo.isUsado());
    }

    @Test
    @DisplayName("Validar sin código activo debe lanzar excepción")
    void testValidarSinCodigoActivo() {
        // Arrange
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456")
        );
        
        assertTrue(exception.getMessage().contains("No existe un código activo"));
    }

    @Test
    @DisplayName("Validar código bloqueado debe lanzar excepción")
    void testValidarCodigoBloqueado() {
        // Arrange
        testCodigo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(1));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser))
                .thenReturn(Optional.of(testCodigo));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456")
        );
        
        assertTrue(exception.getMessage().contains("Demasiados intentos"));
    }
}
