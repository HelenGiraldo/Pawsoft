package co.edu.uniquindio.backendpawsoft.controller;

/**
 * Controlador REST para las operaciones del veterinario autenticado.
 *
 * Expone el endpoint para que el veterinario consulte sus citas asignadas.
 * La identidad del veterinario se extrae del JWT mediante {@link Authentication}.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.dto.PetResponse;
import co.edu.uniquindio.backendpawsoft.dto.RecepAppointmentResponse;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vet/appointments")
@RequiredArgsConstructor
public class VetAppointmentController {

    private final AppointmentService appointmentService;
    private final PetRepository petRepository;

    /**
     * Retorna todas las citas asignadas al veterinario autenticado.
     * GET /api/vet/appointments
     */
    @GetMapping
    public ResponseEntity<List<RecepAppointmentResponse>> getMyAppointments(
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(appointmentService.getAppointmentsByVet(email));
    }

    /**
     * Retorna las citas de hoy del veterinario autenticado.
     * GET /api/vet/appointments/today
     */
    @GetMapping("/today")
    public ResponseEntity<List<RecepAppointmentResponse>> getTodayAppointments(
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(appointmentService.getTodayAppointmentsByVet(email));
    }

    /**
     * Inicia la atención de una cita (CONFIRMED → IN_PROGRESS).
     * POST /api/vet/appointments/{id}/start
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        appointmentService.startAppointment(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancela una atención iniciada por error (IN_PROGRESS → CONFIRMED).
     * POST /api/vet/appointments/{id}/cancel-start
     */
    @PostMapping("/{id}/cancel-start")
    public ResponseEntity<Void> cancelStartedAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        appointmentService.cancelStartedAppointment(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Completa una atención médica (IN_PROGRESS → COMPLETED).
     * POST /api/vet/appointments/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        appointmentService.completeAppointment(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Limpia todas las citas en progreso del veterinario (IN_PROGRESS → CONFIRMED).
     * Útil para resetear después de pruebas o errores.
     * POST /api/vet/appointments/cleanup/in-progress
     */
    @PostMapping("/cleanup/in-progress")
    public ResponseEntity<Void> cleanupInProgressAppointments(
            Authentication authentication) {
        String email = authentication.getName();
        appointmentService.cleanupInProgressAppointments(email);
        return ResponseEntity.ok().build();
    }

    /**
     * Busca mascotas por nombre — usado al hospitalizar sin cita previa.
     * GET /api/vet/appointments/pets/search?name=Luna
     */
    @GetMapping("/pets/search")
    public ResponseEntity<List<PetResponse>> searchPets(@RequestParam String name) {
        List<PetResponse> pets = petRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(p -> PetResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .species(p.getSpecies())
                        .breed(p.getBreed())
                        .birthDate(p.getBirthDate())
                        .sex(p.getSex())
                        .ownerEmail(p.getOwnerEmail())
                        .photoUrl(p.getPhotoUrl())
                        .build())
                .toList();
        return ResponseEntity.ok(pets);
    }
}
