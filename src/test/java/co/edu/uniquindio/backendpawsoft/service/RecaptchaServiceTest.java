package co.edu.uniquindio.backendpawsoft.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RecaptchaService.
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
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de reCAPTCHA")
class RecaptchaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RecaptchaService recaptchaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recaptchaService, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(recaptchaService, "verifyUrl", "https://www.google.com/recaptcha/api/siteverify");
    }

    @Test
    @DisplayName("Debería validar token exitosamente")
    void deberiaValidarTokenExitosamente() {
        // Arrange
        String token = "valid-token";
        Map<String, Object> response = Map.of("success", true);
        
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(response);

        // Act
        boolean result = recaptchaService.isValid(token);

        // Assert
        assertTrue(result);
        verify(restTemplate).postForObject(anyString(), isNull(), eq(Map.class));
    }

    @Test
    @DisplayName("Debería retornar false si el token es inválido")
    void deberiaRetornarFalseSiTokenEsInvalido() {
        // Arrange
        String token = "invalid-token";
        Map<String, Object> response = Map.of("success", false);
        
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(response);

        // Act
        boolean result = recaptchaService.isValid(token);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Debería retornar false si el token es null")
    void deberiaRetornarFalseSiTokenEsNull() {
        // Act
        boolean result = recaptchaService.isValid(null);

        // Assert
        assertFalse(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("Debería retornar false si el token está vacío")
    void deberiaRetornarFalseSiTokenEstaVacio() {
        // Act
        boolean result = recaptchaService.isValid("");

        // Assert
        assertFalse(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("Debería retornar false si el token está en blanco")
    void deberiaRetornarFalseSiTokenEstaEnBlanco() {
        // Act
        boolean result = recaptchaService.isValid("   ");

        // Assert
        assertFalse(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("Debería retornar false si la respuesta de Google es null")
    void deberiaRetornarFalseSiRespuestaEsNull() {
        // Arrange
        String token = "valid-token";
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(null);

        // Act
        boolean result = recaptchaService.isValid(token);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Debería retornar false si ocurre una excepción")
    void deberiaRetornarFalseSiOcurreExcepcion() {
        // Arrange
        String token = "valid-token";
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenThrow(new RuntimeException("Error de red"));

        // Act
        boolean result = recaptchaService.isValid(token);

        // Assert
        assertFalse(result);
    }
}
