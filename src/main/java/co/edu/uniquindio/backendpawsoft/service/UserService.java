package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
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
 * Servicio encargado de la lógica de negocio relacionada con la entidad User.
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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RecaptchaService recaptchaService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return mapToResponse(user);
    }

    /**
     * Registra un nuevo usuario de tipo cliente.
     * Valida reCAPTCHA antes de procesar el registro.
     */
    public UserResponse createUser(UserRequest userRequest) {

        // ── Validar reCAPTCHA antes de cualquier operación ──
        if (!recaptchaService.isValid(userRequest.getRecaptchaToken())) {
            throw new UnauthorizedException("Verificación reCAPTCHA fallida. Intenta de nuevo.");
        }

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

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

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

    public boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

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
                        .orElseThrow(() -> new RuntimeException("Token de verificación inválido"));

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }
}