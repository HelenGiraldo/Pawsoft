package co.edu.uniquindio.backendpawsoft.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración de seguridad.
 * Define el bean encargado de encripar contraseñas
 * utilizando el algoritmo BCrypt
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


@Configuration
public class SecurityConfig {

    /**
     * Configura la seguridad HTTP permitiendo acceso libre
     * a todos los endpoints mientras se implementa la autenticación.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }


    /**
     * Bean que permite encriptar contraseñas
     * antes de almacenarlas en la base de datos
     *
     * @return instancia de BCryptPasswordEnconder
     */

    @Bean
    public BCryptPasswordEncoder passwordEnconder(){
        return new BCryptPasswordEncoder();
    }
}
