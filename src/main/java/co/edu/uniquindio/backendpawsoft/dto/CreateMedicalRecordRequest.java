package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la creación de registros médicos.
 * 
 * Contiene toda la información necesaria para crear un nuevo registro médico
 * incluyendo diagnóstico, tratamiento, medicamentos y observaciones.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordRequest {

    @NotNull(message = "El ID de la mascota es obligatorio")
    private Long petId;

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long appointmentId;

    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnosis;

    @NotBlank(message = "El tratamiento es obligatorio")
    private String treatment;

    private String observations;

    private String symptoms;

    private String physicalExam;

    private String recommendations;

    private Double weight;

    private Double temperature;

    private Integer heartRate;

    private Integer respiratoryRate;

    private List<String> medications;

    private List<String> vaccines;

    private String nextAppointmentRecommendation;

    private Boolean requiresFollowUp;

    private String followUpInstructions;
}