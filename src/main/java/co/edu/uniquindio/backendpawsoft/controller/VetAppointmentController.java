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
import co.edu.uniquindio.backendpawsoft.dto.RecepAppointmentResponse;
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
}
