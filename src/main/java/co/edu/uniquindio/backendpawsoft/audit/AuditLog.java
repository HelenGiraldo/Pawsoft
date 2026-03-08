package co.edu.uniquindio.backendpawsoft.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de auditoría en el sistema.
 *
 * Almacena información detallada sobre las acciones realizadas por los usuarios,
 * incluyendo qué se modificó, quién lo hizo, cuándo y desde dónde.
 *
 * Campos clave:
 * - userId, userRole, userName: Identifica quién realizó la acción
 * - action, description: Describe qué se hizo
 * - entity, entityId: Identifica sobre qué entidad se actuó
 * - oldValue, newValue: Valores antes y después del cambio (formato JSON)
 * - ipAddress, userAgent: Información de la sesión
 * - createdAt: Timestamp de la acción
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
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "user_id", nullable = false)
   private Integer userId;

   @Enumerated(EnumType.STRING)
   @Column(name = "user_role", nullable = false, length = 20)
   private UserRole userRole;

   @Column(name = "user_name", nullable = false, length = 100)
   private String userName;

   @Column(name = "action", nullable = false, length = 100)
   private String action;

   @Column(name = "description", length = 255)
   private String description;

   @Column(name = "entity", nullable = false, length = 50)
   private String entity;

   @Column(name = "entity_id")
   private Integer entityId;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "old_value", columnDefinition = "json")
   private String oldValue;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "new_value", columnDefinition = "json")
   private String newValue;

   @Column(name = "ip_address", length = 45)
   private String ipAddress;

   @Column(name = "user_agent", length = 255)
   private String userAgent;

   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @PrePersist
   protected void onCreate() {
       this.createdAt = LocalDateTime.now();
   }

   public enum UserRole {
       ADMIN, VETERINARIAN, RECEPTIONIST, CLIENT
   }
}