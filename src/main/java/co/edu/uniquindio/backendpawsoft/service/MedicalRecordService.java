package co.edu.uniquindio.backendpawsoft.service;

/**
 * Servicio para la gestión de historiales médicos de mascotas.
 * Maneja la creación, consulta y actualización de registros médicos veterinarios.
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
 * - Raúl Yulbraynner Rivera Gálvez
 */
import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordRequest;
import co.edu.uniquindio.backendpawsoft.dto.MedicalRecordResponse;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.MedicalRecord;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.HospitalizationRepository;
import co.edu.uniquindio.backendpawsoft.repository.MedicalRecordRepository;
import co.edu.uniquindio.backendpawsoft.repository.ServicePriceRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para gestionar los registros médicos de las consultas veterinarias.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ServicePriceRepository servicePriceRepository;
    private final HospitalizationRepository hospitalizationRepository;

    /**
     * Crea o actualiza el registro médico de una cita.
     * Si ya existe un registro para esa cita, lo actualiza.
     * Al guardar con cerrar=true, cambia el estado de la cita a COMPLETED.
     *
     * @param request  datos del registro médico
     * @param vetEmail email del veterinario autenticado
     * @param cerrar   si true, marca la cita como COMPLETED
     * @return registro médico guardado
     */
    public MedicalRecordResponse guardar(MedicalRecordRequest request, String vetEmail, boolean cerrar) {

        User vet = userRepository.findByEmail(vetEmail)
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        // Validar que la cita esté CONFIRMED o IN_PROGRESS si se intenta cerrar
        if (cerrar && appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Solo se pueden cerrar citas que estén confirmadas o en progreso");
        }

        // Crear o actualizar
        MedicalRecord record = medicalRecordRepository
                .findByAppointmentId(request.appointmentId())
                .orElse(MedicalRecord.builder()
                        .appointment(appointment)
                        .pet(appointment.getPet())
                        .vet(vet)
                        .build());

        record.setPeso(request.peso());
        record.setTemperatura(request.temperatura());
        record.setFrecuenciaCardiaca(request.frecuenciaCardiaca());
        record.setFrecuenciaRespiratoria(request.frecuenciaRespiratoria());
        record.setObservacionesGenerales(request.observacionesGenerales());
        record.setDiagnosticoPrincipal(request.diagnosticoPrincipal());
        record.setDiagnosticoSecundario(request.diagnosticoSecundario());
        record.setNotasClinicas(request.notasClinicas());
        record.setMedicamentos(request.medicamentos());
        record.setIndicaciones(request.indicaciones());
        record.setDiagnosticoCliente(request.diagnosticoCliente());
        record.setMedicamentosRecetados(request.medicamentosRecetados());
        record.setIndicacionesCliente(request.indicacionesCliente());
        record.setVacunasAplicadas(request.vacunasAplicadas());
        record.setProximoControlFecha(request.proximoControlFecha());
        record.setProximoControlMotivo(request.proximoControlMotivo());
        record.setFotosAdjuntas(request.fotosAdjuntas());

        // Persistir costos: el costo de medicamentos viene del frontend,
        // el precio base del servicio se consulta directamente desde la BD
        BigDecimal costoMeds = request.costoMedicamentos() != null ? request.costoMedicamentos() : BigDecimal.ZERO;
        BigDecimal precioServicio = servicePriceRepository
                .findByServiceType(appointment.getReason())
                .map(sp -> sp.getPrice())
                .orElse(BigDecimal.ZERO);
        record.setCostoMedicamentos(costoMeds);
        record.setCostoTotal(precioServicio.add(costoMeds));

        if (cerrar) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
            auditLogService.log("APPOINTMENT_COMPLETE", "Cita completada por veterinario",
                    "APPOINTMENT", appointment.getId().intValue());
        }

        MedicalRecord saved = medicalRecordRepository.save(record);
        auditLogService.log("MEDICAL_RECORD_SAVE", "Registro médico guardado",
                "MEDICAL_RECORD", saved.getId().intValue());

        return mapToResponse(saved);
    }

    /**
     * Obtiene todos los registros médicos del veterinario autenticado.
     * Solo retorna registros de citas COMPLETED.
     */
    public List<MedicalRecordResponse> getByVet(String vetEmail) {
        User vet = userRepository.findByEmail(vetEmail)
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        return medicalRecordRepository.findByVetIdOrderByCreadoEnDesc(vet.getId())
                .stream()
                .filter(r -> r.getAppointment().getStatus() == AppointmentStatus.COMPLETED)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Historial clínico completo de una mascota — accesible para todos los veterinarios.
     * Solo retorna registros de citas COMPLETED.
     */
    public List<MedicalRecordResponse> getByPet(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByCreadoEnDesc(petId)
                .stream()
                .filter(r -> r.getAppointment().getStatus() == AppointmentStatus.COMPLETED)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Historial clínico completo de todos los veterinarios — para el módulo de historial compartido.
     * Solo retorna registros de citas COMPLETED.
     */
    public List<MedicalRecordResponse> getAll() {
        return medicalRecordRepository.findAll()
                .stream()
                .filter(r -> r.getAppointment().getStatus() == AppointmentStatus.COMPLETED)
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene el registro médico de una cita específica.
     */
    public MedicalRecordResponse getByAppointment(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new NotFoundException("Registro médico no encontrado"));
        return mapToResponse(record);
    }

    /**
     * Obtiene todos los registros médicos del cliente autenticado.
     * Solo retorna registros de citas COMPLETED.
     */
    public List<MedicalRecordResponse> getByCliente(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        return medicalRecordRepository.findByAppointmentClientIdOrderByCreadoEnDesc(client.getId())
                .stream()
                .filter(r -> r.getAppointment().getStatus() == AppointmentStatus.COMPLETED)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene el resumen médico de una cita para el cliente autenticado.
     * Valida que la cita pertenezca al cliente antes de retornar el registro.
     */
    public MedicalRecordResponse getResumenParaCliente(Long appointmentId, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("No tiene permisos para ver este registro");
        }

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new NotFoundException("Registro médico no encontrado"));

        return mapToResponse(record);
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord r) {
        Appointment a = r.getAppointment();
        
        // Verificar si la mascota está fallecida
        Boolean petIsDeceased = false;
        if (r.getPet() != null) {
            petIsDeceased = hospitalizationRepository
                    .findByPetIdAndStatus(r.getPet().getId(), HospitalizationStatus.DECEASED)
                    .stream()
                    .findAny()
                    .isPresent();
        }
        
        return new MedicalRecordResponse(
                r.getId(),
                a.getId(),
                a.getDate(),
                a.getTime(),
                a.getReason(),
                a.getStatus(),
                r.getPet() != null ? r.getPet().getId()        : null,
                r.getPet() != null ? r.getPet().getName()      : "—",
                r.getPet() != null ? r.getPet().getSpecies()   : "—",
                r.getPet() != null ? r.getPet().getBreed()     : "—",
                r.getPet() != null ? r.getPet().getBirthDate() : null,
                r.getPet() != null ? r.getPet().getSex()       : "—",
                r.getPet() != null ? r.getPet().getPhotoUrl()  : null,
                petIsDeceased,
                a.getClient() != null ? a.getClient().getName()  : "—",
                a.getClient() != null ? a.getClient().getEmail() : "—",
                r.getVet()  != null ? r.getVet().getName()     : "—",
                r.getPeso(),
                r.getTemperatura(),
                r.getFrecuenciaCardiaca(),
                r.getFrecuenciaRespiratoria(),
                r.getObservacionesGenerales(),
                r.getDiagnosticoPrincipal(),
                r.getDiagnosticoSecundario(),
                r.getNotasClinicas(),
                r.getMedicamentos(),
                r.getIndicaciones(),
                r.getDiagnosticoCliente(),
                r.getMedicamentosRecetados(),
                r.getIndicacionesCliente(),
                r.getVacunasAplicadas(),
                r.getProximoControlFecha(),
                r.getProximoControlMotivo(),
                r.getFotosAdjuntas(),
                r.getCostoMedicamentos(),
                r.getCostoTotal(),
                servicePriceRepository.findByServiceType(a.getReason())
                        .map(sp -> sp.getPrice()).orElse(BigDecimal.ZERO),
                r.getCreadoEn(),
                r.getActualizadoEn()
        );
    }
}