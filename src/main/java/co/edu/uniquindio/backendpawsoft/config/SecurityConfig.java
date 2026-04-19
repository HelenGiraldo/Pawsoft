package co.edu.uniquindio.backendpawsoft.config;

import co.edu.uniquindio.backendpawsoft.security.JwtAuthenticationFilter;
import co.edu.uniquindio.backendpawsoft.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Clase de configuración de seguridad de la aplicación.
 *
 * Define la configuración principal de Spring Security para Pawsoft, incluyendo:
 * - El uso de autenticación basada en JWT (stateless).
 * - La autorización por rutas según el rol del usuario.
 * - La desactivación de CSRF (común en APIs REST sin sesión).
 * - El bean para el cifrado de contraseñas con BCrypt.
 *
 * ── Tabla resumen de rutas y acceso ──────────────────────────────────────────
 *
 *  Ruta                                   Roles permitidos
 *  ─────────────────────────────────────────────────────────────────────────
 *  /auth/**                               Público (sin autenticación)
 *  /api/admin/public/**                   ADMIN, CLIENTE, RECEPCIONISTA
 *  /api/admin/users                       ADMIN, RECEPCIONISTA
 *  /api/admin/clients                     ADMIN, RECEPCIONISTA
 *  /api/admin/pets/**                     ADMIN, RECEPCIONISTA
 *  /api/admin/payments/stats              ADMIN
 *  /api/admin/payments/**                 ADMIN
 *  /api/admin/**                          ADMIN (catch-all admin)
 *  /api/recepcionista/appointments/**     RECEPCIONISTA, ADMIN
 *  /api/recepcionista/payments/**         RECEPCIONISTA, ADMIN
 *  /api/recepcionista/**                  RECEPCIONISTA (catch-all recep)
 *  /api/veterinario/**                    VETERINARIO
 *  /api/vet/**                            VETERINARIO
 *  /api/cliente/**                        CLIENTE
 *  Cualquier otra                         Autenticado (cualquier rol)
 *  ─────────────────────────────────────────────────────────────────────────
 *
 * IMPORTANTE — Orden de las reglas:
 * Spring Security evalúa las reglas en el orden en que están declaradas.
 * Las reglas más específicas deben ir ANTES que las más generales.
 * Por eso los permisos compartidos (ADMIN + RECEPCIONISTA) se declaran
 * antes del catch-all de /api/admin/** y /api/recepcionista/**.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
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
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    /**
     * Crea el filtro de autenticación JWT que intercepta las peticiones
     * y valida el token enviado en el header Authorization.
     *
     * @param jwtService         servicio encargado de operaciones sobre el JWT
     *                           (extraer claims y validar token)
     * @param userDetailsService servicio para cargar el usuario a partir del
     *                           username (email)
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
     * La aplicación es completamente STATELESS: no hay sesión HTTP.
     * Cada petición debe incluir el token JWT en el header:
     *   Authorization: Bearer <token>
     *
     * @param http                      objeto de configuración de seguridad HTTP
     * @param jwtAuthenticationFilter   filtro que valida JWT antes del filtro
     *                                  estándar de usuario/contraseña
     * @param corsConfigurationSource   fuente de configuración CORS compartida
     *                                  con el CorsFilter
     * @return {@link SecurityFilterChain} configurado
     * @throws Exception si ocurre un error durante la construcción
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // ── Preflight CORS ───────────────────────────────────────────
                        // OPTIONS siempre permitido para que el navegador pueda
                        // hacer el handshake antes de cada petición con credenciales.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Rutas públicas (sin autenticación) ───────────────────────
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/chatbot/public-chat").permitAll()

                        // ── Chatbot (requiere autenticación para RBAC) ──────────────
                        .requestMatchers("/api/chatbot/**").authenticated()

                        // ── Actuator para Prometheus (sin autenticación) ─────────────
                        // Solo expone /actuator/prometheus y /actuator/health.
                        // No expone información sensible de la app.
                        .requestMatchers("/actuator/**").permitAll()

                        // ── Rutas admin compartidas con recepcionista y/o cliente ────
                        // Se declaran ANTES del catch-all de /api/admin/**
                        // para que la regla más específica tenga precedencia.
                        .requestMatchers("/api/admin/public/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_RECEPCIONISTA")
                        .requestMatchers("/api/admin/users")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                        .requestMatchers("/api/admin/clients")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                        .requestMatchers("/api/admin/pets")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                        .requestMatchers("/api/admin/pets/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                        // ── Módulo de pagos: admin ───────────────────────────────────
                        // Estadísticas financieras, historial completo, reversión de
                        // pagos y gestión de la tabla de precios de servicios.
                        // Solo ADMIN — la recepcionista tiene su propio prefijo.
                        .requestMatchers("/api/admin/payments/**")
                        .hasAuthority("ROLE_ADMIN")

                        // ── Catch-all admin ──────────────────────────────────────────
                        // Cualquier ruta /api/admin/** no cubierta arriba → solo ADMIN.
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                        // ── Módulo de citas: recepcionista ───────────────────────────
                        // Listar, crear, editar, confirmar, marcar inasistencia y
                        // cancelar citas. El ADMIN también tiene acceso para soporte.
                        .requestMatchers("/api/recepcionista/appointments/**")
                        .hasAnyAuthority("ROLE_RECEPCIONISTA", "ROLE_ADMIN")

                        // ── Módulo de pagos: recepcionista ───────────────────────────
                        // Registrar cobros, confirmar pagos en efectivo y consultar
                        // el estado de pago de una cita.
                        // El ADMIN también tiene acceso para soporte y auditoría.
                        .requestMatchers("/api/recepcionista/payments/**")
                        .hasAnyAuthority("ROLE_RECEPCIONISTA", "ROLE_ADMIN")

                        // ── Catch-all recepcionista ──────────────────────────────────
                        // Rutas /api/recepcionista/** no cubiertas arriba.
                        .requestMatchers("/api/recepcionista/**")
                        .hasAuthority("ROLE_RECEPCIONISTA")

                        // ── Veterinario ──────────────────────────────────────────────
                        .requestMatchers("/api/veterinario/**")
                        .hasAuthority("ROLE_VETERINARIO")
                        .requestMatchers("/api/vet/medical-records/appointment/**")
                        .hasAnyAuthority("ROLE_VETERINARIO", "ROLE_RECEPCIONISTA", "ROLE_ADMIN")
                        .requestMatchers("/api/vet/**")
                        .hasAuthority("ROLE_VETERINARIO")

                        // ── Cliente ──────────────────────────────────────────────────
                        .requestMatchers("/api/cliente/medical-records/**")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_RECEPCIONISTA", "ROLE_ADMIN")
                        .requestMatchers("/api/cliente/**")
                        .hasAuthority("ROLE_CLIENTE")

                        // ── Cualquier otra ruta autenticada ──────────────────────────
                        .anyRequest().authenticated()
                )

                // Sin sesión HTTP — autenticación exclusivamente por JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // El filtro JWT actúa antes del filtro estándar de credenciales
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
     * - Almacenar contraseñas de forma segura (hash + salt automático).
     * - Comparar la contraseña ingresada con el hash almacenado (matches).
     *
     * BCrypt es deliberadamente lento para dificultar ataques de fuerza bruta.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEnconder() {
        return new BCryptPasswordEncoder();
    }
}