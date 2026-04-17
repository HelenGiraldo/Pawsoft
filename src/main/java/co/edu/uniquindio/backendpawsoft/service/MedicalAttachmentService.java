package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.MedicalAttachmentDTO;
import co.edu.uniquindio.backendpawsoft.enums.AttachmentFileType;
import co.edu.uniquindio.backendpawsoft.enums.AttachmentReferenceType;
import co.edu.uniquindio.backendpawsoft.model.MedicalAttachment;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.MedicalAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar archivos adjuntos médicos.
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
@Service
@RequiredArgsConstructor
@Transactional
public class MedicalAttachmentService {

    private final MedicalAttachmentRepository attachmentRepository;
    private final CloudinaryService cloudinaryService;
    private final AuditLogService auditLogService;

    /**
     * Sube un archivo adjunto (imagen o PDF) a Cloudinary.
     * 
     * Validaciones:
     * - Imágenes: JPG/PNG, máx 2MB
     * - PDFs: máx 5MB
     * 
     * @param file Archivo a subir
     * @param referenceType Tipo de referencia (MEDICAL_RECORD, HOSPITALIZATION, etc.)
     * @param referenceId ID de la referencia
     * @param uploadedBy Usuario que sube el archivo
     * @return DTO con información del archivo subido
     */
    public MedicalAttachmentDTO uploadAttachment(
            MultipartFile file,
            AttachmentReferenceType referenceType,
            Long referenceId,
            User uploadedBy
    ) {
        // Validar tipo de archivo
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        
        AttachmentFileType fileType;
        
        if (contentType != null && contentType.startsWith("image/")) {
            // Es una imagen
            if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                throw new RuntimeException("Solo se permiten imágenes JPG o PNG");
            }
            if (fileSize > 2 * 1024 * 1024) { // 2MB
                throw new RuntimeException("Las imágenes no pueden exceder 2MB");
            }
            fileType = AttachmentFileType.IMAGE;
        } else if (contentType != null && contentType.equals("application/pdf")) {
            // Es un PDF
            if (fileSize > 5 * 1024 * 1024) { // 5MB
                throw new RuntimeException("Los PDFs no pueden exceder 5MB");
            }
            fileType = AttachmentFileType.PDF;
        } else {
            throw new RuntimeException("Tipo de archivo no permitido. Solo imágenes (JPG/PNG) o PDFs");
        }
        
        // Subir a Cloudinary
        String fileUrl = cloudinaryService.uploadFile(file, "medical-attachments");
        
        // Guardar registro en BD
        MedicalAttachment attachment = MedicalAttachment.builder()
                .referenceType(referenceType)
                .referenceId(referenceId)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .fileName(fileName)
                .uploadedBy(uploadedBy)
                .build();
        
        MedicalAttachment saved = attachmentRepository.save(attachment);
        
        // Registrar en auditoría
        auditLogService.log(
                "MEDICAL_ATTACHMENT_UPLOADED",
                "Archivo adjunto: " + fileName,
                "MedicalAttachment",
                saved.getId().intValue()
        );
        
        return toDTO(saved);
    }

    /**
     * Obtiene todos los archivos adjuntos de una referencia.
     * 
     * @param referenceType Tipo de referencia
     * @param referenceId ID de la referencia
     * @return Lista de archivos adjuntos
     */
    @Transactional(readOnly = true)
    public List<MedicalAttachmentDTO> getAttachments(
            AttachmentReferenceType referenceType,
            Long referenceId
    ) {
        return attachmentRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un archivo adjunto.
     * Solo el usuario que lo subió puede eliminarlo.
     * 
     * @param attachmentId ID del archivo a eliminar
     * @param user Usuario que solicita la eliminación
     */
    public void deleteAttachment(Long attachmentId, User user) {
        MedicalAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
        
        // Verificar que el usuario sea quien subió el archivo
        if (!attachment.getUploadedBy().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este archivo");
        }
        
        // Eliminar de Cloudinary
        cloudinaryService.deleteFile(attachment.getFileUrl());
        
        // Eliminar de BD
        attachmentRepository.delete(attachment);
        
        // Registrar en auditoría
        auditLogService.log(
                "MEDICAL_ATTACHMENT_DELETED",
                "Archivo eliminado: " + attachment.getFileName(),
                "MedicalAttachment",
                attachmentId.intValue()
        );
    }

    // ── Métodos auxiliares ──────────────────────────────────────────────────

    /**
     * Convierte una entidad MedicalAttachment a DTO.
     * 
     * @param a Entidad MedicalAttachment
     * @return DTO con información del archivo
     */
    private MedicalAttachmentDTO toDTO(MedicalAttachment a) {
        return MedicalAttachmentDTO.builder()
                .id(a.getId())
                .referenceType(a.getReferenceType())
                .referenceId(a.getReferenceId())
                .fileUrl(a.getFileUrl())
                .fileType(a.getFileType())
                .fileName(a.getFileName())
                .uploadedById(a.getUploadedBy().getId())
                .uploadedByName(a.getUploadedBy().getName())
                .uploadedAt(a.getUploadedAt())
                .build();
    }
}
