package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositorio de persistencia para la entidad {@link Appointment}.
 *
 * Proporciona operaciones CRUD básicas mediante {@link JpaRepository}
 * y consultas derivadas necesarias para la gestión de citas.
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
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    /**
     * Obtiene todas las citas de un cliente ordenadas por fecha y hora.
     * Usa JOIN FETCH para evitar N+1 queries.
     *
     * @param clientId identificador del cliente
     * @return lista de citas ordenadas ascendentemente
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.client LEFT JOIN FETCH a.vet LEFT JOIN FETCH a.pet WHERE a.client.id = :clientId ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByClientIdOrderByDateAscTimeAsc(@Param("clientId") Long clientId);

    /**
     * Obtiene las citas de un cliente filtradas por estado
     * y ordenadas por fecha y hora.
     *
     * @param clientId identificador del cliente
     * @param status estado de la cita
     * @return lista de citas que cumplen los criterios
     */
    List<Appointment> findByClientIdAndStatusOrderByDateAscTimeAsc(
            Long clientId,
            AppointmentStatus status
    );


    @Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId AND a.date = :date AND a.status != :status")
    List<Appointment> findByVetAndDateAndStatusNot(
            @Param("vetId") Long vetId,
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.vet.id = :vetId AND a.date = :date AND a.time = :time AND a.status != 'CANCELLED'")
    boolean existsByVetIdAndDateAndTime(@Param("vetId") Long vetId, @Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * Lista todas las citas ordenadas por fecha y hora descendente.
     * Usado por el recepcionista para ver el historial completo.
     * Usa JOIN FETCH para evitar N+1 queries.
     *
     * @return lista de todas las citas
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.client LEFT JOIN FETCH a.vet LEFT JOIN FETCH a.pet ORDER BY a.date DESC, a.time DESC")
    List<Appointment> findAllByOrderByDateDescTimeDesc();

    /**
     * Verifica si existe una cita con el veterinario, fecha y hora indicados,
     * excluyendo una cita específica por ID (usado al editar para no bloquearse a sí misma).
     *
     * @param vetId id del veterinario
     * @param date  fecha
     * @param time  hora
     * @param id    id de la cita a excluir
     * @return true si el slot está ocupado por otra cita
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.vet.id = :vetId AND a.date = :date AND a.time = :time AND a.status != 'CANCELLED' AND a.id != :id")
    boolean existsByVetIdAndDateAndTimeAndIdNot(@Param("vetId") Long vetId, @Param("date") LocalDate date, @Param("time") LocalTime time, @Param("id") Long id);

    /**
     * Obtiene la lista de citas asociadas a un veterinario específico,
     * ordenadas por fecha ascendente y, en caso de coincidencia,
     * por hora ascendente.
     * Usa JOIN FETCH para evitar N+1 queries.
     *
     * @param vetId id del veterinario
     * @return lista de citas del veterinario ordenadas por fecha y hora
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.client LEFT JOIN FETCH a.vet LEFT JOIN FETCH a.pet WHERE a.vet.id = :vetId ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByVetIdOrderByDateAscTimeAsc(@Param("vetId") Long vetId);

    void deleteByClientId(Long clientId);

    void deleteByPetId(Long petId);

    List<Appointment> findByPetId(Long petId);

    /**
     * Busca citas en estado UPCOMING o CONFIRMED cuya fecha y hora ya pasaron.
     * Usado por el scheduler para marcarlas automáticamente como NO_SHOW.
     */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.status IN (:statuses)
          AND (a.date < :today OR (a.date = :today AND a.time < :now))
        """)
    List<Appointment> findPastPendingAppointments(
            @Param("statuses") List<AppointmentStatus> statuses,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now
    );

    /**
     * Cuenta el número de citas con un estado específico.
     * Usado para métricas de Prometheus/Grafana.
     *
     * @param status estado de la cita
     * @return número de citas con ese estado
     */
    long countByStatus(AppointmentStatus status);

    /**
     * Cuenta el número de citas completadas en la fecha actual.
     * Usado para métricas de Prometheus/Grafana.
     *
     * @param status estado COMPLETED
     * @return número de citas completadas hoy
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.date = CURRENT_DATE")
    long countByStatusAndDateToday(@Param("status") AppointmentStatus status);

    /**
     * Cuenta el número de citas de un cliente específico.
     * Optimización para evitar findAll().
     *
     * @param clientId ID del cliente
     * @return número de citas del cliente
     */
    long countByClientId(Long clientId);

    /**
     * Cuenta el número de citas en una fecha específica.
     * Optimización para métricas.
     *
     * @param date fecha a consultar
     * @return número de citas en esa fecha
     */
    long countByDate(LocalDate date);
}