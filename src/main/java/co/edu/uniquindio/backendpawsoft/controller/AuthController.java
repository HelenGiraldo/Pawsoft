package co.edu.uniquindio.backendpawsoft.controller;

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
 *
 * Captura de IP:
 * Cada endpoint que interactúa con el flujo 2FA recibe un {@link HttpServletRequest}
 * para capturar la IP de origen mediante {@link #obtenerIp(HttpServletRequest)}.
 * Esta IP se pasa al servicio para registrarla en los logs de auditoría de {@code codigos_2fa}.
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

    // ── Autenticación ─────────────────────────────────────────────────────────

    /**
     * Autentica un usuario con email, contraseña y token reCAPTCHA.
     *
     * Si las credenciales son válidas, genera y envía un código 2FA al correo del usuario.
     * La IP de origen se captura y se almacena junto al código para auditoría.
     *
     * @param loginRequest cuerpo de la solicitud con email, password y recaptchaToken
     * @param request      objeto HTTP de la solicitud, usado para extraer la IP de origen
     * @return respuesta con estado del proceso de login (pendiente de verificación 2FA)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String ip = obtenerIp(request);
        LoginResponse response = authService.login(loginRequest, ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica el código 2FA ingresado por el usuario.
     *
     * Valida el código contra el almacenado en base de datos, aplicando reglas
     * de expiración y límite de intentos. El resultado (EXITOSO, FALLIDO, EXPIRADO)
     * queda registrado en auditoría junto con la IP de este intento.
     *
     * @param email   correo del usuario que intenta verificar
     * @param code    código de 6 dígitos ingresado por el usuario
     * @param request objeto HTTP de la solicitud, usado para extraer la IP de origen
     * @return respuesta con token JWT y rol si la verificación fue exitosa
     */
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

    /**
     * Reenvía un nuevo código 2FA al correo del usuario sin requerir contraseña.
     *
     * El código anterior (si existe) se invalida y queda registrado con resultado
     * {@code INVALIDADO} en auditoría. La IP de esta solicitud se asocia al nuevo código.
     *
     * @param email   correo del usuario que solicita el reenvío
     * @param request objeto HTTP de la solicitud, usado para extraer la IP de origen
     * @return respuesta indicando que el código fue reenviado
     */
    @PostMapping("/resend-2fa")
    public ResponseEntity<LoginResponse> resend2FA(
            @RequestParam String email,
            HttpServletRequest request
    ) {
        String ip = obtenerIp(request);
        LoginResponse response = authService.resend2FACode(email, ip);
        return ResponseEntity.ok(response);
    }

    // ── Contraseña ────────────────────────────────────────────────────────────

    /**
     * Cambia la contraseña del usuario durante su primer inicio de sesión.
     *
     * Se usa cuando el sistema generó una contraseña temporal y el usuario
     * debe reemplazarla antes de poder operar con normalidad.
     *
     * @param request cuerpo con email y nueva contraseña
     * @return respuesta con token JWT actualizado si el cambio fue exitoso
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
     * Inicia el flujo de recuperación de contraseña enviando un enlace al correo.
     *
     * Por seguridad, siempre responde de forma genérica independientemente de si
     * el correo existe o no, para evitar enumeración de usuarios.
     *
     * @param request cuerpo con el email del usuario
     * @return mensaje genérico de confirmación
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestReset(
            @Valid @RequestBody RequestPasswordReset request
    ) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, se ha enviado un enlace de recuperación."
        ));
    }

    /**
     * Finaliza el restablecimiento de contraseña usando el token recibido por correo.
     *
     * Valida que el token exista, no haya expirado y no haya sido usado,
     * luego actualiza la contraseña del usuario asociado.
     *
     * @param request cuerpo con el token de recuperación y la nueva contraseña
     * @return mensaje de confirmación si el proceso fue exitoso
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

    // ── Verificación de cuenta ────────────────────────────────────────────────

    /**
     * Verifica la cuenta del usuario mediante el token enviado al correo de registro.
     *
     * Una vez verificada, la cuenta queda habilitada para iniciar sesión.
     *
     * @param token token de verificación recibido por correo
     * @return mensaje de confirmación para mostrar al usuario
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

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Extrae la dirección IP real del cliente desde la solicitud HTTP.
     *
     * Considera el caso de proxies y balanceadores de carga que añaden la IP original
     * en cabeceras como {@code X-Forwarded-For} o {@code Proxy-Client-IP}.
     * Si ninguna cabecera está presente, usa {@code getRemoteAddr()} directamente.
     *
     * {@code X-Forwarded-For} puede contener múltiples IPs separadas por coma
     * cuando hay cadenas de proxies; siempre se toma la primera (IP del cliente real).
     *
     * @param request objeto HTTP de la solicitud entrante
     * @return dirección IP del cliente como cadena de texto; máximo 45 caracteres (IPv6)
     */
    private String obtenerIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Toma solo la primera IP si vienen varias separadas por coma
        return ip.split(",")[0].trim();
    }
}