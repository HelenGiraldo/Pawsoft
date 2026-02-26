package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.StaffUserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST encargado de exponer los endpoints relacionados con la creación
 * de usuarios de tipo staff dentro del sistema.
 *
 * Permite registrar personal como veterinarios o recepcionistas. La lógica de negocio
 * (validaciones, generación de contraseña temporal y persistencia) se delega a
 * {@link UserService}.
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
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    /**
     * Servicio encargado de la lógica de negocio de usuarios.
     */
    private final UserService userService;

    /**
     * Crea un usuario de tipo staff (por ejemplo, veterinario o recepcionista).
     *
     * @param request DTO con la información necesaria para crear el usuario staff
     *                (nombre, email y rol)
     * @return ResponseEntity con los datos del usuario creado y código HTTP 201 (CREATED)
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createStaff(@RequestBody StaffUserRequest request) {
        UserResponse userResponse = userService.createStaffUser(
                request.getNombre(),
                request.getEmail(),
                request.getRole()
        );
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }
}