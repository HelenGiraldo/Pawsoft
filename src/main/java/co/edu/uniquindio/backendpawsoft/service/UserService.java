package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.EmailVerificationToken;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.EmailVerificationTokenRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la lógica de negocio relacionada con la entidad {@link User}.
 *
 * Proporciona operaciones para:
 * - consultar usuarios (listar y buscar por id)
 * - crear usuarios de tipo cliente (registro)
 * - actualizar y eliminar usuarios
 * - crear usuarios de tipo staff con contraseña temporal (veterinario/recepcionista)
 *
 * Adicionalmente, implementa {@link UserDetailsService} para que Spring Security pueda
 * cargar usuarios desde la base de datos a partir del correo electrónico.
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
public class UserService implements UserDetailsService {


    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    /**
     * Codificador de contraseñas utilizado para almacenar contraseñas de forma segura.
     */
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Repositorio para acceso a datos de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Servicio de correo usado para notificar contraseñas temporales a usuarios staff.
     */
    private final EmailService emailService;

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     *
     * @return lista de usuarios en formato {@link UserResponse}
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return usuario encontrado en formato {@link UserResponse}
     * @throws RuntimeException si el usuario no existe
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        return mapToResponse(user);
    }

    /**
     * Registra un nuevo usuario de tipo cliente en el sistema.
     *
     * Reglas de negocio:
     * - No se permite registrar un usuario con un correo ya existente.
     * - La contraseña se encripta antes de almacenarse.
     * - El rol asignado por defecto es {@code ROLE_CLIENTE}.
     *
     * @param userRequest datos del usuario a registrar
     * @return usuario guardado en formato {@link UserResponse}
     * @throws IllegalArgumentException si el correo ya existe
     */
    public UserResponse createUser(UserRequest userRequest) {

        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.ROLE_CLIENTE);
        user.setEnabled(false);
        user.setPrimerAcceso(false);

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(token)
                        .user(savedUser)
                        .expirationDate(LocalDateTime.now().plusHours(24))
                        .build();

        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return mapToResponse(savedUser);
    }

    /**
     * Elimina un usuario por su identificador.
     *
     * @param id identificador del usuario a eliminar
     * @throws RuntimeException si no existe un usuario con el ID proporcionado
     */
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Actualiza la información de un usuario existente.
     *
     * Reglas de negocio:
     * - El usuario debe existir.
     * - El correo no puede estar en uso por otro usuario diferente.
     * - La contraseña debe cumplir con reglas mínimas de seguridad.
     * - La contraseña se almacena encriptada.
     *
     * @param id identificador único del usuario a actualizar
     * @param userUpdated datos nuevos del usuario
     * @return usuario actualizado en formato {@link UserResponse}
     * @throws RuntimeException si el usuario no existe, si el correo ya está en uso o si la contraseña no cumple reglas
     */
    public UserResponse updateUser(Long id, UserRequest userUpdated) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        Optional<User> userWithEmail = userRepository.findByEmail(userUpdated.getEmail());

        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new RuntimeException("El correo ya está en uso");
        }

        if (!isPasswordStrong(userUpdated.getPassword())) {
            throw new RuntimeException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial"
            );
        }

        existingUser.setName(userUpdated.getName());
        existingUser.setEmail(userUpdated.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userUpdated.getPassword()));

        User savedUser = userRepository.save(existingUser);
        return mapToResponse(savedUser);
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
     * @return true si la contraseña cumple las reglas, false si no
     */
    public boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }

    /**
     * Carga un usuario por su correo electrónico para el proceso de autenticación.
     *
     * Este método es utilizado por Spring Security durante el proceso de validación
     * de credenciales.
     *
     * @param email correo electrónico del usuario
     * @return {@link UserDetails} con la información del usuario
     * @throws UsernameNotFoundException si no existe un usuario con ese correo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /**
     * Convierte una entidad {@link User} a un DTO {@link UserResponse}.
     *
     * Se utiliza para retornar únicamente información pública del usuario
     * (sin exponer la contraseña u otros datos sensibles).
     *
     * @param user entidad de usuario
     * @return DTO {@link UserResponse}
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getPhotoUrl(),
                user.isEnabled()
        );
    }

    /**
     * Crea un usuario de tipo staff (veterinario o recepcionista) con contraseña temporal.
     *
     * Reglas de negocio:
     * - No se permite registrar un usuario con un correo ya existente.
     * - Se genera una contraseña temporal aleatoria.
     * - La contraseña temporal se almacena encriptada.
     * - El usuario se marca con {@code primerAcceso = true} para obligar el cambio de contraseña.
     * - Se envía la contraseña temporal al correo del usuario.
     *
     * @param nombre nombre del usuario
     * @param email correo del usuario
     * @param role rol asignado (por ejemplo, VETERINARIO o RECEPCIONISTA)
     * @return usuario creado en formato {@link UserResponse}
     * @throws IllegalArgumentException si el correo ya está registrado
     */
    public UserResponse createStaffUser(String nombre, String email, Role role) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String passwordTemporal = UUID.randomUUID().toString().substring(0, 8);
        String passwordTemporalEncriptada = passwordEncoder.encode(passwordTemporal);

        User user = User.builder()
                .name(nombre)
                .email(email)
                .role(role)
                .password(passwordTemporalEncriptada)
                .primerAcceso(true)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendTemporaryPassword(email, passwordTemporal);

        return mapToResponse(savedUser);
    }

    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Token de verificación inválido"));

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        User user = verificationToken.getUser();

        user.setEnabled(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }
}