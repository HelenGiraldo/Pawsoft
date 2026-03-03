package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.AdminPetResponse;
import co.edu.uniquindio.backendpawsoft.dto.StaffUserRequest;
import co.edu.uniquindio.backendpawsoft.dto.UserResponse;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para operaciones del panel de administración:
 * - listar todos los usuarios del staff
 * - listar solo veterinarios (para selección al agendar citas)
 * - crear staff con foto
 * - actualizar foto de veterinario
 * - activar/desactivar usuario
 * - eliminar usuario
 *
 * Nota: la creación con contraseña temporal sigue usando UserService.createStaffUser()
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserService userService; // reutiliza createStaffUser()
    private final PetRepository petRepository;

    /** Lista todos los usuarios que NO son clientes */
    public List<UserResponse> getStaffUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ROLE_CLIENTE)
                .map(this::toResponse)
                .toList();
    }

    /** Solo veterinarios activos — para que el cliente los vea al agendar */
    public List<UserResponse> getVeterinarians() {
        return userRepository.findByRole(Role.ROLE_VETERINARIO).stream()
                .filter(User::isEnabled)
                .map(this::toResponse)
                .toList();
    }

    /**
     * Crea un usuario staff con contraseña temporal (reutiliza la lógica existente)
     * y opcionalmente guarda su foto.
     */
    public UserResponse createStaff(StaffUserRequest req) {
        // Usa el flujo existente (genera contraseña temporal + envía email)
        UserResponse created = userService.createStaffUser(req.getNombre(), req.getEmail(), req.getRole());

        // Si viene foto, la guarda en el usuario recién creado
        if (req.getPhotoUrl() != null && !req.getPhotoUrl().isBlank()) {
            userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
                u.setPhotoUrl(req.getPhotoUrl());
                userRepository.save(u);
            });
        }

        return created;
    }

    /** Actualiza nombre y foto de un usuario staff */
    public UserResponse updateStaff(Long id, StaffUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        user.setName(req.getNombre());

        if (req.getPhotoUrl() != null && !req.getPhotoUrl().isBlank())
            user.setPhotoUrl(req.getPhotoUrl());

        return toResponse(userRepository.save(user));
    }

    /** Activa o desactiva un usuario */
    public void toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    /** Elimina un usuario por id */
    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("Usuario no encontrado: " + id);
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .photoUrl(u.getPhotoUrl())
                .enabled(u.isEnabled())
                .build();
    }

    /** Lista todos los clientes registrados */
    public List<UserResponse> getClients() {
        return userRepository.findByRole(Role.ROLE_CLIENTE).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Lista todas las mascotas con datos del propietario */
    public List<AdminPetResponse> getAllPets() {
        return petRepository.findAll().stream()
                .map(p -> {
                    String ownerName = userRepository
                            .findByEmail(p.getOwnerEmail())
                            .map(u -> u.getName())
                            .orElse(p.getOwnerEmail());

                    return AdminPetResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .species(p.getSpecies())
                            .breed(p.getBreed())
                            .sex(p.getSex())
                            .birthDate(p.getBirthDate())
                            .photoUrl(p.getPhotoUrl())
                            .ownerName(ownerName)
                            .ownerEmail(p.getOwnerEmail())
                            .build();
                })
                .toList();
    }


}