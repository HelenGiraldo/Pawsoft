package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.StaffUserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones del panel de administración.
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
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** Lista todo el staff (no clientes) */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getStaff() {
        return ResponseEntity.ok(adminUserService.getStaffUsers());
    }

    /** Solo veterinarios activos */
    @GetMapping("/veterinarians")
    public ResponseEntity<List<UserResponse>> getVets() {
        return ResponseEntity.ok(adminUserService.getVeterinarians());
    }

    /** Lista todos los clientes */
    @GetMapping("/clients")
    public ResponseEntity<List<UserResponse>> getClients() {
        return ResponseEntity.ok(adminUserService.getClients());
    }

    /** Lista todas las mascotas */
    @GetMapping("/pets")
    public ResponseEntity<?> getPets() {
        return ResponseEntity.ok(adminUserService.getAllPets());
    }

    /** Crea usuario staff con contraseña temporal */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@RequestBody StaffUserRequest req) {
        return ResponseEntity.ok(adminUserService.createStaff(req));
    }

    /** Actualiza nombre y foto */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @RequestBody StaffUserRequest req) {
        return ResponseEntity.ok(adminUserService.updateStaff(id, req));
    }

    /** Activa / desactiva */
    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {
        adminUserService.toggleEnabled(id);
        return ResponseEntity.noContent().build();
    }

    /** Elimina */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Veterinarios activos — accesible por clientes para agendar citas */
    @GetMapping("/public/veterinarians")
    public ResponseEntity<List<UserResponse>> getVetsForClients() {
        return ResponseEntity.ok(adminUserService.getVeterinarians());
    }


}