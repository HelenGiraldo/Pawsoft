package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio encargado de gestionar el proceso de autenticación
 * y generación de tokens JWT del sistema.
 *
 * Este servicio valida las credenciales del usuario,
 * verifica la contraseña encriptada y genera el token
 * de acceso correspondiente en caso de autenticación exitosa.
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
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Autentica un usuario y genera token JWT.
     *
     * Reglas de negocio:
     * - Bloqueo temporal tras 5 intentos fallidos.
     * - Reseteo de contador tras login exitoso.
     *
     * @param loginRequest correo y contraseña
     * @return LoginResponse con token JWT
     * @throws NotFoundException si el usuario no existe
     * @throws UnauthorizedException si contraseña incorrecta o cuenta bloqueada
     */
    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Credenciales inválidas"));

        // Revisar si el usuario está bloqueado
        if (user.getLockTime() != null) {
            if (user.getLockTime().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new UnauthorizedException("Usuario bloqueado temporalmente. Intente más tarde.");
            } else {

                // Resetear bloqueo si ya pasó el tiempo
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
        }

        // Validar contraseña
        boolean passwordMatch = passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()
        );

        if (!passwordMatch) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 3) {
                user.setLockTime(LocalDateTime.now());
            }

            userRepository.save(user);
            throw new UnauthorizedException("Credenciales inválidas");
        }

        // Login exitoso: resetear contador
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                "Autenticación exitosa",
                user.getEmail(),
                token
        );
    }

}
