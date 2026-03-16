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
     * Verifica si ya existe el usuario administrador por correo:
     * - Si NO existe: Lo crea con enabled = true
     * - Si existe pero está deshabilitado: Lo habilita automáticamente
     * - Si existe y está habilitado: No hace nada
     *
     * Esto garantiza que siempre haya un admin funcional al arrancar el servidor.
     *
     * @param args argumentos de línea de comandos (no se usan en esta implementación)
     */
    @Override
    public void run(String... args) {

        String adminEmail = "***REDACTED***";
        
        var existingAdmin = userRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isEmpty()) {
            // El admin no existe, crearlo
            User admin = User.builder()
                    .name("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("***REDACTED***"))
                    .role(Role.ROLE_ADMIN)
                    .primerAcceso(false)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado: " + adminEmail);
        } else {
            // El admin existe, verificar que esté habilitado
            User admin = existingAdmin.get();
            
            if (!admin.isEnabled()) {
                // El admin está deshabilitado, habilitarlo
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("⚠️ Usuario ADMIN estaba deshabilitado - HABILITADO automáticamente: " + adminEmail);
            } else {
                System.out.println("ℹ️ Usuario ADMIN ya existe y está habilitado: " + adminEmail);
            }
        }
    }
}