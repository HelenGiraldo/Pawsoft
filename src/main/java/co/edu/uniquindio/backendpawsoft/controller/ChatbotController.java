package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.ChatRequest;
import co.edu.uniquindio.backendpawsoft.dto.ChatResponse;
import co.edu.uniquindio.backendpawsoft.dto.MessageHistory;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.service.SystemPromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);
    private static final int MAX_HISTORY_MESSAGES = 10;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final SystemPromptGenerator systemPromptGenerator;

    public ChatbotController(RestTemplate restTemplate, SystemPromptGenerator systemPromptGenerator) {
        this.restTemplate = restTemplate;
        this.systemPromptGenerator = systemPromptGenerator;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest chatRequest,
            Authentication authentication) {
        try {
            // Validar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Intento de acceso al chatbot sin autenticación válida");
                throw new UnauthorizedException("Autenticación requerida");
            }

            // Extraer rol del usuario
            String role = extractUserRole(authentication);
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            log.info("[CHATBOT] userId={} role={} action=CHAT_REQUEST message_length={}", 
                    userId, role, chatRequest.getMessage().length());

            // Generar system prompt basado en rol
            String systemPrompt = systemPromptGenerator.generateSystemPrompt(role);

            List<Map<String, String>> messages = new ArrayList<>();

            // System prompt dinámico
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // Historial previo (limitado a últimos 10 mensajes)
            if (chatRequest.getHistory() != null) {
                List<MessageHistory> history = chatRequest.getHistory();
                int startIndex = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
                for (int i = startIndex; i < history.size(); i++) {
                    MessageHistory msg = history.get(i);
                    messages.add(Map.of(
                        "role", msg.getRole().equals("model") ? "assistant" : msg.getRole(),
                        "content", msg.getText()
                    ));
                }
            }

            // Mensaje actual
            messages.add(Map.of("role", "user", "content", chatRequest.getMessage()));

            // Body para Groq (formato OpenAI)
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("messages", messages);
            body.put("max_tokens", 512);
            body.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            String replyText = (String) message.get("content");

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setReply(replyText);
            chatResponse.setSuccess(true);

            log.info("[CHATBOT] userId={} role={} action=CHAT_RESPONSE success=true", userId, role);

            return ResponseEntity.ok(chatResponse);

        } catch (UnauthorizedException e) {
            log.error("[CHATBOT] action=CHAT_REQUEST error=UNAUTHORIZED message={}", e.getMessage());
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setReply("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.");
            errorResponse.setSuccess(false);
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("[CHATBOT] action=CHAT_REQUEST error=INTERNAL_ERROR message={}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setReply("Lo siento, ocurrió un error. Intenta de nuevo.");
            errorResponse.setSuccess(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Extrae el rol del usuario desde el objeto Authentication.
     * 
     * @param authentication Objeto de autenticación de Spring Security
     * @return Rol del usuario (ej: ROLE_ADMIN, ROLE_CLIENTE)
     * @throws UnauthorizedException si no se encuentra un rol válido
     */
    private String extractUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> {
                    log.error("[CHATBOT] Rol no encontrado en token para usuario: {}", 
                            authentication.getName());
                    return new UnauthorizedException("Rol no encontrado en el token");
                });
    }
}
