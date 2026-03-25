package co.edu.uniquindio.backendpawsoft.scheduler;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Scheduler que marca automáticamente como NO_SHOW las citas que ya pasaron
 * sin haber sido completadas ni canceladas.
 *
 * Se ejecuta cada 30 minutos para mantener los estados actualizados.
 *
 * Proyecto: Pawsoft — Universidad del Quindío — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentNoShowJob {

    private final AppointmentRepository appointmentRepository;

    @Scheduled(fixedDelay = 1800000) // cada 30 minutos
    @Transactional
    public void marcarCitasPasadasComoNoShow() {
        ZonedDateTime nowColombia = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        LocalDate today = nowColombia.toLocalDate();
        LocalTime now   = nowColombia.toLocalTime();

        List<Appointment> pasadas = appointmentRepository.findPastPendingAppointments(
                List.of(AppointmentStatus.UPCOMING, AppointmentStatus.CONFIRMED),
                today,
                now
        );

        if (pasadas.isEmpty()) return;

        pasadas.forEach(a -> a.setStatus(AppointmentStatus.NO_SHOW));
        appointmentRepository.saveAll(pasadas);

        log.info("[NoShow Job] {} cita(s) marcadas como NO_SHOW", pasadas.size());
    }
}
