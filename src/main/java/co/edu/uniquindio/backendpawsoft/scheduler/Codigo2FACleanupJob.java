package co.edu.uniquindio.backendpawsoft.scheduler;

import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tarea programada encargada de mantener limpia la tabla {@code codigos_2fa}.
 *
 * Se aplican dos estrategias de limpieza diferenciadas según el propósito de cada registro:
 *
 * 1. Limpieza de registros no usados (basura):
 *    Elimina códigos que nunca fueron procesados y tienen más de 24 horas.
 *    Corresponde a sesiones abandonadas donde el usuario no llegó a verificar
 *    ni solicitó un nuevo código. No tienen valor de auditoría.
 *
 * 2. Limpieza de registros de auditoría:
 *    Elimina códigos ya procesados (usados, fallidos, expirados, invalidados)
 *    con más de 30 días de antigüedad. Se conservan durante ese período para
 *    permitir trazabilidad, revisión de incidentes y reportes de seguridad.
 *
 * Ambas tareas se ejecutan todos los días a las 3:00 AM para minimizar
 * el impacto en el rendimiento del sistema durante horas de uso activo.
 *
 * Requiere {@code @EnableScheduling} en la clase principal de Spring Boot.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Codigo2FACleanupJob {

    private final Codigo2FARepository codigo2FARepository;

    /**
     * Elimina códigos 2FA no usados con más de 24 horas de antigüedad.
     *
     * Estos registros corresponden a flujos de login abandonados donde el usuario
     * no llegó a verificar el código ni solicitó uno nuevo. Al no estar procesados,
     * no aportan información de auditoría y pueden eliminarse sin pérdida de trazabilidad.
     *
     * Cron: {@code 0 0 3 * * *} → todos los días a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarCodigosNoUsados() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        log.info("[2FA Cleanup] Eliminando códigos no usados anteriores a {}", limite);

        codigo2FARepository.deleteNoUsadosAntiguos(limite);

        log.info("[2FA Cleanup] Limpieza de códigos no usados completada.");
    }

    /**
     * Elimina registros de auditoría de códigos 2FA con más de 30 días de antigüedad.
     *
     * Estos registros (con {@code usado = true}) corresponden a intentos exitosos,
     * fallidos, expirados o invalidados. Se conservan 30 días para auditoría
     * y luego se eliminan para controlar el crecimiento de la tabla.
     *
     * Solo se eliminan registros con {@code fechaUso} definida, lo que garantiza
     * que nunca se borre accidentalmente un código todavía activo.
     *
     * Cron: {@code 0 0 3 * * *} → todos los días a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarAuditoriaAntigua() {
        LocalDateTime limiteAuditoria = LocalDateTime.now().minusDays(30);
        log.info("[2FA Cleanup] Eliminando registros de auditoría anteriores a {}", limiteAuditoria);

        codigo2FARepository.deleteUsadosAntiguos(limiteAuditoria);

        log.info("[2FA Cleanup] Limpieza de auditoría completada.");
    }
}