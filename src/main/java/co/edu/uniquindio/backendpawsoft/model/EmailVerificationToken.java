package co.edu.uniquindio.backendpawsoft.model;

/**
 * Entidad que representa un token de verificación de correo electrónico.
 *
 * Se genera al registrar un nuevo usuario y se invalida una vez que
 * el usuario confirma su cuenta haciendo clic en el enlace enviado por email.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne
    @JoinColumn(nullable = false)
    private User user;

    private LocalDateTime expirationDate;
}
