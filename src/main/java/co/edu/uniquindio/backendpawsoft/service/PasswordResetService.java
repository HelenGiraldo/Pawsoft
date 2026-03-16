package co.edu.uniquindio.backendpawsoft.service;

/**
 * Servicio para el flujo de recuperación de contraseña.
 *
 * Implementa dos pasos:
 * 1. {@link #requestPasswordReset(String)} — genera un token seguro y lo envía
 *    por correo al usuario. La respuesta es silenciosa si el email no existe,
 *    para evitar enumeración de usuarios.
 * 2. {@link #resetPassword(String, String)} — valida el token y actualiza la
 *    contraseña si cumple los requisitos de seguridad.
 *
 * Los tokens expiran a los 30 minutos y solo pueden usarse una vez.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.PasswordResetToken;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PasswordResetTokenRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRATION_MINUTES = 30;

    /**
     * Genera un token de recuperación y lo envía por correo.
     */
    public void requestPasswordReset(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return; // respuesta silenciosa
        }

        User user = optionalUser.get();

        String token = generateSecureToken();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expirationDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink
        );

        auditLogService.log("USER_PASSWORD_RESET_REQUEST", "Solicitud de recuperación de contraseña", "USER", user.getId().intValue());
    }

    /**
     * Cambia la contraseña usando un token válido.
     */
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository
                .findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new UnauthorizedException("Token inválido o ya usado"));

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("El token ha expirado");
        }

        if (!isPasswordStrong(newPassword)) {
            throw new UnauthorizedException("La contraseña no cumple las reglas de seguridad");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        auditLogService.log("USER_PASSWORD_RESET", "Contraseña restablecida exitosamente", "USER", user.getId().intValue());
    }

    /**
     * Genera token seguro base64.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Valida fortaleza de contraseña.
     * Mínimo 8 caracteres, 1 mayúscula, 1 número y 1 carácter especial.
     */
    private boolean isPasswordStrong(String password) {
        return password != null &&
                password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
    }
}