package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio responsable de la autenticación de usuarios y la emisión de tokens JWT.
 *
 * Gestiona el flujo completo de autenticación en dos fases:
 *
 * Fase 1 — Login:
 *   Valida reCAPTCHA, verifica credenciales (email + contraseña), controla el bloqueo
 *   por intentos fallidos y, si todo es correcto, genera y envía un código 2FA al correo.
 *
 * Fase 2 — Verificación 2FA:
 *   Valida el código ingresado por el usuario contra el almacenado en BD,
 *   aplicando reglas de expiración y fuerza bruta. Si es correcto, emite el JWT.
 *
 * Propagación de IP:
 *   La dirección IP del cliente se recibe desde el controlador (que tiene acceso a
 *   {@code HttpServletRequest}) y se propaga a {@link TwoFactorService} para que quede
 *   registrada en los logs de auditoría de {@code codigos_2fa}. Este servicio no depende
 *   directamente de la capa HTTP, lo que mantiene la separación de responsabilidades.
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
@RequiredArgsConstructor
public class AuthService {

    /** Número máximo de intentos de login fallidos antes de bloquear la cuenta. */
    private static final int MAX_FAILED_ATTEMPTS = 3;

    /** Minutos que la cuenta permanece bloqueada tras agotar los intentos fallidos. */
    private static final long LOCK_DURATION_MINUTES = 1;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final EmailService emailService;
    private final RecaptchaService recaptchaService;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    // ── Fase 1: Login ─────────────────────────────────────────────────────────

    /**
     * Primera fase del login: valida reCAPTCHA, credenciales e inicia el flujo 2FA.
     *
     * Pasos:
     * 1. Valida el token reCAPTCHA para prevenir bots antes de consultar la BD.
     * 2. Busca el usuario por email; lanza excepción genérica si no existe
     *    (evita enumeración de usuarios).
     * 3. Verifica si la cuenta está bloqueada por intentos fallidos previos.
     * 4. Compara la contraseña con el hash almacenado; si falla incrementa el contador.
     * 5. Verifica que la cuenta esté habilitada (email verificado).
     * 6. Resetea el contador de intentos fallidos tras login exitoso.
     * 7. Genera un código 2FA, lo persiste y lo envía al correo del usuario.
     *
     * La IP se propaga a {@link TwoFactorService#crearCodigo} para registrarla
     * en auditoría junto al código generado.
     *
     * @param loginRequest DTO con email, password y recaptchaToken
     * @param ipOrigen     dirección IP del cliente capturada en el controlador
     * @return {@link LoginResponse} indicando que el código 2FA fue enviado
     * @throws UnauthorizedException si el reCAPTCHA falla, las credenciales son inválidas
     *                               o la cuenta está bloqueada
     * @throws NotFoundException     si el email no corresponde a ningún usuario registrado
     */
    public LoginResponse login(LoginRequest loginRequest, String ipOrigen) {

        // Valida reCAPTCHA antes de cualquier consulta a BD
        if (!recaptchaService.isValid(loginRequest.getRecaptchaToken())) {
            throw new UnauthorizedException("Verificación reCAPTCHA fallida. Intenta de nuevo.");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Credenciales inválidas. Verifica tu correo y contraseña"));

        // Verifica si la cuenta está bloqueada temporalmente
        validateAccountLock(user);

        // Verifica contraseña; si falla registra el intento y bloquea si corresponde
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            handleFailedAttempt(user);
            auditLogService.log("USER_LOGIN_FAILED", "Intento de login fallido", "USER", user.getId().intValue());
            throw new UnauthorizedException("Credenciales inválidas. Verifica tu correo y contraseña");
        }

        // Verifica que el usuario haya confirmado su correo
        if (!user.isEnabled()) {
            throw new RuntimeException("Debes verificar tu correo antes de iniciar sesión");
        }

        // Login exitoso — resetea el contador de fallos
        resetFailedAttempts(user);

        // Genera y envía el código 2FA; registra la IP de esta solicitud
        Codigo2FA codigo = twoFactorService.crearCodigo(user, ipOrigen);
        emailService.enviarCodigo2FA(user.getEmail(), codigo.getCodigo());

        auditLogService.log("USER_LOGIN", "Inicio de sesión exitoso, código 2FA enviado", "USER", user.getId().intValue());

        return new LoginResponse(
                "Código de verificación enviado al correo",
                user.getEmail(),
                user.getRole().name(),
                null,
                null,
                false
        );
    }

