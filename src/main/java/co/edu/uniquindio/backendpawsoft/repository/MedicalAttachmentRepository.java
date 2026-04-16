package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.enums.AttachmentReferenceType;
import co.edu.uniquindio.backendpawsoft.model.MedicalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para archivos adjuntos médicos.
 */
@Repository
public interface MedicalAttachmentRepository extends JpaRepository<MedicalAttachment, Long> {

    /**
     * Busca todos los archivos adjuntos de una referencia específica.
     */
    List<MedicalAttachment> findByReferenceTypeAndReferenceId(
            AttachmentReferenceType referenceType,
            Long referenceId
    );

    /**
     * Busca archivos adjuntos de un registro médico.
     */
    List<MedicalAttachment> findByReferenceIdAndReferenceType(
            Long referenceId,
            AttachmentReferenceType referenceType
    );

    void deleteByUploadedById(Long userId);
}
