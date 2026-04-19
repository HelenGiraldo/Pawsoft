package co.edu.uniquindio.backendpawsoft.controller;

import co.edu.uniquindio.backendpawsoft.dto.ChatRequest;
import co.edu.uniquindio.backendpawsoft.dto.ChatResponse;
import co.edu.uniquindio.backendpawsoft.dto.MessageHistory;
import co.edu.uniquindio.backendpawsoft.dto.MedicalFormSuggestionRequest;
import co.edu.uniquindio.backendpawsoft.dto.MedicalFormSuggestionResponse;
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
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
            log.error("[CHATBOT] action=CHAT_REQUEST error=INTERNAL_ERROR message={} cause={}", 
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "unknown", e);
            ChatResponse errorResponse = new ChatResponse();
            
            // Mensajes de error más específicos para debugging
            String errorMsg = "Lo siento, ocurrió un error. Intenta de nuevo.";
            if (e.getMessage() != null && e.getMessage().contains("groq")) {
                errorMsg = "Error conectando con el servicio de IA. Verifica la configuración del servidor.";
            }
            
            errorResponse.setReply(errorMsg);
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

    @PostMapping("/medical-form-suggestions")
    public ResponseEntity<MedicalFormSuggestionResponse> getMedicalFormSuggestions(
            @RequestBody MedicalFormSuggestionRequest request,
            Authentication authentication) {
        try {
            // Validar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Intento de acceso a sugerencias médicas sin autenticación válida");
                throw new UnauthorizedException("Autenticación requerida");
            }

            // Verificar que sea veterinario
            String role = extractUserRole(authentication);
            if (!"ROLE_VETERINARIO".equals(role)) {
                log.warn("Intento de acceso a sugerencias médicas con rol no autorizado: {}", role);
                throw new UnauthorizedException("Solo veterinarios pueden acceder a esta funcionalidad");
            }

            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            log.info("[MEDICAL_SUGGESTIONS] userId={} role={} action=REQUEST animal={}", 
                    userId, role, request.getAnimalType());

            // Construir prompt especializado para autocompletado médico
            String medicalPrompt = buildMedicalFormPrompt(request);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPromptGenerator.generateSystemPrompt(role)));
            messages.add(Map.of("role", "user", "content", medicalPrompt));

            // Body para Groq
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("messages", messages);
            body.put("max_tokens", 1024);
            body.put("temperature", 0.3); // Menor temperatura para respuestas más consistentes

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            String aiResponse = (String) message.get("content");

            // Parsear la respuesta de IA y estructurarla
            MedicalFormSuggestionResponse suggestionResponse = parseAIResponse(aiResponse);
            suggestionResponse.setSuccess(true);

            log.info("[MEDICAL_SUGGESTIONS] userId={} role={} action=RESPONSE success=true", userId, role);

            return ResponseEntity.ok(suggestionResponse);

        } catch (UnauthorizedException e) {
            log.error("[MEDICAL_SUGGESTIONS] action=REQUEST error=UNAUTHORIZED message={}", e.getMessage());
            MedicalFormSuggestionResponse errorResponse = new MedicalFormSuggestionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("No autorizado para esta funcionalidad");
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("[MEDICAL_SUGGESTIONS] action=REQUEST error=INTERNAL_ERROR message={} cause={}", 
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "unknown", e);
            MedicalFormSuggestionResponse errorResponse = new MedicalFormSuggestionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error procesando sugerencias médicas. Intenta de nuevo.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private String buildMedicalFormPrompt(MedicalFormSuggestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("SOLICITUD DE AUTOCOMPLETADO DE FORMULARIO MÉDICO VETERINARIO:\n\n");
        
        prompt.append("INFORMACIÓN DEL PACIENTE:\n");
        if (request.getAnimalType() != null) {
            prompt.append("- Especie: ").append(request.getAnimalType()).append("\n");
        }
        if (request.getAge() != null) {
            prompt.append("- Edad: ").append(request.getAge()).append("\n");
        }
        if (request.getWeight() != null) {
            prompt.append("- Peso: ").append(request.getWeight()).append("\n");
        }
        if (request.getBreed() != null) {
            prompt.append("- Raza: ").append(request.getBreed()).append("\n");
        }
        
        prompt.append("\nSÍNTOMAS REPORTADOS:\n");
        prompt.append(request.getSymptoms()).append("\n");
        
        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().trim().isEmpty()) {
            prompt.append("\nINFORMACIÓN ADICIONAL:\n");
            prompt.append(request.getAdditionalInfo()).append("\n");
        }
        
        prompt.append("\nPor favor, proporciona sugerencias médicas estructuradas usando EXACTAMENTE este formato:\n\n");
        prompt.append("🔍 DIAGNÓSTICO SUGERIDO:\n[Diagnóstico más probable]\n\n");
        prompt.append("🔍 DIAGNÓSTICOS DIFERENCIALES:\n- [Diagnóstico alternativo 1]\n- [Diagnóstico alternativo 2]\n- [Diagnóstico alternativo 3]\n\n");
        prompt.append("💊 TRATAMIENTO RECOMENDADO:\n[Descripción del tratamiento]\n\n");
        prompt.append("💊 MEDICAMENTOS:\n- [Medicamento 1 con dosis]\n- [Medicamento 2 con dosis]\n- [Medicamento 3 con dosis]\n\n");
        prompt.append("🧪 EXÁMENES COMPLEMENTARIOS:\n- [Examen 1]\n- [Examen 2]\n- [Examen 3]\n\n");
        prompt.append("📋 PRONÓSTICO:\n[Expectativa de recuperación]\n\n");
        prompt.append("🏠 RECOMENDACIONES AL PROPIETARIO:\n- [Recomendación 1]\n- [Recomendación 2]\n- [Recomendación 3]\n");
        
        return prompt.toString();
    }

    private MedicalFormSuggestionResponse parseAIResponse(String aiResponse) {
        MedicalFormSuggestionResponse response = new MedicalFormSuggestionResponse();
        
        try {
            // Patrones para extraer información estructurada
            response.setSuggestedDiagnosis(extractSection(aiResponse, "🔍 DIAGNÓSTICO SUGERIDO:", "🔍 DIAGNÓSTICOS DIFERENCIALES:"));
            response.setDifferentialDiagnoses(extractListItems(aiResponse, "🔍 DIAGNÓSTICOS DIFERENCIALES:", "💊 TRATAMIENTO RECOMENDADO:"));
            response.setRecommendedTreatment(extractSection(aiResponse, "💊 TRATAMIENTO RECOMENDADO:", "💊 MEDICAMENTOS:"));
            response.setMedications(extractListItems(aiResponse, "💊 MEDICAMENTOS:", "🧪 EXÁMENES COMPLEMENTARIOS:"));
            response.setComplementaryExams(extractListItems(aiResponse, "🧪 EXÁMENES COMPLEMENTARIOS:", "📋 PRONÓSTICO:"));
            response.setPrognosis(extractSection(aiResponse, "📋 PRONÓSTICO:", "🏠 RECOMENDACIONES AL PROPIETARIO:"));
            response.setOwnerRecommendations(extractListItems(aiResponse, "🏠 RECOMENDACIONES AL PROPIETARIO:", null));
            
        } catch (Exception e) {
            log.warn("[MEDICAL_SUGGESTIONS] Error parsing AI response, using fallback: {}", e.getMessage());
            // Fallback: devolver respuesta completa como diagnóstico sugerido
            response.setSuggestedDiagnosis(aiResponse);
            response.setDifferentialDiagnoses(Arrays.asList("Consultar respuesta completa"));
            response.setRecommendedTreatment("Ver respuesta completa del chatbot");
            response.setMedications(Arrays.asList("Consultar respuesta completa"));
            response.setComplementaryExams(Arrays.asList("Consultar respuesta completa"));
            response.setPrognosis("Ver respuesta completa");
            response.setOwnerRecommendations(Arrays.asList("Consultar respuesta completa"));
        }
        
        return response;
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int startIndex = text.indexOf(startMarker);
        if (startIndex == -1) return "";
        
        startIndex += startMarker.length();
        int endIndex = endMarker != null ? text.indexOf(endMarker, startIndex) : text.length();
        if (endIndex == -1) endIndex = text.length();
        
        return text.substring(startIndex, endIndex).trim();
    }

    private List<String> extractListItems(String text, String startMarker, String endMarker) {
        String section = extractSection(text, startMarker, endMarker);
        List<String> items = new ArrayList<>();
        
        String[] lines = section.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("- ")) {
                items.add(line.substring(2).trim());
            } else if (!line.isEmpty() && !line.contains(":")) {
                items.add(line);
            }
        }
        
        return items;
    }

    /**
     * Endpoint público para el chatbot de autenticación.
     * No requiere JWT. Recibe el body ya formateado para Groq (con system prompt incluido)
     * y lo reenvía directamente. El system prompt restrictivo viene del frontend.
     */
    @PostMapping("/public-chat")
    public ResponseEntity<ChatResponse> publicChat(@RequestBody Map<String, Object> groqBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(groqBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            String replyText = (String) message.get("content");

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setReply(replyText);
            chatResponse.setSuccess(true);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("[PUBLIC_CHATBOT] error={}", e.getMessage());
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setReply("Lo siento, no pude procesar tu consulta. Intenta más tarde.");
            errorResponse.setSuccess(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