    /**
     * Segunda fase del login: valida el código 2FA ingresado por el usuario.
     *
     * Delega la validación a {@link TwoFactorService#validarCodigo}, que aplica
     * las reglas de expiración, intentos fallidos y bloqueo por fuerza bruta,
     * registrando el resultado en auditoría junto con la IP de este intento.
     *
     * Si el código es correcto, emite un JWT firmado con el rol del usuario.
     * También detecta si el usuario debe cambiar su contraseña en este primer acceso.
     *
     * @param email     correo del usuario que intenta verificar
     * @param ingresado código de 6 dígitos ingresado por el usuario
     * @param ipOrigen  dirección IP del cliente capturada en el controlador
     * @return {@link LoginResponse} con token JWT, rol y flag de primer acceso
     * @throws NotFoundException     si el email no corresponde a ningún usuario registrado
     * @throws UnauthorizedException si el código es incorrecto, expiró o hay bloqueo activo
     */
    public LoginResponse verifyCode(String email, String ingresado, String ipOrigen) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Delega la validación del código al servicio 2FA con la IP para auditoría
        twoFactorService.validarCodigo(user, ingresado, ipOrigen);

        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), "web");

        // Los usuarios distintos a ROLE_CLIENTE deben cambiar la contraseña en el primer acceso
        boolean mustChange = user.isPrimerAcceso()
                && user.getRole() != Role.ROLE_CLIENTE;

        auditLogService.log("USER_VERIFY_2FA", "Verificación 2FA completada", "USER", user.getId().intValue());

        return new LoginResponse(
                "Autenticación completa",
                user.getEmail(),
                user.getRole().name(),
                token,
                refreshToken,
                mustChange
        );
    }

    // ── Reenvío de código 2FA ─────────────────────────────────────────────────

    /**
     * Reenvía un nuevo código 2FA al correo del usuario sin requerir contraseña nuevamente.
     *
     * El código anterior (si existe) queda invalidado en auditoría con resultado
     * {@code INVALIDADO}. El nuevo código se registra con la IP de esta solicitud.
     *
     * Se usa cuando el usuario no recibió el correo o el código expiró antes de verificarlo.
     *
     * @param email    correo del usuario que solicita el reenvío
     * @param ipOrigen dirección IP del cliente capturada en el controlador
     * @return {@link LoginResponse} confirmando que el nuevo código fue enviado
     * @throws NotFoundException     si el email no corresponde a ningún usuario registrado
     * @throws UnauthorizedException si la cuenta no está habilitada (email no verificado)
     */
    public LoginResponse resend2FACode(String email, String ipOrigen) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Debes verificar tu correo antes de continuar");
        }

        // Invalida el código activo anterior y genera uno nuevo con la IP de esta solicitud
        Codigo2FA codigo = twoFactorService.crearCodigo(user, ipOrigen);
        emailService.enviarCodigo2FA(user.getEmail(), codigo.getCodigo());

        auditLogService.log("USER_RESEND_2FA", "Reenvío de código 2FA", "USER", user.getId().intValue());

        return new LoginResponse(
                "Código de verificación reenviado al correo",
                user.getEmail(),
                user.getRole().name(),
                null,
                null,
                false
        );
    }

    /**
     * Cambia la contraseña del usuario durante su primer inicio de sesión.
     *
     * Aplica cuando el sistema asignó una contraseña temporal (ej: al crear un empleado
     * desde el panel de administración). El usuario debe reemplazarla antes de operar.
     *
     * Validaciones:
     * - El usuario debe tener el flag {@code primerAcceso = true}.
     * - La nueva contraseña debe cumplir los requisitos de seguridad.
     * - La nueva contraseña no puede ser igual a la temporal actual.
     *
     * @param email       correo del usuario que cambia su contraseña
     * @param newPassword nueva contraseña elegida por el usuario
     * @return {@link LoginResponse} con nuevo token JWT tras el cambio exitoso
     * @throws NotFoundException     si el email no corresponde a ningún usuario registrado
     * @throws UnauthorizedException si el usuario ya cambió la contraseña, si la nueva
     *                               no cumple los requisitos o si es igual a la actual
     */
    public LoginResponse changePasswordFirstLogin(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!user.isPrimerAcceso()) {
            throw new UnauthorizedException("El usuario ya ha cambiado la contraseña");
        }

        if (!isPasswordStrong(newPassword)) {
            throw new UnauthorizedException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial"
            );
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new UnauthorizedException("La nueva contraseña no puede ser igual a la temporal");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPrimerAcceso(false);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), "web");

        auditLogService.log("USER_CHANGE_PASSWORD_FIRST", "Cambio de contraseña en primer acceso", "USER", user.getId().intValue());

        return new LoginResponse(
                "Contraseña cambiada exitosamente",
                user.getEmail(),
                user.getRole().name(),
                token,
                refreshToken,
                false
        );
    }

    // ── Control de intentos fallidos ──────────────────────────────────────────

    /**
     * Verifica si la cuenta del usuario está bloqueada temporalmente por exceso de
     * intentos de login fallidos.
     *
     * Si el tiempo de bloqueo ya expiró, resetea el contador automáticamente para
     * permitir nuevos intentos sin intervención manual.
     *
     * @param user usuario a verificar
     * @throws UnauthorizedException si el bloqueo sigue vigente
     */
    private void validateAccountLock(User user) {
        if (user.getLockTime() == null) return;

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
        if (unlockTime.isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Cuenta bloqueada temporalmente. Intente en 1 minuto.");
        }

        // El bloqueo ya expiró — resetea para permitir nuevos intentos
        resetFailedAttempts(user);
    }

    /**
     * Registra un intento de login fallido para el usuario dado.
     *
     * Incrementa el contador de fallos y, si se alcanza el máximo permitido,
     * registra la hora de bloqueo para activar el cooldown.
     *
     * @param user usuario que realizó el intento fallido
     */
    private void handleFailedAttempt(User user) {
        int nuevosIntentos = user.getFailedAttempts() + 1;
        user.setFailedAttempts(nuevosIntentos);
        if (nuevosIntentos >= MAX_FAILED_ATTEMPTS) {
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    /**
     * Resetea el contador de intentos fallidos y elimina el bloqueo activo del usuario.
     *
     * Se llama tras un login exitoso o tras detectar que el período de bloqueo expiró.
     *
     * @param user usuario cuyo estado de bloqueo se resetea
     */
    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

    // ── Validación de contraseña ──────────────────────────────────────────────

    /**
     * Verifica si una contraseña cumple los requisitos mínimos de seguridad.
     *
     * Requisitos:
     * - Mínimo 8 caracteres.
     * - Al menos una letra minúscula.
     * - Al menos una letra mayúscula.
     * - Al menos un dígito.
     * - Al menos un carácter especial (@$!%*?&).
     *
     * @param password contraseña en texto plano a validar
     * @return {@code true} si cumple todos los requisitos; {@code false} en caso contrario
     */
    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
        return password.matches(pattern);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    /**
     * Renueva el access token usando un refresh token válido.
     */
    public LoginResponse refreshToken(String refreshToken) {
        String userEmail = refreshTokenService.validateRefreshToken(refreshToken);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail(), "web");

        // Revocar el refresh token anterior
        refreshTokenService.revokeRefreshToken(refreshToken);

        auditLogService.log("USER_REFRESH_TOKEN", "Token renovado exitosamente", "USER", user.getId().intValue());

        return new LoginResponse(
                "Token renovado exitosamente",
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                newRefreshToken,
                false
        );
    }

    /**
     * Cierra la sesión del usuario revocando su refresh token.
     */
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}