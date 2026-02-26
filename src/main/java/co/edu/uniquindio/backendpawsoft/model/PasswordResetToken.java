package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un token de restablecimiento de contraseña.
 *
 * Se utiliza en el flujo de “olvidé mi contraseña” para asociar un token único
 * a un usuario, definir una fecha de expiración y controlar si ya fue utilizado.
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
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    /**
     * Identificador único del token.
     * Se genera automáticamente con estrategia IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token único entregado al usuario (por ejemplo, vía correo).
     * Debe ser irrepetible dentro del sistema.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Usuario asociado al token.
     * La relación es muchos-a-uno: un usuario puede tener múltiples tokens.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Fecha y hora de expiración del token.
     * A partir de este momento el token se considera inválido.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    /**
     * Indica si el token ya fue usado para restablecer la contraseña.
     * Por defecto es false y debe marcarse true una vez consumido.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;
}