package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.ProfileUpdateRequest;
import co.edu.uniquindio.backendpawsoft.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestión del perfil del usuario autenticado.
 *
 * Expone endpoints para:
 * - Consultar datos del perfil actual
 * - Solicitar código de verificación 2FA para cambios
 * - Aplicar cambios de perfil tras validar el código
 *
 * Todas las rutas requieren JWT válido (anyRequest().authenticated() en SecurityConfig).
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
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Retorna los datos actuales del perfil.
     * GET /api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getProfile(Authentication auth) {
        return ResponseEntity.ok(profileService.getProfile(auth.getName()));
    }

    /**
     * Envía un código 2FA al correo actual del usuario autenticado.
     * POST /api/profile/request-verification
     */
    @PostMapping("/request-verification")
    public ResponseEntity<Map<String, String>> requestVerification(Authentication auth) {
        profileService.requestVerification(auth.getName());
        return ResponseEntity.ok(Map.of("message", "Código de verificación enviado al correo."));
    }

    /**
     * Valida el código y aplica los cambios de perfil.
     * POST /api/profile/verify-and-save
     */
    @PostMapping("/verify-and-save")
    public ResponseEntity<Map<String, String>> verifyAndSave(
            Authentication auth,
            @RequestBody ProfileUpdateRequest request) {

        profileService.verifyAndSave(auth.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Perfil actualizado correctamente."));
    }
}