package co.edu.uniquindio.backendpawsoft.repository;

/**
 * Repositorio JPA para tokens de verificación de correo electrónico.
 *
 * Permite buscar un token por su valor o por el usuario asociado,
 * usado en el flujo de activación de cuenta tras el registro.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.model.EmailVerificationToken;
import co.edu.uniquindio.backendpawsoft.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user);

    void deleteByUserId(Long userId);
}