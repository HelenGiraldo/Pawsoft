package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.AppointmentResponse;
import co.edu.uniquindio.backendpawsoft.dto.CreateAppointmentRequest;
import co.edu.uniquindio.backendpawsoft.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las operaciones
 * relacionadas con citas médicas del cliente autenticado.
 *
 * Expone endpoints para:
 * - Crear citas
 * - Consultar citas propias
 * - Cancelar citas
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
@RestController
@RequestMapping("/api/cliente/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        AppointmentResponse response =
                appointmentService.createAppointment(request, email);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByClient(email)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        appointmentService.cancelAppointment(id, email);

        return ResponseEntity.noContent().build();
    }

    // En AdminUserController o mejor en un AppointmentController
    @GetMapping("/slots")
    public ResponseEntity<List<String>> getOccupiedSlots(
            @RequestParam Long vetId,
            @RequestParam String date) {
        return ResponseEntity.ok(appointmentService.getOccupiedSlots(vetId, date));
    }
}