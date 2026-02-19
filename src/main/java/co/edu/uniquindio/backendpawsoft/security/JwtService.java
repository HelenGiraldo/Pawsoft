package co.edu.uniquindio.backendpawsoft.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Servicio encargado de generar y validar
 * tokens JWT dentro del sistema.
 *
 *
 * El token JWT permite autenticar usuarios
 * de manera segura mediante un mecanismo
 * basado en firma digital.
 *
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
     * Clave secreta uutilizada para firmar el token
     */
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);


    /**
     * Genera un token JWT con una duración de un minuto,
     * cumpliendo la regla de negocio de cierre de sesión
     * por inactividad.
     *
     * @param username identificador del usuario autenticado
     * @return token JWT firmado con expiración de 1 minuto
     */

    public String generateToken(String username) {

        long expirationTime = 1000 * 60; // 1 minuto

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Extrae el nombre de usuario contenido contenido en el token
     *
     * @param token token JWT
     * @return username almacenado en el token
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

}
