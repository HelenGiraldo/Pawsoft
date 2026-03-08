package co.edu.uniquindio.backendpawsoft.repository;


import co.edu.uniquindio.backendpawsoft.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken>
    findByTokenAndUsedFalseAndExpirationDateAfter(
            String token,
            LocalDateTime now
    );

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    void deleteByUserId(Long userId);
}