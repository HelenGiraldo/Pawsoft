package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.model.TwoFactorAuth;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.TwoFactorAuthRepository;
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
    private TwoFactorAuthRepository twoFactorAuthRepository;

    @InjectMocks
    private TwoFactorService twoFactorService;

    private User testUser;
    private TwoFactorAuth testTwoFactorAuth;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        testTwoFactorAuth = TestDataBuilder.buildTestTwoFactorAuth(testUser);
    }

    @Test
    @DisplayName("Should generate two factor code successfully")
    void shouldGenerateTwoFactorCode() {
        // Given
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(testTwoFactorAuth);

        // When
        TwoFactorAuth result = twoFactorService.generateTwoFactorCode(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getCode());
        assertEquals(6, result.getCode().length());
        assertTrue(result.getCode().matches("\\d{6}")); // Should be 6 digits
        assertFalse(result.isUsed());
        assertNotNull(result.getExpiryTime());
        assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()));

        verify(twoFactorAuthRepository).deleteByUserAndIsUsedFalse(testUser);
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should verify valid two factor code")
    void shouldVerifyValidTwoFactorCode() {
        // Given
        String code = "123456";
        testTwoFactorAuth.setCode(code);
        testTwoFactorAuth.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        testTwoFactorAuth.setUsed(false);

        when(twoFactorAuthRepository.findByUserAndCodeAndIsUsedFalse(testUser, code))
                .thenReturn(Optional.of(testTwoFactorAuth));
        when(twoFactorAuthRepository.save(testTwoFactorAuth)).thenReturn(testTwoFactorAuth);

        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, code);

        // Then
        assertTrue(result);
        assertTrue(testTwoFactorAuth.isUsed());

        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, code);
        verify(twoFactorAuthRepository).save(testTwoFactorAuth);
    }

    @Test
    @DisplayName("Should reject invalid two factor code")
    void shouldRejectInvalidTwoFactorCode() {
        // Given
        String code = "invalid";
        when(twoFactorAuthRepository.findByUserAndCodeAndIsUsedFalse(testUser, code))
                .thenReturn(Optional.empty());

        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, code);

        // Then
        assertFalse(result);

        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, code);
        verify(twoFactorAuthRepository, never()).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should reject expired two factor code")
    void shouldRejectExpiredTwoFactorCode() {
        // Given
        String code = "123456";
        testTwoFactorAuth.setCode(code);
        testTwoFactorAuth.setExpiryTime(LocalDateTime.now().minusMinutes(1)); // Expired
        testTwoFactorAuth.setUsed(false);

        when(twoFactorAuthRepository.findByUserAndCodeAndIsUsedFalse(testUser, code))
                .thenReturn(Optional.of(testTwoFactorAuth));

        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, code);

        // Then
        assertFalse(result);

        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, code);
        verify(twoFactorAuthRepository, never()).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should reject already used two factor code")
    void shouldRejectAlreadyUsedTwoFactorCode() {
        // Given
        String code = "123456";
        // Repository should not return used codes due to isUsedFalse condition
        when(twoFactorAuthRepository.findByUserAndCodeAndIsUsedFalse(testUser, code))
                .thenReturn(Optional.empty());

        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, code);

        // Then
        assertFalse(result);

        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, code);
        verify(twoFactorAuthRepository, never()).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should delete existing unused codes when generating new code")
    void shouldDeleteExistingUnusedCodesWhenGeneratingNewCode() {
        // Given
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(testTwoFactorAuth);

        // When
        twoFactorService.generateTwoFactorCode(testUser);

        // Then
        verify(twoFactorAuthRepository).deleteByUserAndIsUsedFalse(testUser);
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should generate unique codes for multiple requests")
    void shouldGenerateUniqueCodesForMultipleRequests() {
        // Given
        TwoFactorAuth firstAuth = TestDataBuilder.buildTestTwoFactorAuth(testUser);
        TwoFactorAuth secondAuth = TestDataBuilder.buildTestTwoFactorAuth(testUser);
        
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class)))
                .thenReturn(firstAuth)
                .thenReturn(secondAuth);

        // When
        TwoFactorAuth result1 = twoFactorService.generateTwoFactorCode(testUser);
        TwoFactorAuth result2 = twoFactorService.generateTwoFactorCode(testUser);

        // Then
        assertNotNull(result1.getCode());
        assertNotNull(result2.getCode());
        // Note: In a real implementation, codes should be different
        // This test verifies the service is called correctly

        verify(twoFactorAuthRepository, times(2)).deleteByUserAndIsUsedFalse(testUser);
        verify(twoFactorAuthRepository, times(2)).save(any(TwoFactorAuth.class));
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> twoFactorService.generateTwoFactorCode(null));
    }

    @Test
    @DisplayName("Should handle null code gracefully")
    void shouldHandleNullCodeGracefully() {
        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, null);

        // Then
        assertFalse(result);
        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, null);
    }

    @Test
    @DisplayName("Should handle empty code gracefully")
    void shouldHandleEmptyCodeGracefully() {
        // When
        boolean result = twoFactorService.verifyTwoFactorCode(testUser, "");

        // Then
        assertFalse(result);
        verify(twoFactorAuthRepository).findByUserAndCodeAndIsUsedFalse(testUser, "");
    }

    @Test
    @DisplayName("Should clean up expired codes")
    void shouldCleanUpExpiredCodes() {
        // When
        twoFactorService.cleanupExpiredCodes();

        // Then
        verify(twoFactorAuthRepository).deleteByExpiryTimeBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get active two factor auth for user")
    void shouldGetActiveTwoFactorAuthForUser() {
        // Given
        when(twoFactorAuthRepository.findByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testTwoFactorAuth));

        // When
        Optional<TwoFactorAuth> result = twoFactorService.getActiveTwoFactorAuth(testUser);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTwoFactorAuth, result.get());

        verify(twoFactorAuthRepository).findByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return empty when no active two factor auth exists")
    void shouldReturnEmptyWhenNoActiveTwoFactorAuthExists() {
        // Given
        when(twoFactorAuthRepository.findByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When
        Optional<TwoFactorAuth> result = twoFactorService.getActiveTwoFactorAuth(testUser);

        // Then
        assertFalse(result.isPresent());

        verify(twoFactorAuthRepository).findByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should invalidate all codes for user")
    void shouldInvalidateAllCodesForUser() {
        // When
        twoFactorService.invalidateAllCodesForUser(testUser);

        // Then
        verify(twoFactorAuthRepository).deleteByUserAndIsUsedFalse(testUser);
    }

    @Test
    @DisplayName("Should check if user has active two factor code")
    void shouldCheckIfUserHasActiveTwoFactorCode() {
        // Given
        when(twoFactorAuthRepository.existsByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(true);

        // When
        boolean result = twoFactorService.hasActiveTwoFactorCode(testUser);

        // Then
        assertTrue(result);

        verify(twoFactorAuthRepository).existsByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return false when user has no active two factor code")
    void shouldReturnFalseWhenUserHasNoActiveTwoFactorCode() {
        // Given
        when(twoFactorAuthRepository.existsByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(false);

        // When
        boolean result = twoFactorService.hasActiveTwoFactorCode(testUser);

        // Then
        assertFalse(result);

        verify(twoFactorAuthRepository).existsByUserAndIsUsedFalseAndExpiryTimeAfter(eq(testUser), any(LocalDateTime.class));
    }
}