package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.model.RefreshToken;
import co.edu.uniquindio.backendpawsoft.repository.RefreshTokenRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Crea y almacena un refresh token en la base de datos.
     */
    @Transactional
    public String createRefreshToken(String userEmail, String deviceInfo) {
        String refreshToken = jwtService.generateRefreshToken(userEmail);
        String tokenHash = hashToken(refreshToken);

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    /**
     * Valida un refresh token y retorna el email del usuario si es válido.
     */
    @Transactional
    public String validateRefreshToken(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        String userEmail = jwtService.extractUsername(refreshToken);
        String tokenHash = hashToken(refreshToken);
        
        // Buscar el token en la base de datos
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        if (storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token revocado");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado");
        }

        return userEmail;
    }

    /**
     * Revoca un refresh token.
     */
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        try {
            String tokenHash = hashToken(refreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash);
        } catch (Exception e) {
            // Ignorar errores al revocar (el token puede no existir)
        }
    }

    /**
     * Limpieza programada de tokens expirados (diariamente a las 2:00 AM).
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = refreshTokenRepository.deleteExpiredTokens(now);
        
        LocalDateTime cutoff = now.minusDays(30);
        int revokedCount = refreshTokenRepository.deleteRevokedTokensOlderThan(cutoff);
        
        System.out.println("Limpieza de tokens: " + expiredCount + " expirados, " + revokedCount + " revocados eliminados");
    }

    /**
     * Genera un hash SHA-256 del token para almacenamiento seguro.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear el token", e);
        }
    }
}
