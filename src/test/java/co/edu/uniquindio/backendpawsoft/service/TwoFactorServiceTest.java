package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.enums.ResultadoCodigo;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("TwoFactorService Tests")
class TwoFactorServiceTest {

    @Mock
    private Codigo2FARepository codigo2FARepository;

    @InjectMocks
    private TwoFactorService twoFactorService;

    private User testUser;
    private Codigo2FA testCodigo;
    private static final String IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        testCodigo = TestDataBuilder.buildTestCodigo2FA(testUser);
    }

    // ── generateCode ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should generate a 6-digit numeric code")
    void shouldGenerateSixDigitCode() {
        String code = twoFactorService.generateCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Should generate different codes on multiple calls")
    void shouldGenerateDifferentCodes() {
        // With 1,000,000 possible values, two consecutive calls are very unlikely to match
        long distinctCount = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> twoFactorService.generateCode())
                .distinct()
                .count();
        assertTrue(distinctCount > 1);
    }

    // ── crearCodigo ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create new code when no active code exists")
    void shouldCreateNewCodeWhenNoActiveCodeExists() {
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.empty());
        when(codigo2FARepository.findTopByUsuarioOrderByIdDesc(testUser)).thenReturn(Optional.empty());
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        Codigo2FA result = twoFactorService.crearCodigo(testUser, IP);

        assertNotNull(result);
        verify(codigo2FARepository).save(any(Codigo2FA.class));
    }

    @Test
    @DisplayName("Should throw when cooldown between resends not elapsed")
    void shouldThrowWhenCooldownNotElapsed() {
        // Active code created just now — cooldown not elapsed
        testCodigo.setCreadoEn(LocalDateTime.now());
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.crearCodigo(testUser, IP));
    }

    @Test
    @DisplayName("Should invalidate old code and create new one after cooldown")
    void shouldInvalidateOldCodeAndCreateNewAfterCooldown() {
        // Active code created 2 minutes ago — cooldown elapsed
        testCodigo.setCreadoEn(LocalDateTime.now().minusSeconds(61));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.findTopByUsuarioOrderByIdDesc(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        twoFactorService.crearCodigo(testUser, IP);

        // Old code should be saved as INVALIDADO
        verify(codigo2FARepository, atLeast(2)).save(any(Codigo2FA.class));
        assertEquals(ResultadoCodigo.INVALIDADO, testCodigo.getResultado());
    }

    @Test
    @DisplayName("Should throw when active block exists")
    void shouldThrowWhenActiveBlockExists() {
        testCodigo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.crearCodigo(testUser, IP));
    }

    // ── validarCodigo ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should validate correct code successfully")
    void shouldValidateCorrectCode() {
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        assertDoesNotThrow(() -> twoFactorService.validarCodigo(testUser, "123456", IP));

        assertTrue(testCodigo.isUsado());
        assertEquals(ResultadoCodigo.EXITOSO, testCodigo.getResultado());
    }

    @Test
    @DisplayName("Should throw when no active code exists")
    void shouldThrowWhenNoActiveCodeExists() {
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456", IP));
    }

    @Test
    @DisplayName("Should throw when code is expired")
    void shouldThrowWhenCodeIsExpired() {
        testCodigo.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456", IP));

        assertEquals(ResultadoCodigo.EXPIRADO, testCodigo.getResultado());
    }

    @Test
    @DisplayName("Should throw and record FALLIDO when code is wrong")
    void shouldThrowAndRecordFailedWhenCodeIsWrong() {
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "000000", IP));

        assertEquals(ResultadoCodigo.FALLIDO, testCodigo.getResultado());
        assertEquals(1, testCodigo.getIntentosFallidos());
    }

    @Test
    @DisplayName("Should apply block after max failed attempts")
    void shouldApplyBlockAfterMaxFailedAttempts() {
        testCodigo.setIntentosFallidos(2); // one more will hit MAX_INTENTOS=3
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));
        when(codigo2FARepository.save(any(Codigo2FA.class))).thenReturn(testCodigo);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "000000", IP));

        assertTrue(ex.getMessage().contains("minuto"));
        assertNotNull(testCodigo.getBloqueadoHasta());
    }

    @Test
    @DisplayName("Should throw when block is still active on validate")
    void shouldThrowWhenBlockActiveOnValidate() {
        testCodigo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
        when(codigo2FARepository.findByUsuarioAndUsadoFalse(testUser)).thenReturn(Optional.of(testCodigo));

        assertThrows(UnauthorizedException.class,
                () -> twoFactorService.validarCodigo(testUser, "123456", IP));
    }
}
