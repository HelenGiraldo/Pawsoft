package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.MedicalAttachmentDTO;
import co.edu.uniquindio.backendpawsoft.enums.AttachmentReferenceType;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import co.edu.uniquindio.backendpawsoft.service.MedicalAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador para archivos adjuntos médicos.
 */
@RestController
@RequestMapping("/api/vet/attachments")
@RequiredArgsConstructor
public class MedicalAttachmentController {

    private final MedicalAttachmentService attachmentService;
    private final UserRepository userRepository;

    /**
     * POST /api/vet/attachments/upload
     * 
     * Sube un archivo adjunto (imagen o PDF).
     * 
     * Parámetros:
     * - file: archivo a subir
     * - referenceType: MEDICAL_RECORD o HOSPITALIZATION
     * - referenceId: ID del registro médico o hospitalización
     */
    @PostMapping("/upload")
    public ResponseEntity<MedicalAttachmentDTO> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("referenceType") AttachmentReferenceType referenceType,
            @RequestParam("referenceId") Long referenceId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            MedicalAttachmentDTO attachment = attachmentService.uploadAttachment(
                    file,
                    referenceType,
                    referenceId,
                    user
            );
            return ResponseEntity.ok(attachment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/vet/attachments
     * 
     * Obtiene todos los archivos adjuntos de una referencia.
     * 
     * Parámetros:
     * - referenceType: MEDICAL_RECORD o HOSPITALIZATION
     * - referenceId: ID del registro médico o hospitalización
     */
    @GetMapping
    public ResponseEntity<List<MedicalAttachmentDTO>> getAttachments(
            @RequestParam("referenceType") AttachmentReferenceType referenceType,
            @RequestParam("referenceId") Long referenceId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        List<MedicalAttachmentDTO> attachments = attachmentService.getAttachments(referenceType, referenceId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * DELETE /api/vet/attachments/{id}
     * 
     * Elimina un archivo adjunto.
     * Solo el usuario que lo subió puede eliminarlo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!user.getRole().name().equals("ROLE_VETERINARIO")) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            attachmentService.deleteAttachment(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
