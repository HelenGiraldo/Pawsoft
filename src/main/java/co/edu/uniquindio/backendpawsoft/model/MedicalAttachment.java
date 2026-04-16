package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.AttachmentFileType;
import co.edu.uniquindio.backendpawsoft.enums.AttachmentReferenceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Archivo adjunto a un registro médico o hospitalización.
 * Soporta imágenes (JPG/PNG, máx 2MB) y PDFs (máx 5MB).
 */
@Entity
@Table(name = "medical_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de referencia: MEDICAL_RECORD o HOSPITALIZATION
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private AttachmentReferenceType referenceType;

    /**
     * ID de la referencia (medical_record_id o hospitalization_id).
     */
    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    /**
     * URL del archivo en Cloudinary.
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /**
     * Tipo de archivo: IMAGE o PDF
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private AttachmentFileType fileType;

    /**
     * Nombre original del archivo.
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * Veterinario que subió el archivo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /**
     * Fecha y hora de carga.
     */
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
