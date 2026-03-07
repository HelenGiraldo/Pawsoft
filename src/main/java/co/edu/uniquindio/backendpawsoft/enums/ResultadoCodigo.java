package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Enum que representa el resultado final de un código de verificación 2FA.
 *
 * Se almacena en la base de datos como texto (STRING) para facilitar
 * la lectura directa en consultas de auditoría.
 *
 * Valores posibles:
 * - EXITOSO:    el usuario ingresó el código correcto y se autenticó.
 * - FALLIDO:    el usuario ingresó un código incorrecto (puede haber varios intentos).
 * - EXPIRADO:   el código superó el tiempo de validez (10 minutos) antes de ser usado.
 * - INVALIDADO: el código fue descartado porque el usuario solicitó uno nuevo
 *               antes de que el anterior expirara o fuera usado.
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
public enum ResultadoCodigo {

    /** El código fue verificado exitosamente. */
    EXITOSO,

    /** El código ingresado no coincidió con el almacenado. */
    FALLIDO,

    /** El código venció antes de ser utilizado. */
    EXPIRADO,

    /** El código fue reemplazado por uno nuevo antes de ser usado. */
    INVALIDADO
}