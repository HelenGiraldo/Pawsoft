package co.edu.uniquindio.backendpawsoft.scheduler;

import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tarea programada encargada de limpiar los códigos 2FA
 * que ya superaron las 24 horas de antigüedad.
 *
 * Esto mantiene la tabla liviana sin perder trazabilidad reciente.
 * Se ejecuta automáticamente todos los días a las 3:00 AM.
 *
 * Requiere @EnableScheduling en la clase principal de Spring Boot.
 */
@Component
@RequiredArgsConstructor
public class Codigo2FACleanupJob {

    private final Codigo2FARepository codigo2FARepository;

    /**
     * Elimina registros de códigos 2FA con más de 24 horas de antigüedad.
     * Cron: segundos minutos horas * * *  → 0 0 3 * * * = todos los días a las 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarCodigosAntiguos() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        codigo2FARepository.deleteAntiguos(limite);
    }
}