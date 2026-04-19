package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.ChatbotRequest;
import co.edu.uniquindio.backendpawsoft.dto.ChatbotResponse;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatbotService Tests")
class ChatbotServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ChatbotService chatbotService;

    private User testUser;
    private ChatbotRequest chatbotRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();
        chatbotRequest = ChatbotRequest.builder()
                .message("¿Cuáles son los síntomas de la gripe en perros?")
                .userId(testUser.getId())
                .build();

        // Set chatbot API properties using reflection
        ReflectionTestUtils.setField(chatbotService, "chatbotApiUrl", "https://api.chatbot.com/v1/chat");
        ReflectionTestUtils.setField(chatbotService, "chatbotApiKey", "test-api-key");
    }

    @Test
    @DisplayName("Should successfully process chatbot request")
    void shouldProcessChatbotRequest() {
        // Given
        String mockApiResponse = "Los síntomas de la gripe en perros incluyen fiebre, tos, letargo y pérdida de apetito. Es importante consultar con un veterinario para un diagnóstico adecuado.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("success", response.getStatus());
        assertNotNull(response.getTimestamp());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
        verify(auditLogService).logChatbotInteraction(testUser.getId(), chatbotRequest.getMessage(), mockApiResponse);
    }

    @Test
    @DisplayName("Should handle API error gracefully")
    void shouldHandleApiErrorGracefully() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Lo siento"));
        assertEquals("error", response.getStatus());
        assertNotNull(response.getTimestamp());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
        verify(auditLogService).logChatbotError(testUser.getId(), chatbotRequest.getMessage(), "API connection failed");
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // Given
        chatbotRequest.setMessage("");

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Por favor"));
        assertEquals("error", response.getStatus());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Given
        chatbotRequest.setMessage(null);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Por favor"));
        assertEquals("error", response.getStatus());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should handle very long message")
    void shouldHandleVeryLongMessage() {
        // Given
        String longMessage = "a".repeat(2000); // Very long message
        chatbotRequest.setMessage(longMessage);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("demasiado largo"));
        assertEquals("error", response.getStatus());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should process veterinary question correctly")
    void shouldProcessVeterinaryQuestionCorrectly() {
        // Given
        chatbotRequest.setMessage("¿Qué vacunas necesita un cachorro?");
        String mockApiResponse = "Los cachorros necesitan vacunas contra el moquillo, hepatitis, parvovirus, parainfluenza y rabia. El calendario de vacunación debe ser establecido por un veterinario.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("success", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
        verify(auditLogService).logChatbotInteraction(testUser.getId(), chatbotRequest.getMessage(), mockApiResponse);
    }

    @Test
    @DisplayName("Should process appointment question correctly")
    void shouldProcessAppointmentQuestionCorrectly() {
        // Given
        chatbotRequest.setMessage("¿Cómo puedo agendar una cita?");
        String mockApiResponse = "Para agendar una cita, puedes usar nuestro sistema en línea o llamar directamente a la clínica. Necesitarás proporcionar información sobre tu mascota y el motivo de la consulta.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("success", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should handle inappropriate content")
    void shouldHandleInappropriateContent() {
        // Given
        chatbotRequest.setMessage("contenido inapropiado");

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("veterinaria"));
        assertEquals("filtered", response.getStatus());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should provide emergency guidance")
    void shouldProvideEmergencyGuidance() {
        // Given
        chatbotRequest.setMessage("Mi perro está vomitando sangre, ¿qué hago?");
        String mockApiResponse = "Esta es una emergencia veterinaria. Debes llevar a tu perro inmediatamente al veterinario más cercano o a una clínica de emergencias. No esperes.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("emergency", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
        verify(auditLogService).logChatbotEmergency(testUser.getId(), chatbotRequest.getMessage());
    }

    @Test
    @DisplayName("Should handle API timeout")
    void shouldHandleApiTimeout() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Read timed out"));

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("tiempo de espera"));
        assertEquals("timeout", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should validate user exists")
    void shouldValidateUserExists() {
        // Given
        chatbotRequest.setUserId(null);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("usuario"));
        assertEquals("error", response.getStatus());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should provide general pet care information")
    void shouldProvideGeneralPetCareInformation() {
        // Given
        chatbotRequest.setMessage("¿Cómo cuidar a un gato?");
        String mockApiResponse = "El cuidado de un gato incluye alimentación balanceada, agua fresca, caja de arena limpia, ejercicio, cepillado regular y visitas veterinarias periódicas.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("success", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should handle rate limiting")
    void shouldHandleRateLimiting() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Rate limit exceeded"));

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("muchas consultas"));
        assertEquals("rate_limited", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should provide context-aware responses")
    void shouldProvideContextAwareResponses() {
        // Given
        chatbotRequest.setMessage("¿Cuánto cuesta una consulta?");
        String mockApiResponse = "Los precios de las consultas varían según el tipo de servicio. Te recomendamos contactar directamente con la clínica para obtener información actualizada sobre tarifas.";
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(mockApiResponse);

        // When
        ChatbotResponse response = chatbotService.processMessage(chatbotRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getMessage());
        assertEquals("success", response.getStatus());

        verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
    }
}