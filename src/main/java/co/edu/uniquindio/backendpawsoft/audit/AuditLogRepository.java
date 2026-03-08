package co.edu.uniquindio.backendpawsoft.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad AuditLog.
 *
 * Proporciona consultas para filtrar registros de auditoría por:
 * - Usuario específico
 * - Rol de usuario
 * - Entidad y ID de entidad
 * - Acción realizada
 * - Rango de fechas
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
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

     //Todos los logs de un usuario específico
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Integer userId);

     //Todos los logs de un rol (ej: todos los cambios hechos por ADMIN)
    List<AuditLog> findByUserRoleOrderByCreatedAtDesc(AuditLog.UserRole userRole);

     //Todos los logs sobre una entidad (ej: todos los cambios a CLIENT)
    List<AuditLog> findByEntityAndEntityIdOrderByCreatedAtDesc(String entity, Integer entityId);

     //Todos los logs de una acción específica
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

     //Logs en un rango de fechas
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
}