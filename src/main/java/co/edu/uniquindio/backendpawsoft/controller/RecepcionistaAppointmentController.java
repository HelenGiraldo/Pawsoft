package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el panel del recepcionista.
 *
 * Expone endpoints para:
 * - Listar todas las citas del sistema
 * - Crear citas en nombre de un cliente
 * - Editar citas existentes
 * - Confirmar citas
 * - Marcar inasistencia
 * - Cancelar citas con motivo
 *
 * Todos los endpoints requieren rol ROLE_RECEPCIONISTA o ROLE_ADMIN.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/api/recepcionista/appointments")
@RequiredArgsConstructor
public class RecepcionistaAppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Lista todas las citas del sistema.
     * GET /api/recepcionista/appointments
     */
    @GetMapping
    public ResponseEntity<List<RecepAppointmentResponse>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * Crea una nueva cita desde el panel del recepcionista.
     * POST /api/recepcionista/appointments
     */
    @PostMapping
    public ResponseEntity<RecepAppointmentResponse> create(
            @Valid @RequestBody RecepCreateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.recepCreateAppointment(request));
    }

    /**
     * Edita una cita existente.
     * PUT /api/recepcionista/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecepAppointmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecepUpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.recepUpdateAppointment(id, request));
    }

    /**
     * Confirma una cita (cambia estado a CONFIRMADA).
     * PUT /api/recepcionista/appointments/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id) {
        appointmentService.confirmAppointment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca inasistencia del cliente (cambia estado a NO_ASISTIO).
     * PUT /api/recepcionista/appointments/{id}/no-show
     */
    @PutMapping("/{id}/no-show")
    public ResponseEntity<Void> noShow(@PathVariable Long id) {
        appointmentService.markNoShow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela una cita con motivo opcional.
     * PUT /api/recepcionista/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) RecepCancelRequest request) {
        appointmentService.recepCancelAppointment(id, request);
        return ResponseEntity.noContent().build();
    }
}