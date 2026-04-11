package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordResponse;
import co.edu.uniquindio.backendpawsoft.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para que el cliente consulte el resumen médico de sus citas.
 *
 * Endpoints:
 * - GET /api/cliente/medical-records/appointment/{id} → Resumen de una cita completada
 *
 * Requiere ROLE_CLIENTE (configurado en SecurityConfig: /api/cliente/**).
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@RestController
@RequestMapping("/api/cliente/medical-records")
@RequiredArgsConstructor
public class ClienteMedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Retorna todos los registros médicos del cliente autenticado.
     * GET /api/cliente/medical-records
     */
    @GetMapping
    public ResponseEntity<List<MedicalRecordResponse>> getMisRegistros(Authentication authentication) {
        return ResponseEntity.ok(
                medicalRecordService.getByCliente(authentication.getName())
        );
    }

    /**
     * Retorna el resumen médico de una cita completada del cliente autenticado.
     * GET /api/cliente/medical-records/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MedicalRecordResponse> getResumen(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        return ResponseEntity.ok(
                medicalRecordService.getResumenParaCliente(appointmentId, authentication.getName())
        );
    }
}