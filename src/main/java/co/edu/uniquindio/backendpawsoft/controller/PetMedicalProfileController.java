package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.PetMedicalProfileDTO;
import co.edu.uniquindio.backendpawsoft.dto.UpdateMedicalProfileRequest;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.service.PetMedicalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la hoja médica maestra de mascotas.
 */
@RestController
@RequestMapping("/api/vet/pets")
@RequiredArgsConstructor
public class PetMedicalProfileController {

    private final PetMedicalProfileService profileService;
    private final UserRepository userRepository;

    /**
     * GET /api/vet/pets/{id}/medical-profile
     * 
     * Obtiene la hoja médica maestra de una mascota.
     * Se devuelve en el modal previo a iniciar atención.
     */
    @GetMapping("/{id}/medical-profile")
    public ResponseEntity<PetMedicalProfileDTO> getMedicalProfile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        // Validar que el usuario sea veterinario
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        PetMedicalProfileDTO profile = profileService.getProfileDTO(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/vet/pets/{id}/medical-profile
     * 
     * Actualiza la hoja médica maestra después de una consulta.
     * Se llama automáticamente cuando el veterinario cierra la atención.
     * 
     * Lógica de actualización:
     * - Alergias: se concatenan sin duplicados
     * - Condiciones: se concatenan sin duplicados
     * - Medicamentos: se reemplazan
     * - Tipo de sangre: se reemplaza (raro)
     */
    @PutMapping("/{id}/medical-profile")
    public ResponseEntity<PetMedicalProfileDTO> updateMedicalProfile(
            @PathVariable Long id,
            @RequestBody UpdateMedicalProfileRequest request,
            Authentication authentication
    ) {
        // Validar que el usuario sea veterinario
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        profileService.updateProfileAfterConsultation(
                id,
                request.getAllergiesFound(),
                request.getConditionsFound(),
                request.getSurgicalNote(),
                request.getCurrentMeds(),
                request.getBloodType(),
                user
        );
        
        PetMedicalProfileDTO updated = profileService.getProfileDTO(id);
        return ResponseEntity.ok(updated);
    }

    // ── DTOs ────────────────────────────────────────────────────────────────

    public static class UpdateMedicalProfileRequest {
        private String allergiesFound;
        private String conditionsFound;
        private String surgicalNote;
        private String currentMeds;
        private String bloodType;

        public String getAllergiesFound() {
            return allergiesFound;
        }

        public void setAllergiesFound(String allergiesFound) {
            this.allergiesFound = allergiesFound;
        }

        public String getConditionsFound() {
            return conditionsFound;
        }

        public void setConditionsFound(String conditionsFound) {
            this.conditionsFound = conditionsFound;
        }

        public String getSurgicalNote() {
            return surgicalNote;
        }

        public void setSurgicalNote(String surgicalNote) {
            this.surgicalNote = surgicalNote;
        }

        public String getCurrentMeds() {
            return currentMeds;
        }

        public void setCurrentMeds(String currentMeds) {
            this.currentMeds = currentMeds;
        }

        public String getBloodType() {
            return bloodType;
        }

        public void setBloodType(String bloodType) {
            this.bloodType = bloodType;
        }
    }
}
