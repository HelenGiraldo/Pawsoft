package co.edu.uniquindio.backendpawsoft.security;

import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities("ROLE_" + testUser.getRole().name())
                .build();

        // Set JWT properties using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "testSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJwtToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(testUser.getEmail(), extractedUsername);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDateFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Date expirationDate = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should extract all claims from token")
    void shouldExtractAllClaimsFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Claims claims = jwtService.extractAllClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Should validate token with correct user details")
    void shouldValidateTokenWithCorrectUserDetails() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate token with incorrect user details")
    void shouldInvalidateTokenWithIncorrectUserDetails() {
        // Given
        String token = jwtService.generateToken(testUser);
        UserDetails wrongUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("wrong@example.com")
                .password("password")
                .authorities("ROLE_CLIENT")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, wrongUserDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should detect non-expired token")
    void shouldDetectNonExpiredToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given - Create token with very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 millisecond
        String token = jwtService.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void shouldThrowExceptionForMalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(malformedToken));
    }

    @Test
    @DisplayName("Should throw exception for token with invalid signature")
    void shouldThrowExceptionForTokenWithInvalidSignature() {
        // Given
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // When & Then
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(tamperedToken));
    }

    @Test
    @DisplayName("Should generate token with custom claims")
    void shouldGenerateTokenWithCustomClaims() {
        // Given
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("role", testUser.getRole().name());
        extraClaims.put("userId", testUser.getId().toString());

        // When
        String token = jwtService.generateToken(extraClaims, testUser);

        // Then
        assertNotNull(token);
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(testUser.getRole().name(), claims.get("role"));
        assertEquals(testUser.getId().toString(), claims.get("userId"));
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void shouldExtractCustomClaimFromToken() {
        // Given
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("role", testUser.getRole().name());
        String token = jwtService.generateToken(extraClaims, testUser);

        // When
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Then
        assertEquals(testUser.getRole().name(), role);
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> jwtService.generateToken(null));
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyTokenGracefully() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(""));
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullTokenGracefully() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(null));
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given
        String token1 = jwtService.generateToken(testUser);
        Thread.sleep(1000); // Wait 1 second
        String token2 = jwtService.generateToken(testUser);

        // Then
        assertNotEquals(token1, token2);
        
        // But both should be valid for the same user
        assertTrue(jwtService.isTokenValid(token1, userDetails));
        assertTrue(jwtService.isTokenValid(token2, userDetails));
    }

    @Test
    @DisplayName("Should extract issued at date from token")
    void shouldExtractIssuedAtDateFromToken() {
        // Given
        Date beforeGeneration = new Date();
        String token = jwtService.generateToken(testUser);
        Date afterGeneration = new Date();

        // When
        Claims claims = jwtService.extractAllClaims(token);
        Date issuedAt = claims.getIssuedAt();

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.after(beforeGeneration) || issuedAt.equals(beforeGeneration));
        assertTrue(issuedAt.before(afterGeneration) || issuedAt.equals(afterGeneration));
    }
}