package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.dto.ChatRequest;
import co.edu.uniquindio.backendpawsoft.dto.ChatResponse;
import co.edu.uniquindio.backendpawsoft.dto.MessageHistory;
import co.edu.uniquindio.backendpawsoft.dto.MedicalFormSuggestionRequest;
import co.edu.uniquindio.backendpawsoft.dto.MedicalFormSuggestionResponse;
import co.edu.uniquindio.backendpawsoft.controller.ChatbotController;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.service.SystemPromptGenerator;
import co.edu.uniquindio.backendpawsoft.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatbotController Tests")
class ChatbotServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private SystemPromptGenerator systemPromptGenerator;

    @InjectMocks
    private ChatbotController chatbotController;

    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.buildTestUser();

        ReflectionTestUtils.setField(chatbotController, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(chatbotController, "apiUrl", "https://api.groq.com/v1/chat");

        authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of((org.springframework.security.core.GrantedAuthority) () -> "ROLE_CLIENTE"));
    }

    // ── chat ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return successful chat response")
    void shouldReturnSuccessfulChatResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("¿Cómo cuidar a un perro?");
        request.setHistory(List.of());

        when(systemPromptGenerator.generateSystemPrompt("ROLE_CLIENTE")).thenReturn("System prompt");

        Map<String, Object> groqResponse = buildGroqResponse("Los perros necesitan cuidados básicos.");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(groqResponse));

        ResponseEntity<ChatResponse> result = chatbotController.chat(request, authentication);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Los perros necesitan cuidados básicos.", result.getBody().getReply());
    }

    @Test
    @DisplayName("Should return 401 when authentication is null")
    void shouldReturn401WhenAuthenticationIsNull() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hola");

        ResponseEntity<ChatResponse> result = chatbotController.chat(request, null);

        assertEquals(401, result.getStatusCode().value());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);

        ChatRequest request = new ChatRequest();
        request.setMessage("Hola");

        ResponseEntity<ChatResponse> result = chatbotController.chat(request, unauthenticated);

        assertEquals(401, result.getStatusCode().value());
    }

    @Test
    @DisplayName("Should return 500 when Groq API fails")
    void shouldReturn500WhenGroqApiFails() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hola");
        request.setHistory(List.of());

        when(systemPromptGenerator.generateSystemPrompt(anyString())).thenReturn("System prompt");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ResponseEntity<ChatResponse> result = chatbotController.chat(request, authentication);

        assertEquals(500, result.getStatusCode().value());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should include history in request to Groq")
    void shouldIncludeHistoryInRequestToGroq() {
        MessageHistory historyMsg = new MessageHistory();
        historyMsg.setRole("user");
        historyMsg.setText("Mensaje anterior");

        ChatRequest request = new ChatRequest();
        request.setMessage("Nuevo mensaje");
        request.setHistory(List.of(historyMsg));

        when(systemPromptGenerator.generateSystemPrompt(anyString())).thenReturn("System prompt");

        Map<String, Object> groqResponse = buildGroqResponse("Respuesta");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(groqResponse));

        ResponseEntity<ChatResponse> result = chatbotController.chat(request, authentication);

        assertEquals(200, result.getStatusCode().value());
        verify(restTemplate).postForEntity(anyString(), any(), eq(Map.class));
    }

    // ── getMedicalFormSuggestions ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return medical suggestions for veterinarian")
    void shouldReturnMedicalSuggestionsForVeterinarian() {
        User vetUser = TestDataBuilder.buildTestVet();
        Authentication vetAuth = mock(Authentication.class);
        when(vetAuth.isAuthenticated()).thenReturn(true);
        when(vetAuth.getPrincipal()).thenReturn(vetUser);
        when(vetAuth.getAuthorities()).thenAnswer(inv ->
                List.of((org.springframework.security.core.GrantedAuthority) () -> "ROLE_VETERINARIO"));

        MedicalFormSuggestionRequest request = new MedicalFormSuggestionRequest();
        request.setSymptoms("fiebre, tos");
        request.setAnimalType("Perro");

        when(systemPromptGenerator.generateSystemPrompt("ROLE_VETERINARIO")).thenReturn("Vet prompt");

        Map<String, Object> groqResponse = buildGroqResponse(
                "🔍 DIAGNÓSTICO SUGERIDO:\nGripe canina\n\n" +
                "🔍 DIAGNÓSTICOS DIFERENCIALES:\n- Moquillo\n\n" +
                "💊 TRATAMIENTO RECOMENDADO:\nReposo\n\n" +
                "💊 MEDICAMENTOS:\n- Ibuprofeno\n\n" +
                "🧪 EXÁMENES COMPLEMENTARIOS:\n- Hemograma\n\n" +
                "📋 PRONÓSTICO:\nFavorable\n\n" +
                "🏠 RECOMENDACIONES AL PROPIETARIO:\n- Mantener hidratado"
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(groqResponse));

        ResponseEntity<MedicalFormSuggestionResponse> result =
                chatbotController.getMedicalFormSuggestions(request, vetAuth);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return 401 when non-vet tries to access medical suggestions")
    void shouldReturn401WhenNonVetAccessesMedicalSuggestions() {
        MedicalFormSuggestionRequest request = new MedicalFormSuggestionRequest();
        request.setSymptoms("fiebre");

        // testUser is ROLE_CLIENTE
        ResponseEntity<MedicalFormSuggestionResponse> result =
                chatbotController.getMedicalFormSuggestions(request, authentication);

        assertEquals(401, result.getStatusCode().value());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("Should return 401 when authentication is null for medical suggestions")
    void shouldReturn401WhenAuthNullForMedicalSuggestions() {
        MedicalFormSuggestionRequest request = new MedicalFormSuggestionRequest();
        request.setSymptoms("fiebre");

        ResponseEntity<MedicalFormSuggestionResponse> result =
                chatbotController.getMedicalFormSuggestions(request, null);

        assertEquals(401, result.getStatusCode().value());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildGroqResponse(String content) {
        Map<String, String> message = Map.of("content", content);
        Map<String, Object> choice = Map.of("message", message);
        return Map.of("choices", List.of(choice));
    }
}
