package co.edu.uniquindio.backendpawsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


/**
 * Entidad que representa un usuario dentro del sistema Pawsoft.
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
 *
 */

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User implements UserDetails {

    /**
     * Identificador único del usuario
     * se genera automáticamente con estrategia IDENTITY
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario
     */

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Correo electrónico del usuario
     * Debe ser único dentro del sistema
     */

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña del usuario
     */

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /**
     * Contador de intentos fallidos de inicio de sesión
     * Siempre tendrá un valor, nunca null
     */


    @Column(nullable = false)
    private int failedAttempts = 0;



    /**
     * Fecha y hora hasta la cual la cuenta está bloqueada
     * si es null o pasada, la cuenta está desbloqueada
     */
    private LocalDateTime lockTime;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // si no manejas roles aún
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }



}
