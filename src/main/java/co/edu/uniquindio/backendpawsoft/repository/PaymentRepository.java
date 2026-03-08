package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import co.edu.uniquindio.backendpawsoft.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Payment.
 *
 * Proporciona consultas personalizadas para:
 * - Búsqueda de pagos por cita, cliente y estado
 * - Cálculo de ingresos por fecha, rango y concepto
 * - Estadísticas financieras para el panel de administración
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
    List<Payment> findAllByOrderByCreatedAtDesc();
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    List<Payment> findByClientEmailOrderByCreatedAtDesc(String clientEmail);
    long countByStatus(PaymentStatus status);
    void deleteByClientEmail(String clientEmail);  // ← nuevo

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'PAID'
          AND p.appointmentDate = :date
        """)
    BigDecimal sumPaidByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'PAID'
          AND p.appointmentDate >= :from
          AND p.appointmentDate <= :to
        """)
    BigDecimal sumPaidBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal sumAllPaid();

    @Query("""
        SELECT p.concept, COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'PAID'
          AND p.appointmentDate >= :from
          AND p.appointmentDate <= :to
        GROUP BY p.concept
        ORDER BY SUM(p.amount) DESC
        """)
    List<Object[]> revenueByConceptBetween(@Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    void deleteByPetName(String petName);
}