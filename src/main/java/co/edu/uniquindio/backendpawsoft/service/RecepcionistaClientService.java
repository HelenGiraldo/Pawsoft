package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestión de clientes y mascotas desde el panel de recepcionista.
 *
 * Responsabilidades:
 * - CRUD de clientes (crear, actualizar, activar/desactivar, eliminar)
 * - CRUD de mascotas (crear, actualizar, eliminar)
 * - Generación de contraseñas temporales para nuevos clientes
 * - Eliminación en cascada respetando restricciones de FK
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
public class RecepcionistaClientService {

    private final UserRepository        userRepository;
    private final UserService           userService;
    private final PetRepository         petRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder       passwordEncoder;
    private final EmailService          emailService;
    private final PaymentRepository paymentRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogService auditLogService;
    private final PetMedicalProfileService medicalProfileService;
    private final PetMedicalProfileRepository medicalProfileRepository;
    private final EntityManager entityManager;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final MedicalAttachmentRepository medicalAttachmentRepository;
    private final HospitalizationRepository hospitalizationRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    // ═══════════════════════════════════════════════════════════════
    //  CLIENTES
    // ═══════════════════════════════════════════════════════════════

    /** Lista todos los clientes registrados en el sistema. */
    public List<UserResponse> getClients() {
        return userRepository.findByRole(Role.ROLE_CLIENTE)
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * Crea un nuevo cliente desde el panel del recepcionista.
     * Genera contraseña temporal y la envía al correo del cliente.
     */
    public UserResponse createClient(RecepCreateClientRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String rawPassword = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 6).toUpperCase()
                + (int)(Math.random() * 90 + 10) + "!";

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone() != null ? request.getPhone() : "")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_CLIENTE)
                .enabled(true)
                .primerAcceso(true)
                .build();

        User saved = userRepository.save(user);
        emailService.sendTemporaryPassword(saved.getEmail(), rawPassword);

        auditLogService.log("RECEP_CREATE_CLIENT", "Recepcionista creó nuevo cliente", "USER", saved.getId().intValue());

        return toUserResponse(saved);
    }

    /** Actualiza nombre, email y teléfono de un cliente existente. */
    public UserResponse updateClient(Long id, RecepUpdateClientRequest request) {
        User user = findClientById(id);

        user.setName(request.getNombre());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id))
                    throw new RuntimeException("El correo ya está en uso: " + request.getEmail());
            });
            user.setEmail(request.getEmail());
        }

        if (request.getTelefono() != null)
            user.setPhone(request.getTelefono());

        UserResponse response = toUserResponse(userRepository.save(user));

        auditLogService.log("RECEP_UPDATE_CLIENT", "Recepcionista actualizó datos de cliente", "USER", id.intValue());

        return response;
    }

    /** Activa o desactiva un cliente (toggle). */
    public void toggleClient(Long id) {
        User user = findClientById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String action = user.isEnabled() ? "RECEP_ENABLE_CLIENT" : "RECEP_DISABLE_CLIENT";
        auditLogService.log(action, "Recepcionista cambió estado del cliente", "USER", id.intValue());
    }

    /**
     * Elimina un cliente y TODOS sus datos relacionados.
     *
     * Orden obligatorio para respetar las FK
     * @Transactional garantiza que si algo falla, nada queda a medias.
     */
    @Transactional
    public void deleteClient(Long id) {
        User user = findClientById(id);

        // 1 — Citas primero (FK → users y FK → pets)
        appointmentRepository.deleteByClientId(id);
        entityManager.flush();

        // 2 — Pagos (después de citas para evitar FK constraint)
        paymentRepository.deleteByClientEmail(user.getEmail());
        entityManager.flush();

        // 3 — Tokens de reset de contraseña (FK → users)
        passwordResetTokenRepository.deleteByUserId(id);
        entityManager.flush();

        // 4 — Tokens de verificación de email (FK → users)
        emailVerificationTokenRepository.deleteByUserId(id);
        entityManager.flush();

        // 5 — Archivos médicos subidos por el usuario
        medicalAttachmentRepository.deleteByUploadedById(id);
        entityManager.flush();

        // 6 — Registros médicos creados por el usuario
        medicalRecordRepository.deleteByVetId(id);
        entityManager.flush();

        // 7 — Hospitalizaciones del usuario
        hospitalizationRepository.deleteByVetId(id);
        entityManager.flush();

        // 8 — Perfiles médicos de mascotas del cliente
        petRepository.findByOwnerEmail(user.getEmail())
                .forEach(pet -> medicalProfileRepository.deleteByPetId(pet.getId()));
        entityManager.flush();

        // 9 — Mascotas (FK → users)
        petRepository.deleteByOwnerEmail(user.getEmail());
        entityManager.flush();

        // 10 — Usuario
        userRepository.deleteById(id);
        entityManager.flush();

        auditLogService.log("RECEP_DELETE_CLIENT", "Recepcionista eliminó cliente y sus datos", "USER", id.intValue());
    }

    // ═══════════════════════════════════════════════════════════════
    //  MASCOTAS
    // ═══════════════════════════════════════════════════════════════

    /** Lista las mascotas de un cliente por su email. */
    public List<PetResponse> getPetsByOwner(String ownerEmail) {
        return petRepository.findByOwnerEmail(ownerEmail)
                .stream()
                .map(this::toPetResponse)
                .toList();
    }

    /** Agrega una nueva mascota a un cliente existente. */
    public PetResponse createPet(RecepPetRequest request) {
        User owner = userRepository.findByEmail(request.getOwnerEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Cliente no encontrado: " + request.getOwnerEmail()));

        Pet pet = Pet.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .sex(request.getSex())
                .birthDate(request.getBirthDate())
                .photoUrl(request.getPhotoUrl())
                .ownerEmail(request.getOwnerEmail())
                .build();

        Pet saved = petRepository.save(pet);

        // Crear perfil médico inicial si se proporcionó información médica
        if (request.getMedicalProfileInitial() != null) {
            CreateMedicalProfileInitialRequest medicalInfo = request.getMedicalProfileInitial();
            
            // Solo crear el perfil si al menos un campo tiene información
            if (hasAnyMedicalInfo(medicalInfo)) {
                medicalProfileService.createInitialProfile(
                    saved.getId(),
                    medicalInfo.getBloodType(),
                    medicalInfo.getKnownAllergies(),
                    medicalInfo.getChronicConditions(),
                    medicalInfo.getCurrentMedications(),
                    medicalInfo.getAdditionalNotes(),
                    owner
                );
            }
        }

        auditLogService.log("RECEP_CREATE_PET", "Recepcionista registró nueva mascota", "PET", saved.getId().intValue());

        return toPetResponse(saved);
    }

    /** Actualiza los datos de una mascota existente. */
    public PetResponse updatePet(Long id, RecepPetRequest request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada: " + id));

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setSex(request.getSex());
        pet.setBirthDate(request.getBirthDate());

        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isBlank())
            pet.setPhotoUrl(request.getPhotoUrl());

        PetResponse response = toPetResponse(petRepository.save(pet));

        auditLogService.log("RECEP_UPDATE_PET", "Recepcionista actualizó datos de mascota", "PET", id.intValue());

        return response;
    }

    /** Elimina una mascota del sistema. */
    @Transactional
    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada: " + id));

        // 1 — Perfil médico de la mascota
        medicalProfileRepository.deleteByPetId(id);
        entityManager.flush();

        // 2 — Pagos de las citas de esta mascota (por appointmentId, no por nombre)
        appointmentRepository.findByPetId(id)
                .forEach(a -> paymentRepository.deleteByAppointmentId(a.getId()));
        entityManager.flush();

        // 3 — Citas de esta mascota (FK appointments.pet_id → pets.id)
        appointmentRepository.deleteByPetId(id);
        entityManager.flush();

        // 4 — Mascota
        petRepository.deleteById(id);
        entityManager.flush();

        auditLogService.log("RECEP_DELETE_PET", "Recepcionista eliminó mascota", "PET", id.intValue());
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ═══════════════════════════════════════════════════════════════

    private User findClientById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        if (user.getRole() != Role.ROLE_CLIENTE)
            throw new RuntimeException("El usuario no es un cliente: " + id);
        return user;
    }
    
    private boolean hasAnyMedicalInfo(CreateMedicalProfileInitialRequest medicalInfo) {
        return (medicalInfo.getBloodType() != null && !medicalInfo.getBloodType().trim().isEmpty()) ||
               (medicalInfo.getKnownAllergies() != null && !medicalInfo.getKnownAllergies().trim().isEmpty()) ||
               (medicalInfo.getChronicConditions() != null && !medicalInfo.getChronicConditions().trim().isEmpty()) ||
               (medicalInfo.getCurrentMedications() != null && !medicalInfo.getCurrentMedications().trim().isEmpty()) ||
               (medicalInfo.getAdditionalNotes() != null && !medicalInfo.getAdditionalNotes().trim().isEmpty());
    }

    private UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().name())
                .photoUrl(u.getPhotoUrl())
                .enabled(u.isEnabled())
                .build();
    }

    private PetResponse toPetResponse(Pet p) {
        // Verificar si la mascota está fallecida
        Boolean isDeceased = hospitalizationRepository
                .findByPetIdAndStatus(p.getId(), HospitalizationStatus.DECEASED)
                .stream()
                .findAny()
                .isPresent();

        // Verificar si la mascota está hospitalizada (activa)
        Boolean isHospitalized = hospitalizationRepository
                .findByPetIdAndStatus(p.getId(), HospitalizationStatus.ACTIVE)
                .stream()
                .findAny()
                .isPresent();

        return PetResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .species(p.getSpecies())
                .breed(p.getBreed())
                .sex(p.getSex())
                .birthDate(p.getBirthDate())
                .photoUrl(p.getPhotoUrl())
                .ownerEmail(p.getOwnerEmail())
                .isDeceased(isDeceased)
                .isHospitalized(isHospitalized)
                .build();
    }
}