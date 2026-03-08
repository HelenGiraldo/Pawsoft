package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad ServicePrice.
 *
 * Proporciona consultas para gestionar los precios base de los servicios veterinarios.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {

    Optional<ServicePrice> findByServiceType(String serviceType);

    /** Solo los activos, ordenados alfabéticamente (para la recepcionista) */
    List<ServicePrice> findByActiveTrueOrderByDisplayNameAsc();

    boolean existsByServiceType(String serviceType);
}