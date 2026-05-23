package co.edu.uniquindio.backendpawsoft.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Controlador proxy para métricas de Prometheus.
 * Evita problemas de CORS con NGINX al exponer las métricas directamente desde la API.
 */
@RestController
@RequestMapping("/api/prometheus")
@CrossOrigin(origins = "*")
public class PrometheusProxyController {

    /**
     * Endpoint proxy para métricas de Prometheus.
     * Permite acceso directo sin problemas de CORS.
     */
    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPrometheusMetrics() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String metrics = restTemplate.getForObject("http://localhost:8080/actuator/prometheus", String.class);
            
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Allow-Headers", "*")
                .body(metrics);
        } catch (Exception e) {
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body("# Error retrieving metrics: " + e.getMessage());
        }
    }

    /**
     * Maneja peticiones OPTIONS para CORS preflight
     */
    @RequestMapping(value = "/metrics", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, OPTIONS")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Max-Age", "3600")
            .build();
    }
}