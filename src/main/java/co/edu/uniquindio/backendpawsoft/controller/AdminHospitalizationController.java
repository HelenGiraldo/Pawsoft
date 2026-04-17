package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.HospitalizationDTO;
import co.edu.uniquindio.backendpawsoft.service.HospitalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para que el administrador consulte hospitalizaciones (solo lectura).
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
@RestController
@RequestMapping("/api/admin/hospitalizations")
@RequiredArgsConstructor
public class AdminHospitalizationController {

    private final HospitalizationService hospitalizationService;

    /**
     * GET /api/admin/hospitalizations
     *
     * Devuelve todas las hospitalizaciones del sistema (activas, dadas de alta y fallecidas).
     * Solo accesible por ROLE_ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<HospitalizationDTO>> getAllHospitalizations() {
        List<HospitalizationDTO> hospitalizations = hospitalizationService.getAllHospitalizations();
        return ResponseEntity.ok(hospitalizations);
    }

    /**
     * GET /api/admin/hospitalizations/{id}
     *
     * Devuelve el detalle de una hospitalización específica.
     * Solo accesible por ROLE_ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<HospitalizationDTO> getHospitalization(@PathVariable Long id) {
        HospitalizationDTO hospitalization = hospitalizationService.getHospitalization(id);
        return ResponseEntity.ok(hospitalization);
    }
}
