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
 * - eliminar códigos no usados antiguos (limpieza de basura)
 * - eliminar códigos usados/fallidos con más de 30 días (limpieza de auditoría)
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
     * @param codigo  valor del código 2FA a validar
     * @return Optional con el código 2FA si coincide y no ha sido usado; vacío en caso contrario
     */
    Optional<Codigo2FA> findByUsuarioAndCodigoAndUsadoFalse(User usuario, String codigo);

    /**
     * Elimina los códigos 2FA no usados cuya fecha de creación sea anterior al límite indicado.
     *
     * Se usa para limpiar registros que nunca fueron procesados y ya no tienen utilidad
     * (el usuario abandonó el flujo sin verificar ni solicitar nuevo código).
     *
     * Solo elimina registros con {@code usado = false} para no afectar el historial de auditoría.
     *
     * Importante: al ser una operación de escritura, debe ejecutarse dentro de una transacción.
     *
     * @param limite fecha/hora límite; se eliminan registros no usados con {@code creadoEn} anterior a este valor
     */
    @Modifying
    @Query("DELETE FROM Codigo2FA c WHERE c.usado = false AND c.creadoEn < :limite")
    void deleteNoUsadosAntiguos(@Param("limite") LocalDateTime limite);

    /**
     * Elimina los códigos 2FA ya usados o procesados cuya fecha de uso sea anterior al límite indicado.
     *
     * Se usa para limpiar el historial de auditoría después de 30 días, evitando crecimiento
     * indefinido de la tabla mientras se conserva trazabilidad reciente.
     *
     * Solo elimina registros con {@code usado = true} y {@code fechaUso} definida,
     * lo que garantiza que nunca se borre un código todavía activo.
     *
     * Importante: al ser una operación de escritura, debe ejecutarse dentro de una transacción.
     *
     * @param limite fecha/hora límite; se eliminan registros usados con {@code fechaUso} anterior a este valor
     */
    @Modifying
    @Query("DELETE FROM Codigo2FA c WHERE c.usado = true AND c.fechaUso < :limite")
    void deleteUsadosAntiguos(@Param("limite") LocalDateTime limite);


    Optional<Codigo2FA> findTopByUsuarioOrderByIdDesc(User usuario);

    
}