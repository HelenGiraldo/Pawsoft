package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
    
    List<PaymentItem> findByPaymentId(Long paymentId);
}
