package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de inicializar datos básicos del sistema al arrancar la aplicación.
 *
 * Su función principal es garantizar la existencia de un usuario administrador por defecto
 * en la base de datos, evitando su duplicación si ya existe un registro con el correo definido.
 *
 * Este componente se ejecuta automáticamente al iniciar el proyecto gracias a la interfaz
 * {@link CommandLineRunner}.
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
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /**
     * Repositorio de usuarios utilizado para consultar y crear el usuario administrador.
     */
    private final UserRepository userRepository;

    /**
     * Codificador de contraseñas para almacenar el hash de la contraseña del administrador.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Método ejecutado al iniciar la aplicación.
     *
     * Verifica si ya existe el usuario administrador por correo. Si no existe, lo crea con:
     * - rol ADMIN
     * - contraseña inicial codificada con BCrypt
     * - primerAcceso = false (para evitar obligar cambio inmediato de contraseña)
     *
     * @param args argumentos de línea de comandos (no se usan en esta implementación)
     */
    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("helenx.giraldol@uqvirtual.edu.co").isEmpty()) {
            User admin = User.builder()
                    .name("Administrador")
                    .email("helenx.giraldol@uqvirtual.edu.co")
                    .password(passwordEncoder.encode("Admin123!"))
                    .role(Role.ROLE_ADMIN)
                    .primerAcceso(false)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println("Usuario ADMIN creado: admin@pawsoft.com / Admin123!");
        } else {
            System.out.println("Usuario ADMIN ya existe");
        }
    }
}