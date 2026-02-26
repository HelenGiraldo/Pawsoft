package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Enumeración que define los roles de acceso disponibles dentro del sistema.
 *
 * Los roles se utilizan principalmente para controlar permisos y restringir
 * el acceso a funcionalidades según el tipo de usuario autenticado, integrándose
 * con los mecanismos de seguridad  de Spring Security mediante el
 * prefijo estándar {@code ROLE_}
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 *
 * Valentina Porras Salazar
 * >Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */

public enum Role {

    ROLE_ADMIN,
    ROLE_VETERINARIO,
    ROLE_RECEPCIONISTA,
    ROLE_CLIENTE
}