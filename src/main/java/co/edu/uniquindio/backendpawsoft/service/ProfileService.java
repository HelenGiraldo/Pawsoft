package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.ProfileUpdateRequest;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio para la gestión del perfil del usuario autenticado.
 *
 * Permite al usuario actualizar sus datos personales (correo, teléfono y contraseña)
 * de forma segura mediante un flujo de verificación con segundo factor (2FA):
 *
 * 1. El usuario solicita un código de verificación → {@link #requestVerification(String)}.
 * 2. El usuario envía el código junto con los cambios → {@link #verifyAndSave(String, ProfileUpdateRequest)}.
 *
 * Los tres campos editables (email, phone, newPassword) son completamente opcionales.
 * Solo se actualiza lo que el usuario envíe con valor — lo demás permanece igual.
 *
 * Nota sobre IP:
 * Las operaciones de perfil no provienen del flujo de autenticación, por lo que
 * no se captura la IP del cliente en este contexto. Se pasa {@code null} a
 * {@link TwoFactorService} en los campos {@code ipOrigen}, lo cual es válido ya
 * que la columna {@code ip_origen} en BD permite valores nulos.
 * Si en el futuro se desea registrar la IP también en cambios de perfil, basta con
 * agregar {@code HttpServletRequest} al controlador correspondiente y propagarla aquí.
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
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository        userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TwoFactorService      twoFactorService;
    private final EmailService          emailService;
    private final AuditLogService       auditLogService;
    private final PetRepository         petRepository;

    // ── Solicitud de verificación ─────────────────────────────────────────────

    /**
     * Genera y envía un código 2FA al correo del usuario autenticado.
     *
     * Si el usuario ya tenía un código activo, este se invalida y queda registrado
     * con resultado {@code INVALIDADO} en auditoría antes de crear el nuevo.
     *
     * Se pasa {@code null} como IP porque las solicitudes de perfil no requieren
     * trazabilidad de IP en la implementación actual.
     *
     * @param email correo del usuario autenticado que solicita el código
     * @throws NotFoundException si el email no corresponde a ningún usuario registrado
     */
    public void requestVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // IP null: válido para cambios de perfil donde no se requiere trazabilidad de red
        Codigo2FA codigo = twoFactorService.crearCodigo(user, null);
        emailService.enviarCodigo2FA(user.getEmail(), codigo.getCodigo());
    }

    // ── Verificación y actualización ──────────────────────────────────────────

    /**
     * Valida el código 2FA y aplica únicamente los campos que el usuario modificó.
     *
     * El código se valida primero; si es incorrecto, expirado o el usuario está bloqueado,
     * se lanza {@code UnauthorizedException} sin aplicar ningún cambio.
     *
     * Campos opcionales:
     * - {@code email}       → solo se actualiza si tiene valor y es distinto al actual.
     *                         Se verifica que no esté en uso por otro usuario.
     * - {@code phone}       → solo se actualiza si tiene valor.
     * - {@code newPassword} → solo se actualiza si tiene valor y cumple requisitos de seguridad.
     *
     * Se pasa {@code null} como IP al validar el código porque en este contexto
     * no se requiere registrar la IP del intento de verificación.
     *
     * @param currentEmail correo actual del usuario autenticado (extraído del JWT)
     * @param request      DTO con el código 2FA y los campos a actualizar
     * @throws NotFoundException     si el usuario no existe
     * @throws UnauthorizedException si el código es inválido, expiró, hay bloqueo activo,
     *                               el correo nuevo ya está en uso, o la contraseña no
     *                               cumple los requisitos de seguridad
     */
    public void verifyAndSave(String currentEmail, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Valida el código 2FA — lanza excepción si es inválido, expirado o bloqueado
        // IP null: no se requiere trazabilidad de red para cambios de perfil
        twoFactorService.validarCodigo(user, request.getCode(), null);

        // ── Email (opcional) ──────────────────────────────────────────────────
        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {

            // Verifica que el nuevo correo no esté en uso por otro usuario
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new UnauthorizedException("El correo ya está en uso por otro usuario.");
                }
            });

            // Transfiere las mascotas al nuevo correo
            String emailAnterior = user.getEmail();
            List<Pet> mascotas = petRepository.findByOwnerEmail(emailAnterior);
            mascotas.forEach(pet -> pet.setOwnerEmail(request.getEmail()));
            petRepository.saveAll(mascotas);

            user.setEmail(request.getEmail());
            auditLogService.log("PROFILE_UPDATE_EMAIL", "Usuario actualizó su correo", "USER", user.getId().intValue());
        }

        // ── Teléfono (opcional) ───────────────────────────────────────────────
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
            auditLogService.log("PROFILE_UPDATE_PHONE", "Usuario actualizó su teléfono", "USER", user.getId().intValue());
        }

        // ── Contraseña (opcional) ─────────────────────────────────────────────
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // Validar contraseña actual si se proporciona una nueva contraseña
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new UnauthorizedException("Debes proporcionar tu contraseña actual para cambiarla.");
            }
            
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new UnauthorizedException("La contraseña actual es incorrecta.");
            }
            
            if (!isPasswordStrong(request.getNewPassword())) {
                throw new UnauthorizedException(
                        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
                );
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            auditLogService.log("PROFILE_UPDATE_PASSWORD", "Usuario actualizó su contraseña", "USER", user.getId().intValue());
        }

        userRepository.save(user);
    }

    // ── Consulta de perfil ────────────────────────────────────────────────────

    /**
     * Retorna los datos actuales del perfil del usuario autenticado.
     *
     * Solo expone los campos que el usuario puede visualizar y editar desde el frontend.
     * El campo {@code phone} se retorna como cadena vacía si no tiene valor registrado,
     * para facilitar el binding en el formulario del cliente.
     *
     * @param email correo del usuario autenticado (extraído del JWT)
     * @return mapa con las claves {@code name}, {@code email} y {@code phone}
     * @throws NotFoundException si el usuario no existe
     */
    public Map<String, String> getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return Map.of(
                "name",  user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : ""
        );
    }

    // ── Validación de contraseña actual ──────────────────────────────────────

    /**
     * Valida si la contraseña actual proporcionada por el usuario es correcta.
     *
     * @param email correo del usuario autenticado
     * @param currentPassword contraseña actual en texto plano
     * @return {@code true} si la contraseña es correcta; {@code false} en caso contrario
     * @throws NotFoundException si el usuario no existe
     */
    public boolean validateCurrentPassword(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        
        return passwordEncoder.matches(currentPassword, user.getPassword());
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
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$"
        );
    }
}