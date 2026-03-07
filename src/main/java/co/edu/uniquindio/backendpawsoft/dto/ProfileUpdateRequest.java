package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO con los datos que el cliente puede actualizar en su perfil.
 * newPassword es opcional — si viene null o vacío, no se cambia.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Software III
 */
@Data
public class ProfileUpdateRequest {
    private String code;         // código 2FA de verificación
    private String email;
    private String phone;
    private String newPassword;
}