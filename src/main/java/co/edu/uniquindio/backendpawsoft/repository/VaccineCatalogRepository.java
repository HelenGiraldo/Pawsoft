package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.VaccineCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaccineCatalogRepository extends JpaRepository<VaccineCatalog, Long> {
    
    List<VaccineCatalog> findByActiveTrueOrderByNameAsc();
    
    Optional<VaccineCatalog> findByName(String name);
    
    boolean existsByName(String name);
}
