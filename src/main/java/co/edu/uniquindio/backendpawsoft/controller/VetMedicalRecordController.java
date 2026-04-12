package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordRequest;
import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordResponse;
import co.edu.uniquindio.backendpawsoft.dto.MedicationCatalogResponse;
import co.edu.uniquindio.backendpawsoft.service.CatalogService;
import co.edu.uniquindio.backendpawsoft.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el módulo de registros médicos del veterinario.
 *
 * Endpoints:
 * - POST /api/vet/medical-records?cerrar=false  → Guardar borrador
 * - POST /api/vet/medical-records?cerrar=true   → Cerrar atención (marca cita COMPLETED)
 * - GET  /api/vet/medical-records               → Historial del veterinario (solo COMPLETED)
 * - GET  /api/vet/medical-records/appointment/{id} → Registro de una cita específica
 *
 * Todos los endpoints requieren ROLE_VETERINARIO (configurado en SecurityConfig).
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@RestController
@RequestMapping("/api/vet/medical-records")
@RequiredArgsConstructor
public class VetMedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final CatalogService catalogService;

    /**
     * Guarda el registro médico (borrador o cierre de atención).
     * Si cerrar=true, cambia el estado de la cita a COMPLETED.
     *
     * POST /api/vet/medical-records?cerrar=false
     * POST /api/vet/medical-records?cerrar=true
     */
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> guardar(
            @Valid @RequestBody MedicalRecordRequest request,
            @RequestParam(defaultValue = "false") boolean cerrar,
            Authentication authentication) {

        String vetEmail = authentication.getName();
        return ResponseEntity.ok(medicalRecordService.guardar(request, vetEmail, cerrar));
    }

    /**
     * Retorna el historial clínico del veterinario autenticado.
     * Solo incluye registros de citas con estado COMPLETED.
     *
     * GET /api/vet/medical-records
     */
    @GetMapping
    public ResponseEntity<List<MedicalRecordResponse>> getHistorial(Authentication authentication) {
        return ResponseEntity.ok(medicalRecordService.getByVet(authentication.getName()));
    }

    /**
     * Retorna el registro médico de una cita específica.
     *
     * GET /api/vet/medical-records/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MedicalRecordResponse> getByAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(medicalRecordService.getByAppointment(appointmentId));
    }

    /**
     * Retorna el catálogo de medicamentos activos con sus precios.
     * El vet lo usa para seleccionar medicamentos con precio en el formulario.
     *
     * GET /api/vet/medical-records/medications
     */
    @GetMapping("/medications")
    public ResponseEntity<List<MedicationCatalogResponse>> getMedications() {
        return ResponseEntity.ok(catalogService.getActiveMedications());
    }
}