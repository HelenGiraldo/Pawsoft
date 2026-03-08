package co.edu.uniquindio.backendpawsoft.audit;

import co.edu.uniquindio.backendpawsoft.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servicio para registro de auditoría de acciones en el sistema.
 *
 * Captura automáticamente información del usuario autenticado, la acción realizada,
 * la entidad afectada, valores antes/después del cambio, y datos de la sesión
 * (IP, User-Agent).
 *
 * Los logs de auditoría nunca deben interrumpir la operación principal - cualquier
 * error en el registro se captura y se registra en los logs de aplicación.
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
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Guarda un log de auditoría completo con valores antes/después.
     */
    public void log(String action,
                    String description,
                    String entity,
                    Integer entityId,
                    Object oldValue,
                    Object newValue) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("[AUDIT] No hay usuario autenticado para la acción: {}", action);
                return;
            }

            // Tu User implementa directamente UserDetails, el cast es directo
            User currentUser = (User) auth.getPrincipal();

            Integer userId   = currentUser.getId().intValue();
            String  userName = currentUser.getName();
            AuditLog.UserRole userRole = mapRole(currentUser.getRole().name());

            // IP y User-Agent del request actual
            String ipAddress = null;
            String userAgent = null;
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    ipAddress = getClientIp(request);
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception e) {
                log.debug("[AUDIT] No se pudo obtener IP/UserAgent: {}", e.getMessage());
            }

            // Convertir oldValue y newValue a String simple
            String oldJson = oldValue != null ? oldValue.toString() : null;
            String newJson = newValue != null ? newValue.toString() : null;

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userRole(userRole)
                    .userName(userName)
                    .action(action)
                    .description(description)
                    .entity(entity)
                    .entityId(entityId)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("[AUDIT] {} | usuario={} | entidad={}#{}", action, userName, entity, entityId);

        } catch (Exception e) {
            // Nunca dejes que un error de log rompa la operación principal
            log.error("[AUDIT] Error al guardar log: {}", e.getMessage(), e);
        }
    }

    /**
     * Versión simplificada sin valores antes/después (para LOGIN, DELETE, etc.)
     */
    public void log(String action, String description, String entity, Integer entityId) {
        log(action, description, entity, entityId, null, null);
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODOS AUXILIARES
    // ─────────────────────────────────────────────────────────────

    /**
     * Mapea tu enum Role de PawSoft al enum interno AuditLog.UserRole.
     */
    private AuditLog.UserRole mapRole(String roleName) {
        return switch (roleName) {
            case "ROLE_ADMIN"         -> AuditLog.UserRole.ADMIN;
            case "ROLE_VETERINARIO"   -> AuditLog.UserRole.VETERINARIAN;
            case "ROLE_RECEPCIONISTA" -> AuditLog.UserRole.RECEPTIONIST;
            case "ROLE_CLIENTE"       -> AuditLog.UserRole.CLIENT;
            default -> {
                log.warn("[AUDIT] Rol desconocido: {}", roleName);
                yield AuditLog.UserRole.CLIENT;
            }
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}