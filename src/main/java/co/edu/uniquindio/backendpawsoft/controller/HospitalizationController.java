package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.HospitalizationDTO;
import co.edu.uniquindio.backendpawsoft.dto.HospitalizationNoteDTO;
import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.service.HospitalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para hospitalizaciones.
 */
@RestController
@RequestMapping("/api/vet/hospitalizations")
@RequiredArgsConstructor
public class HospitalizationController {

    private final HospitalizationService hospitalizationService;
    private final UserRepository userRepository;

    /**
     * POST /api/vet/hospitalizations
     * 
     * Crea una nueva hospitalización.
     */
    @PostMapping
    public ResponseEntity<HospitalizationDTO> createHospitalization(
            @RequestBody CreateHospitalizationRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationDTO hospitalization = hospitalizationService.createHospitalization(
                request.getPetId(),
                user.getId(),
                request.getAppointmentId(),
                request.getReason(),
                request.getInitialObservations(),
                request.getHourlyRate()
        );
        
        return ResponseEntity.ok(hospitalization);
    }

    /**
     * GET /api/vet/hospitalizations/{id}
     * 
     * Obtiene los detalles de una hospitalización.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HospitalizationDTO> getHospitalization(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationDTO hospitalization = hospitalizationService.getHospitalization(id);
        return ResponseEntity.ok(hospitalization);
    }

    /**
     * GET /api/vet/hospitalizations/active
     * 
     * Obtiene todas las hospitalizaciones activas del veterinario.
     */
    @GetMapping("/active")
    public ResponseEntity<List<HospitalizationDTO>> getActiveHospitalizations(
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        List<HospitalizationDTO> hospitalizations = hospitalizationService.getActiveHospitalizationsByVet(user.getId());
        return ResponseEntity.ok(hospitalizations);
    }

    /**
     * GET /api/vet/hospitalizations
     * 
     * Obtiene todas las hospitalizaciones del veterinario (activas, dadas de alta y fallecidas).
     */
    @GetMapping
    public ResponseEntity<List<HospitalizationDTO>> getAllHospitalizations(
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        List<HospitalizationDTO> hospitalizations = hospitalizationService.getAllHospitalizationsByVet(user.getId());
        return ResponseEntity.ok(hospitalizations);
    }

    /**
     * PUT /api/vet/hospitalizations/{id}/status
     * 
     * Cambia el estado de una hospitalización.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<HospitalizationDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationDTO updated = hospitalizationService.updateStatus(
                id,
                request.getStatus(),
                user
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/vet/hospitalizations/{id}/notes
     * 
     * Agrega una nota de evolución a una hospitalización.
     */
    @PostMapping("/{id}/notes")
    public ResponseEntity<HospitalizationNoteDTO> addNote(
            @PathVariable Long id,
            @RequestBody AddNoteRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationNoteDTO note = hospitalizationService.addNote(id, request.getNote(), user);
        return ResponseEntity.ok(note);
    }

    /**
     * PUT /api/vet/hospitalizations/{id}/discharge
     * 
     * Da de alta una mascota (cambia estado a DADO_DE_ALTA).
     */
    @PutMapping("/{id}/discharge")
    public ResponseEntity<HospitalizationDTO> dischargeHospitalization(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationDTO updated = hospitalizationService.updateStatus(
                id,
                HospitalizationStatus.DISCHARGED,
                user
        );
        
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/vet/hospitalizations/{id}/deceased
     * 
     * Registra el fallecimiento de una mascota.
     */
    @PutMapping("/{id}/deceased")
    public ResponseEntity<HospitalizationDTO> recordDeceased(
            @PathVariable Long id,
            @RequestBody DeceasedRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        HospitalizationDTO updated = hospitalizationService.recordDeceased(
                id,
                request.getCauseOfDeath(),
                user
        );
        
        return ResponseEntity.ok(updated);
    }

    // ── DTOs ────────────────────────────────────────────────────────────────

    public static class CreateHospitalizationRequest {
        private Long petId;
        private Long appointmentId;
        private String reason;
        private String initialObservations;
        private BigDecimal hourlyRate;

        public Long getPetId() { return petId; }
        public void setPetId(Long petId) { this.petId = petId; }

        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getInitialObservations() { return initialObservations; }
        public void setInitialObservations(String initialObservations) { this.initialObservations = initialObservations; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    }

    public static class UpdateStatusRequest {
        private HospitalizationStatus status;

        public HospitalizationStatus getStatus() { return status; }
        public void setStatus(HospitalizationStatus status) { this.status = status; }
    }

    public static class AddNoteRequest {
        private String note;

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class DeceasedRequest {
        private String causeOfDeath;

        public String getCauseOfDeath() { return causeOfDeath; }
        public void setCauseOfDeath(String causeOfDeath) { this.causeOfDeath = causeOfDeath; }
    }
}
