package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO con los datos que el usuario puede actualizar en su perfil.
 *
 * Todos los campos son opcionales excepto el código de verificación 2FA.
 * Solo se actualizan los campos que vengan con valor.
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
public class ProfileUpdateRequest {
    private String code;         // código 2FA de verificación
    private String email;
    private String phone;
    private String newPassword;
}