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
 * Relación con {@link User}:
 * - La relación es {@code @ManyToOne}, ya que un usuario puede tener múltiples
 *   códigos 2FA a lo largo del tiempo (uno por sesión de login).
 * - El campo {@code usuario} es el lado propietario de la relación bidireccional
 *   definida en {@link User#codigos2fa}.
 * - Gracias al {@code CascadeType.ALL} y {@code orphanRemoval = true} configurados
 *   en {@link User}, al eliminar un usuario todos sus códigos 2FA se eliminan
 *   automáticamente sin lanzar errores de clave foránea.
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
     * Usuario propietario de este código 2FA.
     *
     * Es el lado propietario de la relación bidireccional con {@link User}.
     * La columna {@code user_id} en la tabla {@code codigos_2fa} actúa como
     * clave foránea hacia la tabla {@code users}.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User usuario;

    /**
     * Valor del código de verificación generado y enviado al usuario.
     * Generalmente es un código numérico de 6 dígitos.
     */
    @Column(nullable = false)
    private String codigo;

    /**
     * Fecha y hora en la que se generó el código.
     * Se usa para auditoría y para calcular si el código sigue vigente.
     */
    @Column(nullable = false)
    private LocalDateTime creadoEn;

    /**
     * Fecha y hora en la que el código deja de ser válido.
     * Se establece como {@code creadoEn + 10 minutos}.
     */
    @Column(nullable = false)
    private LocalDateTime expiraEn;

    /**
     * Indica si el código ya fue utilizado exitosamente.
     *
     * Una vez marcado como {@code true}, el código no puede reutilizarse
     * aunque no haya expirado aún.
     */
    @Column(nullable = false)
    private boolean usado;

    /**
     * Contador de intentos fallidos de verificación asociados a este código.
     *
     * Si se alcanzan 3 intentos fallidos, el código se invalida para
     * prevenir ataques de fuerza bruta sobre el segundo factor.
     */
    @Column(nullable = false)
    private int intentosFallidos;

    /**
     * Fecha y hora hasta la cual el usuario debe esperar antes de volver a intentar
     * verificar o solicitar un nuevo código 2FA (cooldown anti fuerza bruta).
     *
     * Si es {@code null} o si la fecha actual es posterior a este valor,
     * no hay bloqueo activo y el usuario puede proceder normalmente.
     */
    @Column
    private LocalDateTime bloqueadoHasta;
}