package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.AttachmentFileType;
import co.edu.uniquindio.backendpawsoft.enums.AttachmentReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para archivos adjuntos médicos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalAttachmentDTO {

    private Long id;

    private AttachmentReferenceType referenceType;

    private Long referenceId;

    private String fileUrl;

    private AttachmentFileType fileType;

    private String fileName;

    private Long uploadedById;

    private String uploadedByName;

    private LocalDateTime uploadedAt;
}
