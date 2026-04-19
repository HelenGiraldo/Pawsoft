package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la actualización de registros médicos.
 * 
 * Contiene la información que puede ser modificada en un registro médico existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
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
public class UpdateMedicalRecordRequest {

    private String diagnosis;

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