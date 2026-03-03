package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.PetRequest;
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints de gestión de mascotas.
 *
 * Permite a los clientes autenticados realizar operaciones CRUD
 * sobre sus propias mascotas. Cada operación valida que el usuario
 * autenticado sea el propietario de la mascota antes de ejecutar
 * cualquier modificación o eliminación.
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
@RequestMapping("/api/cliente/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * Retorna la lista de mascotas registradas por el cliente autenticado.
     *
     * @param auth Objeto de autenticación que contiene el email del usuario.
     * @return Lista de mascotas del cliente.
     */
    @GetMapping
    public ResponseEntity<List<PetResponse>> getMine(Authentication auth) {
        return ResponseEntity.ok(petService.getByOwner(auth.getName()));
    }

    /**
     * Registra una nueva mascota asociada al cliente autenticado.
     *
     * @param req  Datos de la mascota a registrar.
     * @param auth Objeto de autenticación que contiene el email del propietario.
     * @return Mascota creada con su ID asignado.
     */
    @PostMapping
    public ResponseEntity<PetResponse> create(
            @Valid @RequestBody PetRequest req,
            Authentication auth) {
        return ResponseEntity.ok(petService.create(req, auth.getName()));
    }

    /**
     * Actualiza los datos de una mascota existente.
     *
     * Solo el propietario de la mascota puede modificarla.
     * Si el ID no existe o no pertenece al usuario autenticado,
     * el servicio lanzará una excepción.
     *
     * @param id   Identificador de la mascota a actualizar.
     * @param req  Nuevos datos de la mascota.
     * @param auth Objeto de autenticación para validar propiedad.
     * @return Mascota actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest req,
            Authentication auth) {
        return ResponseEntity.ok(petService.update(id, req, auth.getName()));
    }

    /**
     * Elimina una mascota del sistema.
     *
     * Solo el propietario puede eliminar su mascota.
     * Si el ID no existe o no pertenece al usuario autenticado,
     * el servicio lanzará una excepción.
     *
     * @param id   Identificador de la mascota a eliminar.
     * @param auth Objeto de autenticación para validar propiedad.
     * @return Respuesta vacía con código 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication auth) {
        petService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}