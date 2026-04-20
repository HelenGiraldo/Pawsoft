package co.edu.uniquindio.backendpawsoft.security;

/**
 * Servicio para la gestión de tokens JWT (JSON Web Tokens).
 * Maneja la generación, validación y extracción de información de los tokens de autenticación.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * - Raúl Yulbraynner Rivera Gálvez
 */
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Servicio encargado de generar y validar tokens JWT dentro del sistema.
 *
 * Este servicio centraliza la creación de tokens firmados (HS256) y la lectura de claims
 * para soportar la autenticación sin sesión (stateless) en Spring Security.
 *
 * Funcionalidades principales:
 * - Generar un token para un usuario autenticado, incluyendo el rol como claim.
 * - Validar un token (usuario correcto y no expirado).
 * - Extraer información relevante del token como username y rol.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Service
public class JwtService {

    /**
     * Clave secreta configurada en application.properties.
     * Se espera en Base64 para poder decodificarla y generar el {@link Key}.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Tiempo de expiración del token en milisegundos, configurado en application.properties.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Tiempo de expiración del refresh token en milisegundos (7 días).
     */
    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpiration;

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * El token incluye:
     * - subject: username (email) del usuario
     * - claim "role": autoridad del usuario
     * - issuedAt: fecha de generación
     * - expiration: fecha de expiración
     *
     * @param userDetails usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .claim("role",
                        userDetails.getAuthorities()
                                .stream()
                                .findFirst()
                                .get()
                                .getAuthority()
                )
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un token JWT es válido para un usuario específico.
     *
     * La validación comprueba:
     * - que el username del token coincida con el username del usuario
     * - que el token no esté expirado
     *
     * @param token token JWT recibido
     * @param userDetails usuario contra el cual se valida el token
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Extrae el username (subject) desde el token JWT.
     *
     * @param token token JWT
     * @return username (email) contenido en el subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Verifica si el token ya expiró.
     *
     * @param token token JWT
     * @return true si el token está expirado, false si aún es válido por tiempo
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración desde el token.
     *
     * @param token token JWT
     * @return fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Método genérico para extraer un claim del token usando un resolver.
     *
     * @param token token JWT
     * @param claimsResolver función que indica qué claim extraer
     * @return valor del claim solicitado
     * @param <T> tipo de dato del claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     *
     * @param token token JWT
     * @return claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Construye la llave de firma a partir de la clave secreta configurada.
     *
     * @return Key válida para firmar/verificar tokens HS256
     */
    private Key getSignKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el rol almacenado en el claim "role" del token JWT.
     *
     * @param token token JWT
     * @return rol del usuario como texto
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Genera un refresh token JWT para un usuario autenticado.
     * El refresh token tiene una expiración de 7 días y solo contiene el email.
     *
     * @param userEmail email del usuario
     * @return refresh token JWT firmado
     */
    public String generateRefreshToken(String userEmail) {
        return Jwts.builder()
                .claim("type", "refresh")
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un refresh token es válido (firma correcta y no expirado).
     *
     * @param token refresh token JWT
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}