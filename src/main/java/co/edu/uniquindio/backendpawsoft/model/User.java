package co.edu.uniquindio.backendpawsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;


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

public class User {

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



}
