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
     * Ejecuta el proceso de autenticación de un usuario.
     *
     * 1. Busca el usuario por correo electrónico.
     * 2. Verifica que la contraseña coincida con la almacenada.
     * 3. Genera un token JWT si la autenticación es válida.
     *
     * @param loginRequest objeto que contiene correo y contraseña.
     * @return LoginResponse con mensaje, correo y token JWT.
     * @throws NotFoundException si el usuario no existe.
     * @throws UnauthorizedException si la contraseña es incorrecta.
     */
    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Credenciales inválidas"));

        boolean passwordMatch = passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()
        );

        if (!passwordMatch) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                "Autenticación exitosa",
                user.getEmail(),
                token
        );
    }
}
