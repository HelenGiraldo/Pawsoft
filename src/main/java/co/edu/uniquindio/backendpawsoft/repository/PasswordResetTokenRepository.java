package co.edu.uniquindio.backendpawsoft.repository;

/**
 * Repositorio JPA para tokens de recuperación de contraseña.
 *
 * Permite buscar tokens válidos (no usados y no expirados) y limpiar
 * tokens asociados a un usuario al eliminarlo.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */

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