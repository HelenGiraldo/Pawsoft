package co.edu.uniquindio.backendpawsoft.config;

import co.edu.uniquindio.backendpawsoft.security.JwtAuthenticationFilter;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de seguridad de la aplicación.
 *
 * Define la configuración principal de Spring Security para Pawsoft, incluyendo:
 * - El uso de autenticación basada en JWT (stateless).
 * - La autorización por rutas según el rol del usuario.
 * - La desactivación de CSRF (común en APIs REST sin sesión).
 * - El bean para el cifrado de contraseñas con BCrypt.
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
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Crea el filtro de autenticación JWT que intercepta las peticiones
     * y valida el token enviado en el header Authorization.
     *
     * @param jwtService servicio encargado de operaciones sobre el JWT (extraer claims y validar token)
     * @param userDetailsService servicio para cargar el usuario a partir del username (email)
     * @return instancia de {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    /**
     * Configura la cadena de filtros de Spring Security y las reglas de autorización.
     *
     * Reglas principales:
     * - Rutas bajo /auth/** son públicas.
     * - Rutas bajo /api/admin/** requieren rol ADMIN.
     * - Rutas bajo /api/veterinario/** requieren rol VETERINARIO.
     * - Rutas bajo /api/recepcionista/** requieren rol RECEPCIONISTA.
     * - Rutas bajo /api/cliente/** requieren rol CLIENTE.
     * - Cualquier otra ruta requiere autenticación.
     *
     * Además, define que la aplicación no manejará sesión (STATELESS),
     * ya que el control de autenticación se realiza mediante tokens JWT.
     *
     * @param http objeto de configuración de seguridad HTTP
     * @param jwtAuthenticationFilter filtro que valida JWT antes del filtro de usuario/contraseña
     * @return {@link SecurityFilterChain} configurado
     * @throws Exception si ocurre un error durante la construcción de la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // VETERINARIO
                        .requestMatchers("/api/veterinario/**").hasRole("VETERINARIO")

                        // RECEPCIONISTA
                        .requestMatchers("/api/recepcionista/**").hasRole("RECEPCIONISTA")

                        // CLIENTE
                        .requestMatchers("/api/cliente/**").hasRole("CLIENTE")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Bean encargado de cifrar contraseñas utilizando el algoritmo BCrypt.
     *
     * Se usa para:
     * - almacenar contraseñas de forma segura (hash + salt)
     * - comparar contraseñas ingresadas con las almacenadas (match)
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEnconder() {
        return new BCryptPasswordEncoder();
    }
}