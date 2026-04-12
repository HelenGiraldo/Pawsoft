package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.MedicationCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationCatalogRepository extends JpaRepository<MedicationCatalog, Long> {
    
    List<MedicationCatalog> findByActiveTrueOrderByNameAsc();
    
    Optional<MedicationCatalog> findByName(String name);
    
    boolean existsByName(String name);
}
