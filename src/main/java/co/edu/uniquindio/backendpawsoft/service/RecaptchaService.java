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
            System.out.println("[reCAPTCHA] Token vacío o nulo");
            return false;
        }

        String url = verifyUrl + "?secret=" + secretKey + "&response=" + token;
        System.out.println("[reCAPTCHA] Validando token con Google...");
        System.out.println("[reCAPTCHA] URL: " + verifyUrl);
        System.out.println("[reCAPTCHA] Secret Key (primeros 10 chars): " + secretKey.substring(0, Math.min(10, secretKey.length())) + "...");
        System.out.println("[reCAPTCHA] Token (primeros 50 chars): " + token.substring(0, Math.min(50, token.length())) + "...");

        try {
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            System.out.println("[reCAPTCHA] Respuesta completa de Google: " + response);
            
            if (response != null) {
                Boolean success = (Boolean) response.get("success");
                System.out.println("[reCAPTCHA] Success: " + success);
                
                if (Boolean.FALSE.equals(success)) {
                    Object errorCodes = response.get("error-codes");
                    System.out.println("[reCAPTCHA] ❌ VALIDACIÓN FALLIDA");
                    System.out.println("[reCAPTCHA] Error codes: " + errorCodes);
                    return false;
                }
                
                System.out.println("[reCAPTCHA] ✅ Token válido");
                return Boolean.TRUE.equals(success);
            }
            
            System.out.println("[reCAPTCHA] ❌ Respuesta nula de Google");
            return false;
        } catch (Exception e) {
            System.out.println("[reCAPTCHA] ❌ Excepción al validar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}