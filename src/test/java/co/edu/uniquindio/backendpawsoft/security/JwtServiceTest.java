package co.edu.uniquindio.backendpawsoft.security;

import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private UserDetails userDetails;

    // A valid Base64-encoded 256-bit key for HS256
    private static final String SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvblRoYXRJc0xvbmdFbm91Z2g=";

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(testUser.getRole().name())
                .build();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        String token = jwtService.generateToken(testUser);

        assertEquals(testUser.getEmail(), jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRole() {
        String token = jwtService.generateToken(testUser);

        assertEquals(testUser.getRole().name(), jwtService.extractRole(token));
    }

    @Test
    @DisplayName("Should validate token for correct user")
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(testUser);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Should invalidate token for wrong user")
    void shouldInvalidateTokenForWrongUser() {
        String token = jwtService.generateToken(testUser);
        UserDetails wrongUser = org.springframework.security.core.userdetails.User.builder()
                .username("wrong@example.com")
                .password("pass")
                .authorities("ROLE_CLIENTE")
                .build();

        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(testUser);
        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class,
                () -> jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser.getEmail());

        assertNotNull(refreshToken);
        assertEquals(3, refreshToken.split("\\.").length);
        assertEquals(testUser.getEmail(), jwtService.extractUsername(refreshToken));
    }

    @Test
    @DisplayName("Should validate valid refresh token")
    void shouldValidateValidRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser.getEmail());

        assertTrue(jwtService.isRefreshTokenValid(refreshToken));
    }

    @Test
    @DisplayName("Should return false for invalid refresh token")
    void shouldReturnFalseForInvalidRefreshToken() {
        assertFalse(jwtService.isRefreshTokenValid("invalid.token.here"));
    }

    @Test
    @DisplayName("Should generate different tokens at different times")
    void shouldGenerateDifferentTokensAtDifferentTimes() throws InterruptedException {
        String token1 = jwtService.generateToken(testUser);
        Thread.sleep(1000);
        String token2 = jwtService.generateToken(testUser);

        assertNotEquals(token1, token2);
        assertTrue(jwtService.isTokenValid(token1, userDetails));
        assertTrue(jwtService.isTokenValid(token2, userDetails));
    }
}
