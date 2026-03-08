package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwnerEmail(String ownerEmail);

    // Elimina todas las mascotas de un cliente — usado al borrar el cliente
    void deleteByOwnerEmail(String ownerEmail);
}