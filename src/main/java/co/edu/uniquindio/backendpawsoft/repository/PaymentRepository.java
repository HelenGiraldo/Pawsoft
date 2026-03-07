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

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    List<Payment> findAllByOrderByCreatedAtDesc();

    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<Payment> findByClientEmailOrderByCreatedAtDesc(String clientEmail);

    long countByStatus(PaymentStatus status);

    /* ── Consultas para reportes del admin ────────────────────────── */

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

    /**
     * Agrupación de ingresos por concepto en un rango de fechas.
     * Retorna Object[] con [0]=concept (String), [1]=total (BigDecimal).
     */
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
}