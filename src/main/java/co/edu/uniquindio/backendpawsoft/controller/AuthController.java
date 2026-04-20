package co.edu.uniquindio.backendpawsoft.controller;

/**
 * Controlador REST para la gestión de autenticación y autorización.
 * Maneja el registro, login, verificación 2FA, recuperación de contraseñas y refresh tokens.
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
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.AuthService;
import co.edu.uniquindio.backendpawsoft.service.PasswordResetService;
import co.edu.uniquindio.backendpawsoft.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST que expone los endpoints de autenticación del sistema.
 *
 * Maneja:
 * - Login de usuarios e inicio del flujo 2FA.
 * - Verificación del código de segundo factor (2FA).
 * - Reenvío del código 2FA sin requerir contraseña nuevamente.
 * - Cambio de contraseña en el primer inicio de sesión.
 * - Flujo de "olvidé mi contraseña" (solicitar token y restablecer).
 * - Verificación de cuenta por token de correo.
 * - Reenvío del correo de verificación de cuenta.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;

    // ── Autenticación ─────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String ip = obtenerIp(request);
        LoginResponse response = authService.login(loginRequest, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletRequest request
    ) {
        String ip = obtenerIp(request);
        LoginResponse response = authService.verifyCode(email, code, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-2fa")
    public ResponseEntity<LoginResponse> resend2FA(
            @RequestParam String email,
            HttpServletRequest request
    ) {
        String ip = obtenerIp(request);
        LoginResponse response = authService.resend2FACode(email, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada exitosamente"));
    }

    // ── Contraseña ────────────────────────────────────────────────────────────

    @PostMapping("/change-password-first")
    public ResponseEntity<LoginResponse> changePasswordFirstLogin(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        LoginResponse response = authService.changePasswordFirstLogin(
                request.getEmail(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestReset(
            @Valid @RequestBody RequestPasswordReset request
    ) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, se ha enviado un enlace de recuperación."
        ));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(
            @Valid @RequestBody ResetPassword request
    ) {
        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(Map.of(
                "message", "Contraseña actualizada correctamente"
        ));
    }

    // ── Verificación de cuenta ────────────────────────────────────────────────

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(
                "Cuenta verificada correctamente. Puedes cerrar esta ventana."
        );
    }

    /**
     * ✅ NUEVO — Reenvía el correo de verificación de cuenta al usuario indicado.
     *
     * Se usa cuando el usuario no recibió el correo original o el token expiró.
     * Por seguridad siempre responde de forma genérica (no revela si el correo existe).
     *
     * IMPORTANTE: este endpoint debe estar en la lista de rutas públicas del SecurityConfig
     * ya que el usuario aún no está autenticado cuando lo invoca.
     *
     * @param email correo del usuario que solicita el reenvío
     * @return mensaje genérico de confirmación
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestParam String email
    ) {
        userService.reenviarVerificacion(email);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe y no está verificado, se ha reenviado el enlace."
        ));
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Extrae la dirección IP real del cliente desde la solicitud HTTP.
     * Considera proxies y balanceadores de carga (X-Forwarded-For, Proxy-Client-IP).
     */
    private String obtenerIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}