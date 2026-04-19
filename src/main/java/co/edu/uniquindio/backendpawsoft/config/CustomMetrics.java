package co.edu.uniquindio.backendpawsoft.config;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Componente para exponer métricas personalizadas de negocio
 * que Prometheus puede consumir para el dashboard de Grafana.
 *
 * Estas métricas complementan las métricas HTTP estándar de Spring Boot
 * con información específica del estado de la aplicación.
 */
@Component
@RequiredArgsConstructor
public class CustomMetrics {

    private final AppointmentRepository appointmentRepository;
    private final MeterRegistry meterRegistry;

    /**
     * Registra métricas personalizadas después de que Spring inicialice el bean.
     */
    @PostConstruct
    public void registerMetrics() {
        // Métrica: número actual de citas en estado IN_PROGRESS
        Gauge.builder("appointments_in_progress", this, CustomMetrics::getAppointmentsInProgress)
                .description("Número actual de citas en estado IN_PROGRESS")
                .register(meterRegistry);

        // Métrica: número de citas completadas hoy
        Gauge.builder("appointments_completed_today", this, CustomMetrics::getAppointmentsCompletedToday)
                .description("Número de citas completadas hoy")
                .register(meterRegistry);
        
        // Métrica: número total de citas completadas (para usar con increase())
        Gauge.builder("appointments_completed_total", this, CustomMetrics::getAppointmentsCompletedTotal)
                .description("Número total de citas completadas")
                .register(meterRegistry);
    }

    /**
     * Obtiene el número actual de citas en estado IN_PROGRESS.
     *
     * @return número de citas en estado IN_PROGRESS
     */
    public double getAppointmentsInProgress() {
        return appointmentRepository.countByStatus(AppointmentStatus.IN_PROGRESS);
    }

    /**
     * Obtiene el número de citas completadas hoy.
     *
     * @return número de citas completadas hoy
     */
    public double getAppointmentsCompletedToday() {
        return appointmentRepository.countByStatusAndDateToday(AppointmentStatus.COMPLETED);
    }
    
    /**
     * Obtiene el número total de citas completadas.
     *
     * @return número total de citas completadas
     */
    public double getAppointmentsCompletedTotal() {
        return appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
    }
}