package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.ResultadoCodigo;
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
 * - el número de intentos fallidos de verificación (anti fuerza bruta)
 * - cuántas veces se ha reenviado el código en esta sesión
 * - cuántos bloqueos graduales acumula el usuario (para escalar el tiempo de espera)
 * - el resultado final del intento (auditoría)
 * - la fecha en que fue usado o invalidado (auditoría)
 * - la IP desde donde se realizó la verificación (auditoría)
 *
 * Reglas de negocio:
 * - Un código expira a los 3 minutos de ser generado.
 * - El usuario debe esperar al menos 60 segundos entre reenvíos.
 * - Se permiten máximo 5 reenvíos antes de aplicar un bloqueo gradual.
 * - Se permiten máximo 3 intentos fallidos de verificación por código.
 * - El bloqueo es gradual: el tiempo de espera se incrementa en 5 minutos
 *   por cada bloqueo acumulado (1er bloqueo = 5 min, 2do = 10 min, etc.).
 * - El contador de bloqueos acumulados persiste entre códigos para que
 *   la penalización no se reinicie al solicitar un nuevo código.
 * - Una vez marcado como usado, el código no puede reutilizarse.
 * - Los registros no usados se limpian tras 24 horas (tarea programada).
 * - Los registros usados/fallidos se conservan 30 días para auditoría.
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
     * Se usa para auditoría y para calcular el cooldown entre reenvíos.
     */
    @Column(nullable = false)
    private LocalDateTime creadoEn;

    /**
     * Fecha y hora en la que el código deja de ser válido.
     * Se establece como {@code creadoEn + 3 minutos}.
     */
    @Column(nullable = false)
    private LocalDateTime expiraEn;

    /**
     * Indica si el código ya fue procesado (usado, expirado o invalidado).
     *
     * Una vez marcado como {@code true}, el código no puede reutilizarse
     * aunque no haya expirado aún.
     */
    @Column(nullable = false)
    private boolean usado;

    /**
     * Contador de intentos fallidos de verificación asociados a este código.
     *
     * Si se alcanzan 3 intentos fallidos, se aplica un bloqueo gradual
     * para prevenir ataques de fuerza bruta sobre el segundo factor.
     */
    @Column(nullable = false)
    private int intentosFallidos;

    /**
     * Número de veces que el usuario ha solicitado reenviar el código
     * dentro de la misma sesión de login.
     *
     * Al alcanzar el máximo de reenvíos permitidos (5), se aplica un bloqueo
     * gradual que impide solicitar más códigos por un tiempo progresivo.
     * Este contador se transfiere al nuevo código para mantener la sesión continua.
     */
    @Column(nullable = false)
    private int cantidadReenvios;

    /**
     * Número de veces que el usuario ha sido bloqueado de forma acumulada,
     * ya sea por exceso de reenvíos o por exceso de intentos fallidos.
     *
     * Este valor determina la duración del próximo bloqueo según la fórmula:
     * {@code MINUTOS_BLOQUEO_BASE × bloqueosAcumulados}
     *
     * Se mantiene entre códigos (persiste en el nuevo código al generar uno)
     * para que la penalización escale progresivamente y no se reinicie.
     *
     * Ejemplos:
     * - 1er bloqueo: 5 minutos
     * - 2do bloqueo: 10 minutos
     * - 3er bloqueo: 15 minutos
     */
    @Column(nullable = false)
    private int bloqueosAcumulados;

    /**
     * Fecha y hora hasta la cual el usuario debe esperar antes de poder
     * volver a verificar o solicitar un nuevo código 2FA.
     *
     * Si es {@code null} o si la fecha actual es posterior a este valor,
     * no hay bloqueo activo y el usuario puede proceder normalmente.
     */
    @Column
    private LocalDateTime bloqueadoHasta;

    // ── Campos de auditoría ───────────────────────────────────────────────────

    /**
     * Resultado final de este código de verificación.
     *
     * Registra si el código fue usado exitosamente, si falló, si expiró,
     * o si fue invalidado porque el usuario solicitó uno nuevo.
     *
     * Almacenado como texto (STRING) para facilitar consultas directas en BD.
     * Puede ser {@code null} mientras el código todavía está activo y pendiente.
     *
     * @see ResultadoCodigo
     */
    @Column
    @Enumerated(EnumType.STRING)
    private ResultadoCodigo resultado;

    /**
     * Fecha y hora en que el código fue consumido, invalidado o marcado como expirado.
     *
     * Junto con {@code creadoEn}, permite calcular cuánto tiempo tardó el usuario
     * en verificar, y sirve como referencia temporal en reportes de auditoría.
     *
     * Es {@code null} mientras el código sigue activo.
     */
    @Column
    private LocalDateTime fechaUso;

    /**
     * Dirección IP desde la cual se realizó la solicitud de verificación o reenvío.
     *
     * Útil para detectar intentos desde ubicaciones inusuales o ataques remotos.
     * Soporta hasta 45 caracteres para cubrir direcciones IPv6.
     *
     * Puede ser {@code null} si la IP no fue capturada en el contexto de la solicitud.
     */
    @Column(length = 45)
    private String ipOrigen;
}