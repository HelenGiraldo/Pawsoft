package co.edu.uniquindio.backendpawsoft.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de JWT.
 * 
 * Valida la generación y validación de tokens JWT:
 * - Generación de tokens con claims personalizados (rol)
 * - Extracción de información del token (username, rol)
 * - Validación de tokens (firma, expiración, usuario)
 * - Manejo de tokens expirados o inválidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio JWT")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails testUserDetails;
    private String secretKey;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        // Configurar clave secreta y tiempo de expiración
        secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        jwtExpiration = 86400000; // 24 horas

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);

        // Crear usuario de prueba
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CLIENTE");
        testUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(authority))
                .build();
    }

    @Test
    @DisplayName("Generar token debe crear JWT válido")
    void testGenerateToken() {
        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes
    }

    @Test
    @DisplayName("Extraer username del token debe retornar email correcto")
    void testExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Extraer rol del token debe retornar rol correcto")
    void testExtractRole() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        String role = jwtService.extractRole(token);

        // Assert
        assertEquals("ROLE_CLIENTE", role);
    }

    @Test
    @DisplayName("Validar token válido debe retornar true")
    void testIsTokenValid() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validar token con usuario diferente debe retornar false")
    void testIsTokenValidUsuarioDiferente() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        
        UserDetails otroUsuario = User.builder()
                .username("otro@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Token debe contener fecha de emisión y expiración")
    void testTokenContieneFechas() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        
        // Decodificar token manualmente para verificar claims
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Assert
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    @DisplayName("Token expirado debe ser inválido")
    void testTokenExpirado() throws InterruptedException {
        // Arrange - Crear token con expiración de 1 segundo
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L);
        String token = jwtService.generateToken(testUserDetails);
        
        // Esperar a que expire
        Thread.sleep(1500);
        
        // Restaurar expiración normal
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);

        // Act & Assert - El token expirado debe lanzar excepción o retornar false
        try {
            boolean isValid = jwtService.isTokenValid(token, testUserDetails);
            assertFalse(isValid);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Esto es esperado para tokens expirados
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Token debe incluir rol como claim personalizado")
    void testTokenIncluyeRol() {
        // Arrange
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_VETERINARIO");
        UserDetails vet = User.builder()
                .username("vet@example.com")
                .password("password")
                .authorities(Collections.singletonList(authority))
                .build();

        // Act
        String token = jwtService.generateToken(vet);
        String role = jwtService.extractRole(token);

        // Assert
        assertEquals("ROLE_VETERINARIO", role);
    }

    @Test
    @DisplayName("Generar múltiples tokens debe crear tokens únicos")
    void testGenerarMultiplesTokens() throws InterruptedException {
        // Act
        String token1 = jwtService.generateToken(testUserDetails);
        Thread.sleep(1000); // Pausa de 1 segundo para asegurar diferente timestamp
        String token2 = jwtService.generateToken(testUserDetails);

        // Assert
        assertNotEquals(token1, token2);
    }
}
