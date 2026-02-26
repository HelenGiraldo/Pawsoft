package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un código de verificación de segundo factor (2FA).
 *
 * Cada código está asociado a un usuario y registra:
 * - el valor del código generado
 * - la fecha de creación y la fecha de expiración
 * - si ya fue utilizado
 * - el número de intentos fallidos, para reducir riesgo de fuerza bruta
 *
 * Reglas de negocio:
 * - Un código expira a los 10 minutos de ser generado.
 * - Se permiten máximo 3 intentos fallidos antes de invalidarse.
 * - Una vez marcado como usado, el código no puede reutilizarse.
 * - Los registros se limpian de la base de datos después de 24 horas (mediante tarea programada).
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
@Entity
@Table(name = "codigos_2fa")
@Getter
@Setter
@NoArgsConstructor
public class Codigo2FA {

    /**
     * Identificador único del código 2FA.
     * Se genera automáticamente con estrategia IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario asociado al código 2FA.
     * Un usuario puede tener múltiples códigos a lo largo del tiempo.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User usuario;

    /**
     * Código de verificación generado para el usuario.
     */
    @Column(nullable = false)
    private String codigo;

    /**
     * Fecha y hora en la que se generó el código.
     */
    @Column(nullable = false)
    private LocalDateTime creadoEn;

    /**
     * Fecha y hora en la que el código deja de ser válido.
     */
    @Column(nullable = false)
    private LocalDateTime expiraEn;

    /**
     * Indica si el código ya fue utilizado exitosamente.
     */
    @Column(nullable = false)
    private boolean usado;

    /**
     * Contador de intentos fallidos asociados a este código.
     */
    @Column(nullable = false)
    private int intentosFallidos;


    /**
     * Fecha y hora hasta la cual el usuario debe esperar antes de volver a intentar
     * verificar/solicitar el código 2FA (cooldown anti fuerza bruta).
     *
     * Si es null o si la fecha actual es posterior, no hay bloqueo activo.
     */
    @Column
    private LocalDateTime bloqueadoHasta;

}