package co.edu.uniquindio.backendpawsoft.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio encargado de validar el token de reCAPTCHA v3 con la API de Google.
 *
 * Se utiliza para proteger los endpoints de registro y autenticación contra bots
 * y ataques automatizados. El token es generado por el widget de reCAPTCHA en el
 * frontend y validado aquí contra la API de verificación de Google.
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
@Service
@RequiredArgsConstructor
public class RecaptchaService {

    @Value("${recaptcha.secret}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate;

    /**
     * Valida el token de reCAPTCHA enviado desde el frontend.
     *
     * @param token token generado por el widget de reCAPTCHA v2
     * @return true si Google confirma que el token es válido
     */
    @SuppressWarnings("unchecked")
    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String url = verifyUrl + "?secret=" + secretKey + "&response=" + token;

        try {
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            return false;
        }
    }
}