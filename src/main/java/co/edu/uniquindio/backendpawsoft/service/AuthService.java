package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
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
 * Servicio responsable de la autenticación de usuarios y la emisión de tokens JWT del sistema.
 *
 * Implementa el flujo de autenticación en dos fases:
 * 1) Validación de credenciales (email + contraseña) e inicio de 2FA
 * 2) Verificación del código 2FA y generación del JWT
 *
 * También gestiona reglas de seguridad adicionales:
 * - Control de intentos fallidos
 * - Bloqueo temporal de cuenta por intentos repetidos
 * - Cambio de contraseña en primer acceso para usuarios con contraseña temporal
 *
 * Flujo general:
 * 1. POST /auth/login       → valida credenciales → genera código → envía correo
 * 2. POST /auth/verify-2fa  → valida código → emite JWT
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

    /**
     * Número máximo de intentos fallidos permitidos antes de bloquear temporalmente la cuenta.
     */
    private static final int MAX_FAILED_ATTEMPTS = 3;

    /**
     * Duración del bloqueo temporal de cuenta en minutos.
     */
    private static final long LOCK_DURATION_MINUTES = 1;

    /**
     * Repositorio para acceder y persistir información de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Encoder BCrypt para validar y generar hashes de contraseñas.
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Servicio encargado de generar y validar tokens JWT.
     */
    private final JwtService jwtService;

    /**
     * Servicio que encapsula la lógica de generación y validación de códigos 2FA.
     */
    private final TwoFactorService twoFactorService;

    /**
     * Servicio de envío de correo, usado para enviar el código 2FA.
     */
    private final EmailService emailService;

    /**
     * Primera fase del login: valida credenciales e inicia el flujo 2FA.
     *
     * Reglas de negocio:
     * - Si el usuario no existe, se lanza {@link NotFoundException}.
     * - Si la cuenta está bloqueada, se lanza {@link UnauthorizedException}.
     * - Si la contraseña es incorrecta, se incrementan intentos fallidos.
     * - Al llegar al máximo de intentos, la cuenta se bloquea temporalmente.
     * - Si las credenciales son correctas, se genera y envía el código 2FA.
     *
     * @param loginRequest objeto con email y contraseña
     * @return LoginResponse indicando que el código fue enviado (sin JWT todavía)
     */
    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Credenciales inválidas"));

        validateAccountLock(user);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            handleFailedAttempt(user);
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if(!user.isEnabled()) {
            throw new RuntimeException("Debes verificar tu correo antes de iniciar sesión");
        }

        resetFailedAttempts(user);

        Codigo2FA codigo = twoFactorService.crearCodigo(user);

        emailService.enviarCodigo2FA(user.getEmail(), codigo.getCodigo());

        return new LoginResponse(
                "Código de verificación enviado al correo",
                user.getEmail(),
                user.getRole().name(),
                null
        );
    }

    /**
     * Reenvía un nuevo código 2FA al correo del usuario SIN volver a validar contraseña.
     * Útil cuando el usuario no recibió el correo o el código expiró.
     */
    public LoginResponse resend2FACode(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Debes verificar tu correo antes de continuar");
        }

        // Opcional: si quieres respetar bloqueo temporal también aquí, descomenta:
        // validateAccountLock(user);

        Codigo2FA codigo = twoFactorService.crearCodigo(user);
        emailService.enviarCodigo2FA(user.getEmail(), codigo.getCodigo());

        return new LoginResponse(
                "Código de verificación reenviado al correo",
                user.getEmail(),
                user.getRole().name(),
                null
        );
    }

    /**
     * Segunda fase del login: verifica el código 2FA y emite el JWT.
     *
     * Reglas de negocio:
     * - El usuario debe existir.
     * - La validación del código (existencia, expiración, uso e intentos) se delega a {@link TwoFactorService}.
     * - Si el código es válido, se emite el JWT.
     *
     * @param email correo del usuario
     * @param ingresado código ingresado por el usuario
     * @return LoginResponse con el JWT listo para usar
     */
    public LoginResponse verifyCode(String email, String ingresado) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        twoFactorService.validarCodigo(user, ingresado);

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                "Autenticación completa",
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }

    /**
     * Verifica si la cuenta está bloqueada temporalmente.
     *
     * Si la cuenta está bloqueada y el tiempo de bloqueo aún no termina, lanza excepción.
     * Si el tiempo ya pasó, resetea el estado de bloqueo e intentos fallidos.
     *
     * @param user usuario a validar
     * @throws UnauthorizedException si la cuenta sigue bloqueada
     */
    private void validateAccountLock(User user) {

        if (user.getLockTime() == null) {
            return;
        }

        LocalDateTime unlockTime = user.getLockTime()
                .plusMinutes(LOCK_DURATION_MINUTES);

        if (unlockTime.isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException(
                    "Cuenta bloqueada temporalmente. Intente en 1 minuto."
            );
        }

        resetFailedAttempts(user);
    }

    /**
     * Incrementa el contador de intentos fallidos y, si se alcanza el máximo,
     * establece el inicio del bloqueo temporal.
     *
     * @param user usuario a actualizar
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
     * Resetea el contador de intentos fallidos y elimina el bloqueo temporal.
     *
     * @param user usuario a actualizar
     */
    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

    /**
     * Cambia la contraseña temporal de un usuario durante el primer inicio de sesión.
     *
     * Reglas de negocio:
     * - Solo aplica si el usuario tiene {@code primerAcceso = true}.
     * - La nueva contraseña debe cumplir reglas de seguridad.
     * - La nueva contraseña no puede ser igual a la contraseña temporal actual.
     * - Si la operación es exitosa, se marca {@code primerAcceso = false} y se emite un JWT.
     *
     * @param email correo del usuario
     * @param newPassword nueva contraseña
     * @return LoginResponse con JWT listo para usar si se actualiza correctamente
     * @throws NotFoundException si no existe el usuario
     * @throws UnauthorizedException si el usuario no puede cambiar la contraseña o la contraseña no cumple reglas
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

        return new LoginResponse(
                "Contraseña cambiada exitosamente",
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }

    /**
     * Valida la fuerza de una contraseña según reglas mínimas de seguridad.
     *
     * Reglas:
     * - mínimo 8 caracteres
     * - al menos una minúscula
     * - al menos una mayúscula
     * - al menos un número
     * - al menos un carácter especial
     *
     * @param password contraseña a validar
     * @return true si cumple las reglas, false en caso contrario
     */
    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }
}