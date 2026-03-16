package co.edu.uniquindio.backendpawsoft.model;

/**
 * Entidad que representa una mascota registrada en el sistema.
 *
 * Cada mascota está vinculada a un propietario mediante su correo electrónico.
 * Al cambiar el correo del propietario, el campo {@code ownerEmail} se actualiza
 * en cascada desde {@link co.edu.uniquindio.backendpawsoft.service.ProfileService}.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private String breed;

    private String birthDate;

    @Column(nullable = false)
    private String sex;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(length = 500)
    private String photoUrl;
}