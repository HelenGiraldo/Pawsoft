package co.edu.uniquindio.backendpawsoft.config;

/**
 * Configuración de CORS para la aplicación.
 *
 * Permite solicitudes desde los orígenes autorizados (producción y desarrollo local),
 * habilitando credenciales y los métodos HTTP necesarios para la API REST.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Especificar orígenes exactos en lugar de usar *
        // Esto es necesario cuando setAllowCredentials(true)
        config.setAllowedOrigins(Arrays.asList(
            "https://pawsoft.online",
            "https://www.pawsoft.online",
            "https://icy-dune-0d82b770f.2.azurestaticapps.net",
            "http://localhost:4200",
            "http://localhost:8100"
        ));
        
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}