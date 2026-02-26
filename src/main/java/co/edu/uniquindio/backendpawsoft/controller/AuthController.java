package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.AuthService;
import co.edu.uniquindio.backendpawsoft.service.PasswordResetService;
import co.edu.uniquindio.backendpawsoft.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST que expone los endpoints de autenticación del sistema.
 *
 * Maneja:
 * - Login de usuarios.
 * - Verificación de segundo factor (2FA).
 * - Cambio de contraseña en el primer acceso.
 * - Flujo de “olvidé mi contraseña” (solicitar token y restablecer contraseña).
 *
 * La lógica de negocio se delega a {@link AuthService} y {@link PasswordResetService}.
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

    /**
     * Autentica un usuario e inicia el flujo de 2FA si aplica.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica el código 2FA enviado al correo del usuario.
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(
            @RequestParam String email,
            @RequestParam String code
    ) {
        LoginResponse response = authService.verifyCode(email, code);
        return ResponseEntity.ok(response);
    }

    /**
     * Reenvía el código 2FA al correo SIN pedir contraseña nuevamente.
     */
    @PostMapping("/resend-2fa")
    public ResponseEntity<LoginResponse> resend2FA(
            @RequestParam String email
    ) {
        LoginResponse response = authService.resend2FACode(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Cambia la contraseña durante el primer inicio de sesión.
     */
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

    /**
     * Inicia el flujo de recuperación de contraseña.
     *
     * Por seguridad, siempre responde de forma genérica.
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestReset(
            @Valid @RequestBody RequestPasswordReset request
    ) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("mensaje",
                "Si el correo existe, se ha enviado un enlace de recuperación."
        ));
    }

    /**
     * Finaliza el restablecimiento de contraseña usando un token válido.
     */
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

    /**
     * Verifica la cuenta del usuario mediante token.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(
                "Cuenta verificada correctamente. Puedes cerrar esta ventana."
        );
    }
}