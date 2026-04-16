package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.HospitalizationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para notas de evolución de hospitalizaciones.
 */
@Repository
public interface HospitalizationNoteRepository extends JpaRepository<HospitalizationNote, Long> {

    /**
     * Busca todas las notas de una hospitalización, ordenadas por fecha.
     */
    List<HospitalizationNote> findByHospitalizationIdOrderByCreatedAtDesc(Long hospitalizationId);
}
