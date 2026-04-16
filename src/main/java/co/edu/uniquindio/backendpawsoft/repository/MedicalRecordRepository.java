package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link MedicalRecord}.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /** Registros médicos de una mascota, ordenados por fecha descendente. */
    List<MedicalRecord> findByPetIdOrderByCreadoEnDesc(Long petId);

    /** Registros médicos creados por un veterinario, ordenados por fecha descendente. */
    List<MedicalRecord> findByVetIdOrderByCreadoEnDesc(Long vetId);

    /** Registro médico asociado a una cita específica. */
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    void deleteByVetId(Long vetId);
}