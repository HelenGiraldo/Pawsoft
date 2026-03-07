package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {

    Optional<ServicePrice> findByServiceType(String serviceType);

    /** Solo los activos, ordenados alfabéticamente (para la recepcionista) */
    List<ServicePrice> findByActiveTrueOrderByDisplayNameAsc();

    boolean existsByServiceType(String serviceType);
}