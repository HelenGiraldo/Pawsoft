package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.LoginRequest;
import co.edu.uniquindio.backendpawsoft.dto.LoginResponse;
import co.edu.uniquindio.backendpawsoft.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST encargado de exponer los endpoints
 * relacionados con la autenticación de usuarios en el sistema.
 *
 * Se encarga exclusivamente del proceso de login y generación
 * de token de autenticación (JWT).
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

    private final AuthService authService;

    /**
     * Autentica un usuario en el sistema.
     *
     * Recibe las credenciales (correo y contraseña),
     * las valida y delega el proceso de autenticación
     * al servicio correspondiente.
     *
     * @param loginRequest objeto que contiene las credenciales del usuario.
     * @return ResponseEntity con el token JWT y código HTTP 200 (OK)
     *         si la autenticación es exitosa.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
