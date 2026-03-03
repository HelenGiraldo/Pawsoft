package co.edu.uniquindio.backendpawsoft.controller;

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
}
