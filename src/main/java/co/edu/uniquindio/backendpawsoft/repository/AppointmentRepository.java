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
     *
     * @param clientId identificador del cliente
     * @return lista de citas ordenadas ascendentemente
     */
    List<Appointment> findByClientIdOrderByDateAscTimeAsc(Long clientId);

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
     *
     * @return lista de todas las citas
     */
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
     *
     * @param vetId id del veterinario
     * @return lista de citas del veterinario ordenadas por fecha y hora
     */
    List<Appointment> findByVetIdOrderByDateAscTimeAsc(Long vetId);

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
}