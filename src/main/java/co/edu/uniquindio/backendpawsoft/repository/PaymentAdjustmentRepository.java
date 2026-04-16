package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.PaymentAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAdjustmentRepository extends JpaRepository<PaymentAdjustment, Long> {
    
    List<PaymentAdjustment> findAllByOrderByAdjustedAtDesc();
}
