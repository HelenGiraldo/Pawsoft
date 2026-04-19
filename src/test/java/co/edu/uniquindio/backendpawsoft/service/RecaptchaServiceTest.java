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

@ExtendWith(MockitoExtension.class)
@DisplayName("RecaptchaService Tests")
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
    @DisplayName("Should return true for valid token")
    void shouldReturnTrueForValidToken() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(Map.of("success", true));

        assertTrue(recaptchaService.isValid("valid-token"));
        verify(restTemplate).postForObject(anyString(), isNull(), eq(Map.class));
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void shouldReturnFalseForInvalidToken() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(Map.of("success", false));

        assertFalse(recaptchaService.isValid("invalid-token"));
    }

    @Test
    @DisplayName("Should return false for null token")
    void shouldReturnFalseForNullToken() {
        assertFalse(recaptchaService.isValid(null));
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        assertFalse(recaptchaService.isValid(""));
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should return false for blank token")
    void shouldReturnFalseForBlankToken() {
        assertFalse(recaptchaService.isValid("   "));
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should return false when Google response is null")
    void shouldReturnFalseWhenResponseIsNull() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenReturn(null);

        assertFalse(recaptchaService.isValid("valid-token"));
    }

    @Test
    @DisplayName("Should return false when exception occurs")
    void shouldReturnFalseWhenExceptionOccurs() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertFalse(recaptchaService.isValid("valid-token"));
    }
}
