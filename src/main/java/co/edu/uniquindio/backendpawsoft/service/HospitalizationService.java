package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.HospitalizationDTO;
import co.edu.uniquindio.backendpawsoft.dto.HospitalizationNoteDTO;
import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import co.edu.uniquindio.backendpawsoft.model.*;
import co.edu.uniquindio.backendpawsoft.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar hospitalizaciones de mascotas.
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
@Service
@RequiredArgsConstructor
@Transactional
public class HospitalizationService {

    private final HospitalizationRepository hospitalizationRepository;
    private final HospitalizationNoteRepository noteRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    /**
     * Crea una nueva hospitalización.
     * 
     * @param petId ID de la mascota
     * @param vetId ID del veterinario responsable
     * @param appointmentId ID de la cita asociada (opcional)
     * @param reason Razón de la hospitalización
     * @param initialObservations Observaciones iniciales
     * @param hourlyRate Tarifa por hora de hospitalización
     * @return DTO con datos de la hospitalización creada
     */
    public HospitalizationDTO createHospitalization(
            Long petId,
            Long vetId,
            Long appointmentId,
            String reason,
            String initialObservations,
            BigDecimal hourlyRate
    ) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        
        User vet = userRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        
        Appointment appointment = null;
        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId)
                    .orElse(null);
        }
        
        Hospitalization hospitalization = Hospitalization.builder()
                .pet(pet)
                .vet(vet)
                .appointment(appointment)
                .status(HospitalizationStatus.ACTIVE)
                .admissionDate(LocalDateTime.now())
                .reason(reason)
                .initialObservations(initialObservations)
                .hourlyRate(hourlyRate)
                .build();
        
        Hospitalization saved = hospitalizationRepository.save(hospitalization);
        
        // Registrar en auditoría
        auditLogService.log(
                "HOSPITALIZATION_CREATED",
                "Hospitalización creada para mascota: " + pet.getName(),
                "Hospitalization",
                saved.getId().intValue()
        );
        
        return toDTO(saved);
    }

    /**
     * Obtiene una hospitalización por ID.
     * 
     * @param id ID de la hospitalización
     * @return DTO con datos de la hospitalización
     */
    @Transactional(readOnly = true)
    public HospitalizationDTO getHospitalization(Long id) {
        Hospitalization hospitalization = hospitalizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
        
        return toDTO(hospitalization);
    }

    /**
     * Obtiene todas las hospitalizaciones de una mascota.
     * 
     * @param petId ID de la mascota
     * @return Lista de hospitalizaciones de la mascota
     */
    @Transactional(readOnly = true)
    public List<HospitalizationDTO> getHospitalizationsByPet(Long petId) {
        return hospitalizationRepository.findByPetId(petId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las hospitalizaciones del sistema (sin filtrar por veterinario).
     * Usado por el administrador para vista de solo lectura.
     *
     * @return Lista de todas las hospitalizaciones ordenadas por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<HospitalizationDTO> getAllHospitalizations() {
        return hospitalizationRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las hospitalizaciones activas.
     * 
     * @return Lista de hospitalizaciones con estado ACTIVE
     */
    @Transactional(readOnly = true)
    public List<HospitalizationDTO> getActiveHospitalizations() {
        return hospitalizationRepository.findActiveHospitalizations()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las hospitalizaciones activas de un veterinario.
     * 
     * @param vetId ID del veterinario
     * @return Lista de hospitalizaciones activas del veterinario
     */
    @Transactional(readOnly = true)
    public List<HospitalizationDTO> getActiveHospitalizationsByVet(Long vetId) {
        return hospitalizationRepository.findActiveHospitalizationsByVet(vetId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las hospitalizaciones de un veterinario (activas, dadas de alta y fallecidas).
     * 
     * @param vetId ID del veterinario
     * @return Lista de todas las hospitalizaciones del veterinario ordenadas por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<HospitalizationDTO> getAllHospitalizationsByVet(Long vetId) {
        return hospitalizationRepository.findByVetIdOrderByCreatedAtDesc(vetId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cambia el estado de una hospitalización.
     * 
     * @param id ID de la hospitalización
     * @param newStatus Nuevo estado
     * @param vet Veterinario que realiza el cambio
     * @return DTO con la hospitalización actualizada
     */
    public HospitalizationDTO updateStatus(Long id, HospitalizationStatus newStatus, User vet) {
        Hospitalization hospitalization = hospitalizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
        
        HospitalizationStatus oldStatus = hospitalization.getStatus();
        hospitalization.setStatus(newStatus);
        
        // Si se da de alta, registrar fecha de alta
        if (newStatus == HospitalizationStatus.DISCHARGED && hospitalization.getDischargeDate() == null) {
            hospitalization.setDischargeDate(LocalDateTime.now());
        }
        
        Hospitalization updated = hospitalizationRepository.save(hospitalization);
        
        // Registrar en auditoría
        auditLogService.log(
                "HOSPITALIZATION_STATUS_CHANGED",
                "Estado cambió de " + oldStatus + " a " + newStatus,
                "Hospitalization",
                updated.getId().intValue()
        );
        
        return toDTO(updated);
    }

    /**
     * Registra el fallecimiento de una mascota en hospitalización.
     * 
     * @param id ID de la hospitalización
     * @param causeOfDeath Causa del fallecimiento
     * @param vet Veterinario que registra el fallecimiento
     * @return DTO con la hospitalización actualizada
     */
    public HospitalizationDTO recordDeceased(Long id, String causeOfDeath, User vet) {
        Hospitalization hospitalization = hospitalizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
        
        hospitalization.setStatus(HospitalizationStatus.DECEASED);
        hospitalization.setCauseOfDeath(causeOfDeath);
        hospitalization.setDischargeDate(LocalDateTime.now());
        
        Hospitalization updated = hospitalizationRepository.save(hospitalization);
        
        // Registrar en auditoría
        auditLogService.log(
                "HOSPITALIZATION_DECEASED",
                "Mascota fallecida. Causa: " + causeOfDeath,
                "Hospitalization",
                updated.getId().intValue()
        );
        
        return toDTO(updated);
    }

    /**
     * Agrega una nota de evolución a una hospitalización.
     * 
     * @param hospitalizationId ID de la hospitalización
     * @param noteText Contenido de la nota
     * @param vet Veterinario que agrega la nota
     * @return DTO con la nota creada
     */
    public HospitalizationNoteDTO addNote(Long hospitalizationId, String noteText, User vet) {
        Hospitalization hospitalization = hospitalizationRepository.findById(hospitalizationId)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
        
        HospitalizationNote note = HospitalizationNote.builder()
                .hospitalization(hospitalization)
                .vet(vet)
                .note(noteText)
                .build();
        
        HospitalizationNote saved = noteRepository.save(note);
        
        // Registrar en auditoría
        auditLogService.log(
                "HOSPITALIZATION_NOTE_ADDED",
                "Nota de evolución agregada",
                "HospitalizationNote",
                saved.getId().intValue()
        );
        
        return toNoteDTO(saved);
    }

    /**
     * Obtiene todas las notas de una hospitalización ordenadas por fecha descendente.
     * 
     * @param hospitalizationId ID de la hospitalización
     * @return Lista de notas de evolución
     */
    @Transactional(readOnly = true)
    public List<HospitalizationNoteDTO> getNotes(Long hospitalizationId) {
        return noteRepository.findByHospitalizationIdOrderByCreatedAtDesc(hospitalizationId)
                .stream()
                .map(this::toNoteDTO)
                .collect(Collectors.toList());
    }

    // ── Métodos auxiliares ──────────────────────────────────────────────────

    /**
     * Convierte una entidad Hospitalization a DTO.
     * 
     * @param h Entidad Hospitalization
     * @return DTO con información de la hospitalización
     */
    private HospitalizationDTO toDTO(Hospitalization h) {
        List<HospitalizationNoteDTO> notes = h.getNotes() != null ?
                h.getNotes().stream().map(this::toNoteDTO).collect(Collectors.toList()) :
                List.of();
        
        return HospitalizationDTO.builder()
                .id(h.getId())
                .petId(h.getPet().getId())
                .petName(h.getPet().getName())
                .petSpecies(h.getPet().getSpecies())
                .ownerEmail(h.getPet().getOwnerEmail())
                .vetId(h.getVet().getId())
                .vetName(h.getVet().getName())
                .appointmentId(h.getAppointment() != null ? h.getAppointment().getId() : null)
                .status(h.getStatus())
                .admissionDate(h.getAdmissionDate())
                .dischargeDate(h.getDischargeDate())
                .reason(h.getReason())
                .initialObservations(h.getInitialObservations())
                .hourlyRate(h.getHourlyRate())
                .causeOfDeath(h.getCauseOfDeath())
                .createdAt(h.getCreatedAt())
                .totalHours(h.getTotalHours())
                .totalCost(h.getTotalCost())
                .notes(notes)
                .build();
    }

    /**
     * Convierte una entidad HospitalizationNote a DTO.
     * 
     * @param n Entidad HospitalizationNote
     * @return DTO con información de la nota
     */
    private HospitalizationNoteDTO toNoteDTO(HospitalizationNote n) {
        return HospitalizationNoteDTO.builder()
                .id(n.getId())
                .hospitalizationId(n.getHospitalization().getId())
                .vetId(n.getVet().getId())
                .vetName(n.getVet().getName())
                .note(n.getNote())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
