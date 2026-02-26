package co.edu.uniquindio.backendpawsoft.repository;

import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Codigo2FA}.
 *
 * Proporciona operaciones de persistencia y consultas específicas para soportar
 * el flujo de autenticación con segundo factor (2FA), como:
 * - buscar el código vigente (no usado) de un usuario
 * - validar un código por usuario y valor
 * - eliminar códigos antiguos para limpieza de registros
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
public interface Codigo2FARepository extends JpaRepository<Codigo2FA, Long> {

    /**
     * Busca el código 2FA vigente (no usado) asociado a un usuario.
     *
     * @param usuario usuario propietario del código
     * @return Optional con el código 2FA no usado si existe; vacío en caso contrario
     */
    Optional<Codigo2FA> findByUsuarioAndUsadoFalse(User usuario);

    /**
     * Busca un código 2FA no usado filtrando por usuario y por el valor del código.
     *
     * Se utiliza para verificar si el código ingresado por el usuario coincide con el que
     * está almacenado y aún no ha sido marcado como usado.
     *
     * @param usuario usuario propietario del código
     * @param codigo valor del código 2FA a validar
     * @return Optional con el código 2FA si coincide y no ha sido usado; vacío en caso contrario
     */
    Optional<Codigo2FA> findByUsuarioAndCodigoAndUsadoFalse(User usuario, String codigo);

    /**
     * Elimina los códigos 2FA cuya fecha de creación sea anterior al límite indicado.
     *
     * Este método se usa típicamente en tareas programadas de mantenimiento para evitar
     * crecimiento innecesario de la tabla.
     *
     * Importante: al ser una operación de escritura, debe ejecutarse dentro de una transacción.
     *
     * @param limite fecha/hora límite; se eliminan registros con creadoEn menor a este valor
     */
    @Modifying
    @Query("DELETE FROM Codigo2FA c WHERE c.creadoEn < :limite")
    void deleteAntiguos(@Param("limite") LocalDateTime limite);
}