package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.audit.AuditLogService;
import co.edu.uniquindio.backendpawsoft.dto.*;
import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import co.edu.uniquindio.backendpawsoft.exception.NotFoundException;
import co.edu.uniquindio.backendpawsoft.model.Appointment;
import co.edu.uniquindio.backendpawsoft.model.Pet;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.AppointmentRepository;
import co.edu.uniquindio.backendpawsoft.repository.PetRepository;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio encargado de gestionar la lógica de negocio
 * relacionada con las citas médicas del sistema Pawsoft.
 *
 * Responsabilidades:
 * - Crear citas validando disponibilidad y reglas del negocio
 * - Consultar citas del cliente autenticado
 * - Cancelar citas existentes con validaciones de seguridad
 *
 * Este servicio encapsula la lógica de negocio,
 * manteniendo el controlador desacoplado del acceso a datos.
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
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final AuditLogService auditLogService;

    /**
     * Crea una nueva cita para el cliente autenticado.
     * <p>
     * Validaciones:
     * - El usuario debe existir
     * - No se permiten fechas pasadas
     * - La hora debe estar dentro del horario de atención (07:00 - 21:00)
     * - El horario no debe estar previamente reservado
     *
     * @param request datos de la cita
     * @param email   correo del usuario autenticado
     * @return AppointmentResponse con la información creada
     */
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, String email) {

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        User vet = userRepository.findById(request.vetId())
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));

        if (request.date().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden agendar citas en fechas pasadas");
        }

        if (request.date().isEqual(LocalDate.now()) && request.time().isBefore(LocalTime.now())) {
            throw new RuntimeException("No se pueden agendar citas en horas que ya pasaron");
        }

        LocalTime openingTime = LocalTime.of(7, 0);
        LocalTime closingTime = LocalTime.of(21, 0);

        if (request.time().isBefore(openingTime) || request.time().isAfter(closingTime)) {
            throw new RuntimeException("La hora seleccionada está fuera del horario de atención");
        }

        if (appointmentRepository.existsByVetIdAndDateAndTime(
                request.vetId(), request.date(), request.time())) {
            throw new RuntimeException("El horario seleccionado ya está reservado");
        }

        Appointment appointment = Appointment.builder()
                .date(request.date())
                .time(request.time())
                .reason(request.reason())
                .status(AppointmentStatus.UPCOMING)
                .client(client)
                .vet(vet)
                .pet(pet)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log("APPOINTMENT_CREATE", "Cita creada por cliente", "APPOINTMENT", saved.getId().intValue());

        return mapToResponse(saved);
    }

    /**
     * Obtiene todas las citas asociadas al cliente autenticado.
     *
     * @param email correo del usuario autenticado
     * @return lista de citas ordenadas por fecha y hora
     */
    public List<AppointmentResponse> getAppointmentsByClient(String email) {

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return appointmentRepository
                .findByClientIdOrderByDateAscTimeAsc(client.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Cancela una cita existente del cliente autenticado.
     *
     * Validaciones:
     * - El usuario debe existir
     * - La cita debe existir
     * - La cita debe pertenecer al cliente autenticado
     * - No se puede cancelar una cita ya cancelada
     * - No se puede cancelar una cita completada
     * - No se puede cancelar una cita en el pasado
     *
     * @param appointmentId identificador de la cita
     * @param email correo del usuario autenticado
     */
    public void cancelAppointment(Long appointmentId, String email) {

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("No tiene permisos para cancelar esta cita");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("La cita ya se encuentra cancelada");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("No se puede cancelar una cita completada");
        }

        if (appointment.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede cancelar una cita que ya ocurrió");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        auditLogService.log("APPOINTMENT_CANCEL", "Cita cancelada por cliente", "APPOINTMENT", appointmentId.intValue());
    }

    /**
     * Convierte una entidad Appointment en su DTO de respuesta.
     *
     * @param appointment entidad persistida
     * @return AppointmentResponse
     */
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getPet()  != null ? appointment.getPet().getName()     : "Mascota",
                appointment.getVet()  != null ? appointment.getVet().getName()     : "Veterinario",
                appointment.getPet()  != null ? appointment.getPet().getPhotoUrl() : null
        );
    }


    public List<String> getOccupiedSlots(Long vetId, String date) {
        return appointmentRepository
                .findByVetAndDateAndStatusNot(vetId, LocalDate.parse(date), AppointmentStatus.CANCELLED)
                .stream()
                .map(a -> a.getTime().toString())
                .toList();
    }

    /**
     * Lista TODAS las citas del sistema, enriquecidas con datos
     * de cliente, mascota y veterinario para el panel del recepcionista.
     *
     * @return lista completa de citas ordenadas por fecha y hora descendente
     */
    public List<RecepAppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByDateDescTimeDesc()
                .stream()
                .map(this::mapToRecepResponse)
                .toList();
    }

    /**
     * Crea una cita desde el panel del recepcionista.
     * El recepcionista especifica el email del cliente existente.
     *
     * @param request datos de la cita
     * @return respuesta enriquecida con los datos creados
     */
    public RecepAppointmentResponse recepCreateAppointment(RecepCreateAppointmentRequest request) {

        User client = userRepository.findByEmail(request.clientEmail())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + request.clientEmail()));

        User vet = userRepository.findById(request.vetId())
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));

        if (appointmentRepository.existsByVetIdAndDateAndTime(
                request.vetId(), request.date(), request.time())) {
            throw new RuntimeException("El horario ya está reservado para ese veterinario");
        }

        Appointment appointment = Appointment.builder()
                .date(request.date())
                .time(request.time())
                .reason(request.reason())
                .notes(request.notes())
                .status(AppointmentStatus.UPCOMING)
                .client(client)
                .vet(vet)
                .pet(pet)
                .build();

        RecepAppointmentResponse response = mapToRecepResponse(appointmentRepository.save(appointment));

        auditLogService.log("APPOINTMENT_CREATE_RECEP", "Cita creada por recepcionista", "APPOINTMENT", response.id().intValue());

        return response;
    }

    /**
     * Edita los datos de una cita existente.
     *
     * @param id      identificador de la cita
     * @param request nuevos datos
     * @return cita actualizada
     */
    public RecepAppointmentResponse recepUpdateAppointment(Long id, RecepUpdateAppointmentRequest request) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        User vet = userRepository.findById(request.vetId())
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        // Validar conflicto de horario solo si cambió fecha/hora/vet
        boolean slotChanged = !appointment.getDate().equals(request.date())
                || !appointment.getTime().equals(request.time())
                || !appointment.getVet().getId().equals(request.vetId());

        if (slotChanged && appointmentRepository.existsByVetIdAndDateAndTimeAndIdNot(
                request.vetId(), request.date(), request.time(), id)) {
            throw new RuntimeException("El nuevo horario ya está reservado para ese veterinario");
        }

        appointment.setDate(request.date());
        appointment.setTime(request.time());
        appointment.setVet(vet);
        if (request.reason() != null) appointment.setReason(request.reason());
        if (request.notes()  != null) appointment.setNotes(request.notes());

        RecepAppointmentResponse response = mapToRecepResponse(appointmentRepository.save(appointment));

        auditLogService.log("APPOINTMENT_UPDATE", "Cita actualizada por recepcionista", "APPOINTMENT", id.intValue());

        return response;
    }

    /**
     * Confirma una cita (PENDIENTE → CONFIRMADA).
     *
     * @param id identificador de la cita
     */
    public void confirmAppointment(Long id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(apt);

        auditLogService.log("APPOINTMENT_CONFIRM", "Cita confirmada", "APPOINTMENT", id.intValue());
    }

    /**
     * Marca al cliente como no asistido (CONFIRMADA → NO_ASISTIO).
     *
     * @param id identificador de la cita
     */
    public void markNoShow(Long id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));
        apt.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(apt);

        auditLogService.log("APPOINTMENT_NO_SHOW", "Cita marcada como no asistida", "APPOINTMENT", id.intValue());
    }

    /**
     * Cancela una cita desde el panel del recepcionista,
     * registrando opcionalmente el motivo.
     *
     * @param id     identificador de la cita
     * @param request motivo de cancelación
     */
    public void recepCancelAppointment(Long id, RecepCancelRequest request) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));

        if (apt.getStatus() == AppointmentStatus.CANCELLED ||
                apt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("La cita ya está cancelada");
        }

        apt.setStatus(AppointmentStatus.CANCELLED);
        apt.setCancelReason(request != null ? request.cancelReason() : null);
        appointmentRepository.save(apt);

        auditLogService.log("APPOINTMENT_CANCEL_RECEP", "Cita cancelada por recepcionista", "APPOINTMENT", id.intValue());
    }

    /**
     * Mapea una entidad Appointment al DTO enriquecido del recepcionista.
     *
     * @param a entidad persistida
     * @return RecepAppointmentResponse con todos los datos necesarios para el frontend
     */
    private RecepAppointmentResponse mapToRecepResponse(Appointment a) {
        return new RecepAppointmentResponse(
                a.getId(),
                a.getDate(),
                a.getTime(),
                a.getReason(),
                a.getStatus(),
                a.getCancelReason(),
                a.getClient() != null ? a.getClient().getId()    : null,
                a.getClient() != null ? a.getClient().getName()  : "—",
                a.getClient() != null ? a.getClient().getEmail() : "—",
                a.getPet()    != null ? a.getPet().getId()       : null,
                a.getPet()    != null ? a.getPet().getName()     : "—",
                a.getPet()    != null ? a.getPet().getSpecies()  : "—",
                a.getPet()    != null ? a.getPet().getPhotoUrl() : null,
                a.getPet()    != null ? a.getPet().getBreed()    : "—",
                a.getPet()    != null ? a.getPet().getBirthDate(): null,
                a.getVet()    != null ? a.getVet().getId()       : null,
                a.getVet()    != null ? a.getVet().getName()     : "—",
                a.getVet()    != null ? a.getVet().getPhotoUrl() : null,
                a.getNotes()
        );
    }

    /**
     * Obtiene todas las citas asociadas a un veterinario específico,
     * identificado por su correo electrónico.
     *
     * Busca el veterinario en el sistema y, si existe, retorna la lista
     * de sus citas ordenadas por fecha y hora ascendente. Cada cita es
     * transformada a un DTO enriquecido para el recepcionista.
     *
     * @param email correo electrónico del veterinario
     * @return lista de citas del veterinario en formato RecepAppointmentResponse
     * @throws NotFoundException si el veterinario no existe
     */
    public List<RecepAppointmentResponse> getAppointmentsByVet(String email) {
        User vet = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Veterinario no encontrado"));

        return appointmentRepository
                .findByVetIdOrderByDateAscTimeAsc(vet.getId())
                .stream()
                .map(this::mapToRecepResponse)
                .toList();
    }
}