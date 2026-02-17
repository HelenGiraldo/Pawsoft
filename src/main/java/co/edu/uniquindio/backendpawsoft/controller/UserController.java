package co.edu.uniquindio.backendpawsoft.controller;


import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de exponer los endpoints
 * relacionados con la gestión de usuarios.
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * Endpoint para obtener a todos los usuarios
     *
     * @return lista de usuarios con código HTTP 200
     */

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Endpoint para crear un nuevo usuario
     *
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user){
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }catch (RuntimeException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Endpoint para eliminar un usuario existente por ID
     *
     * @param id identificador del usuario
     * @return codigo HTTP 204 si se elimina correctamente o 404 si el usuario no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser (@PathVariable Long id){

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Endpoint para obtener un usuario específico por su ID
     *
     * @param id identificador único del usuario
     * @return usuario encontrado con código HTTP 200 o 404 si no existe
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id){

        try{
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }


    }


    /**
     * Endpoint para actualizar la información de un usuario existente
     *
     * @param id identificador único del usuario a actualizar
     * @param user datos actualizados del usuario
     * @retunr usuario actualizado con código HTTP 200 o 404 si el usuario no existe
     *
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user){

        try{
            User updateUser = userService.updateUser(id,user);
            return ResponseEntity.ok(updateUser);

        } catch (RuntimeException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }

    }


}
