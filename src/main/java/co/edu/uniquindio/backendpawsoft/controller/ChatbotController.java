package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.ChatRequest;
import co.edu.uniquindio.backendpawsoft.dto.ChatResponse;
import co.edu.uniquindio.backendpawsoft.dto.MessageHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ChatbotController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            // System prompt
            messages.add(Map.of(
                "role", "system",
                "content", "Eres PawBot, el asistente virtual de PawSoft, una aplicación de gestión veterinaria. " +
                           "FUNCIONES DISPONIBLES en la app actualmente:\n" +
                           "- Registro e inicio de sesión de clientes\n" +
                           "- Recuperación de contraseña\n" +
                           "- Agendar, ver y cancelar citas veterinarias\n" +
                           "- Gestión de mascotas (agregar, ver, editar)\n" +
                           "- Ver y realizar pagos de citas\n" +
                           "- Editar perfil de usuario\n" +
                           "IMPORTANTE: Si te preguntan por funciones que NO están en esa lista (historial médico, recordatorios, mensajería, etc.), " +
                           "responde honestamente que esa función no está disponible aún. " +
                           "Responde siempre en español, de forma concisa y amigable."
            ));

            // Historial previo
            if (chatRequest.getHistory() != null) {
                for (MessageHistory msg : chatRequest.getHistory()) {
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

            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Error llamando a Groq: {}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setReply("Lo siento, ocurrió un error. Intenta de nuevo.");
            errorResponse.setSuccess(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
