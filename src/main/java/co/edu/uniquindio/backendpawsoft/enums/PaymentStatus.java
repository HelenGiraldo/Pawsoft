package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Enum que representa el estado de un pago en el sistema.
 *
 * Valores posibles:
 * - PENDING: Pago registrado pero aún no cobrado en efectivo
 * - PAID: Efectivo recibido y confirmado por la recepcionista
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
public enum PaymentStatus {
    /** Pago registrado pero aún no cobrado en efectivo */
    PENDING,
    /** Efectivo recibido y confirmado por la recepcionista */
    PAID
}