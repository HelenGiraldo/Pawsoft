package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.RecepcionistaClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de clientes y mascotas desde el panel de recepcionista.
 *
 * Expone endpoints para:
 * - Listar, crear, editar y desactivar clientes
 * - Listar, crear, editar y eliminar mascotas de un cliente
 *
 * Todos los endpoints requieren rol ROLE_RECEPCIONISTA o ROLE_ADMIN.
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
@RequestMapping("/api/recepcionista")
@RequiredArgsConstructor
public class RecepcionistaClientController {

    private final RecepcionistaClientService clientService;

    // ── Clientes ──────────────────────────────────────────────────────────────

    /**
     * Lista todos los clientes registrados.
     * GET /api/recepcionista/clients
     */
    @GetMapping("/clients")
    public ResponseEntity<List<UserResponse>> getClients() {
        return ResponseEntity.ok(clientService.getClients());
    }

    /**
     * Crea un nuevo cliente (sin agendar cita).
     * POST /api/recepcionista/clients
     */
    @PostMapping("/clients")
    public ResponseEntity<UserResponse> createClient(
            @Valid @RequestBody RecepCreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(request));
    }


    /**
     * Actualiza nombre y teléfono de un cliente existente.
     * PUT /api/recepcionista/clients/{id}
     */
    @PutMapping("/clients/{id}")
    public ResponseEntity<UserResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody RecepUpdateClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    /**
     * Activa o desactiva un cliente (toggle).
     * PATCH /api/recepcionista/clients/{id}/toggle
     */
    @PatchMapping("/clients/{id}/toggle")
    public ResponseEntity<Void> toggleClient(@PathVariable Long id) {
        clientService.toggleClient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina cliente y todo lo relacionado
     */
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mascotas ──────────────────────────────────────────────────────────────

    /**
     * Lista las mascotas de un cliente por su email.
     * GET /api/recepcionista/clients/{email}/pets
     */
    @GetMapping("/clients/{email}/pets")
    public ResponseEntity<List<PetResponse>> getPetsByClient(
            @PathVariable String email) {
        return ResponseEntity.ok(clientService.getPetsByOwner(email));
    }

    /**
     * Agrega una nueva mascota a un cliente existente.
     * POST /api/recepcionista/pets
     */
    @PostMapping("/pets")
    public ResponseEntity<PetResponse> createPet(
            @Valid @RequestBody RecepPetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createPet(request));
    }

    /**
     * Actualiza los datos de una mascota.
     * PUT /api/recepcionista/pets/{id}
     */
    @PutMapping("/pets/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody RecepPetRequest request) {
        return ResponseEntity.ok(clientService.updatePet(id, request));
    }

    /**
     * Elimina una mascota del sistema.
     * DELETE /api/recepcionista/pets/{id}
     */
    @DeleteMapping("/pets/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        clientService.deletePet(id);
        return ResponseEntity.noContent().build();
    }


}