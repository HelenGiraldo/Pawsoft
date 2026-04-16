package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.PetMedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la hoja médica maestra de mascotas.
 */
@Repository
public interface PetMedicalProfileRepository extends JpaRepository<PetMedicalProfile, Long> {

    /**
     * Busca el perfil médico por ID de mascota.
     */
    Optional<PetMedicalProfile> findByPetId(Long petId);

    void deleteByPetId(Long petId);
}
