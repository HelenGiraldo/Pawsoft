package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario dentro del sistema Pawsoft.
 *
 * Además de ser una entidad JPA, implementa {@link UserDetails} para integrarse con
 * Spring Security y permitir la autenticación/autorización basada en roles.
 *
 * Incluye campos y reglas asociadas a seguridad, como:
 * - rol del usuario
 * - control de primer acceso (cambio de contraseña temporal)
 * - bloqueo temporal por seguridad
 * - soporte para datos de segundo factor (2FA)
 *
 * Relaciones:
 * - Un usuario puede tener múltiples códigos 2FA asociados a lo largo del tiempo.
 *   La relación está configurada con {@code CascadeType.ALL} y {@code orphanRemoval = true},
 *   lo que garantiza que al eliminar un usuario, todos sus códigos 2FA se eliminen
 *   automáticamente sin violar restricciones de clave foránea.
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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /**
     * Identificador único del usuario.
     * Se genera automáticamente con estrategia IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Correo electrónico del usuario.
     * Debe ser único dentro del sistema.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña del usuario.
     * No se expone en respuestas JSON.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /**
     * Indica si el usuario está en su primer acceso.
     *
     * Si es true, el usuario debe cambiar su contraseña temporal
     * antes de continuar usando el sistema.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean primerAcceso = true;

    /**
     * Rol asignado al usuario dentro del sistema.
     * Define permisos y accesos disponibles.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Contador de intentos fallidos de inicio de sesión.
     * Se utiliza como apoyo para políticas de seguridad (por ejemplo, bloqueo).
     */
    @Column(nullable = false)
    private int failedAttempts = 0;

    /**
     * Fecha y hora hasta la cual la cuenta está bloqueada.
     *
     * Si es null o si la fecha actual es posterior a este valor,
     * la cuenta se considera desbloqueada.
     */
    private LocalDateTime lockTime;

    /**
     * Código asociado a segundo factor (2FA), si aplica.
     *
     * Nota: si el sistema usa la entidad {@link Codigo2FA} como fuente principal,
     * este campo puede servir como soporte o para compatibilidad con el flujo actual.
     */
    private String twoFactorCode;

    /**
     * Fecha y hora de expiración del código 2FA asociado, si aplica.
     */
    private LocalDateTime twoFactorExpiration;

    /**
     * Indica si la cuenta del usuario está habilitada.
     *
     * Un usuario deshabilitado no puede iniciar sesión en el sistema.
     * Se inicializa en {@code false} y se activa tras completar el registro o
     * ser habilitado manualmente por un administrador.
     */
    @Column(nullable = false)
    private boolean enabled = false;

    /**
     * URL de la foto de perfil del usuario.
     *
     * Aplica especialmente para veterinarios, cuya foto se muestra al cliente
     * durante el proceso de agendamiento de citas.
     * Se almacena como URL pública (por ejemplo, desde Cloudinary).
     */
    @Column(length = 500)
    private String photoUrl;

    /**
     * Lista de códigos 2FA asociados a este usuario.
     *
     * La relación es bidireccional con {@link Codigo2FA}.
     * Se usa {@code CascadeType.ALL} para que cualquier operación sobre el usuario
     * (incluyendo eliminación) se propague automáticamente a sus códigos 2FA.
     * {@code orphanRemoval = true} garantiza que los códigos huérfanos
     * (sin usuario asociado) sean eliminados de la base de datos.
     *
     * Esto evita errores de clave foránea al eliminar un usuario que tenga
     * códigos 2FA registrados en la tabla {@code codigos_2fa}.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Codigo2FA> codigos2fa = new ArrayList<>();

    /**
     * Retorna las autoridades (roles) del usuario.
     *
     * @return colección con la autoridad correspondiente al rol del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role.name());
    }

    /**
     * Retorna el correo electrónico como nombre de usuario para autenticación.
     *
     * @return email del usuario
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta no ha expirado.
     *
     * En Pawsoft no se maneja expiración de cuentas actualmente.
     *
     * @return true siempre
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta no está bloqueada.
     *
     * La cuenta se considera bloqueada si {@code lockTime} no es null
     * y la fecha actual es anterior a {@code lockTime}.
     *
     * @return true si la cuenta no está bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        if (lockTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(lockTime);
    }

    /**
     * Indica si las credenciales no han expirado.
     *
     * En Pawsoft no se maneja expiración de credenciales actualmente.
     *
     * @return true siempre
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado.
     *
     * @return true si el usuario está activo en el sistema
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Column(length = 20)
    private String phone;
}