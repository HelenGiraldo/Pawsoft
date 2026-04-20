package co.edu.uniquindio.backendpawsoft.controller;

/**
 * Controlador REST para la gestión de usuarios del sistema.
 * Maneja las operaciones CRUD de usuarios y la gestión de perfiles.
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
 * - Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.dto.UserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de exponer los endpoints
 * relacionados con la gestión de usuarios del sistema.
 *
 * Se encarga exclusivamente de operaciones CRUD.
 * No maneja autenticación (ver AuthController).
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
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     *
     * @return ResponseEntity con la lista de usuarios y código HTTP 200 (OK).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param userRequest objeto User con la información del nuevo usuario.
     * @return ResponseEntity con el usuario creado y código HTTP 201 (CREATED).
     */
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest){
        UserResponse savedUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    /**
     * Elimina un usuario existente según su identificador.
     *
     * @param id identificador único del usuario.
     * @return ResponseEntity con código HTTP 204 (NO CONTENT) si la eliminación es exitosa.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene un usuario específico según su identificador.
     *
     * @param id identificador único del usuario.
     * @return ResponseEntity con el usuario encontrado y código HTTP 200 (OK).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/get/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param id identificador único del usuario a actualizar.
     * @param user objeto User con los nuevos datos.
     * @return ResponseEntity con el usuario actualizado y código HTTP 200 (OK).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest user){
        UserResponse updatedUser = userService.updateUser(id,user);
        return ResponseEntity.ok(updatedUser);
    }
}
