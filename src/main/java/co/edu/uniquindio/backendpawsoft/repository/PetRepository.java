package co.edu.uniquindio.backendpawsoft.repository;

/**
 * Repositorio JPA para la entidad {@link co.edu.uniquindio.backendpawsoft.model.Pet}.
 *
 * Provee consultas por correo del propietario y eliminación en cascada
 * cuando se borra un cliente del sistema.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwnerEmail(String ownerEmail);

    // Elimina todas las mascotas de un cliente — usado al borrar el cliente
    void deleteByOwnerEmail(String ownerEmail);
}