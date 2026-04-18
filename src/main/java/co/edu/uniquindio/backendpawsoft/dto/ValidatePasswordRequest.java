package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO para la validación de contraseña actual del usuario.
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
public class ValidatePasswordRequest {
    private String currentPassword;
}