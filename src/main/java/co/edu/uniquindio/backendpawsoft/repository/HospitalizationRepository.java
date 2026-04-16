package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import co.edu.uniquindio.backendpawsoft.model.Hospitalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para hospitalizaciones.
 */
@Repository
public interface HospitalizationRepository extends JpaRepository<Hospitalization, Long> {

    /**
     * Busca todas las hospitalizaciones de una mascota.
     */
    List<Hospitalization> findByPetId(Long petId);

    /**
     * Busca todas las hospitalizaciones activas.
     */
    @Query("SELECT h FROM Hospitalization h WHERE h.status = 'ACTIVE'")
    List<Hospitalization> findActiveHospitalizations();

    /**
     * Busca hospitalizaciones activas de un veterinario específico.
     */
    @Query("SELECT h FROM Hospitalization h WHERE h.vet.id = :vetId AND h.status = 'ACTIVE'")
    List<Hospitalization> findActiveHospitalizationsByVet(@Param("vetId") Long vetId);

    /**
     * Busca todas las hospitalizaciones de un veterinario específico ordenadas por fecha de creación descendente.
     */
    List<Hospitalization> findByVetIdOrderByCreatedAtDesc(Long vetId);

    /**
     * Busca hospitalizaciones de una mascota con estado específico.
     */
    List<Hospitalization> findByPetIdAndStatus(Long petId, HospitalizationStatus status);

    /**
     * Busca la hospitalización activa de una mascota (si existe).
     */
    @Query("SELECT h FROM Hospitalization h WHERE h.pet.id = :petId AND h.status = 'ACTIVE'")
    Optional<Hospitalization> findActiveByPetId(@Param("petId") Long petId);

    void deleteByVetId(Long vetId);
}
