package co.edu.uniquindio.backendpawsoft.exception;

import co.edu.uniquindio.backendpawsoft.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Clase encargada de manejar de manera global
 * las excepciones generadas dentro de la aplicación.
 *
 * <p>
 * Utiliza la anotación {@code @RestControllerAdvice}
 * para interceptar excepciones lanzadas en los controladores
 * y devolver respuestas estructuradas y coherentes
 * con los estándares de una API REST.
 * </p>
 *
 * <p>
 * Permite centralizar el manejo de errores,
 * evitando la repetición de código en cada controlador
 * y garantizando uniformidad en las respuestas.
 * </p>
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo RuntimeException.
     *
     * <p>
     * Se utiliza para capturar errores de negocio como:
     * - Usuario no encontrado
     * - Credenciales inválidas
     * - Conflictos de registro
     * </p>
     *
     * @param ex excepción capturada
     * @return respuesta estructurada con estado HTTP 400
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones generadas por validaciones
     * de datos utilizando la anotación {@code @Valid}.
     *
     * <p>
     * Se activa cuando un campo obligatorio no cumple
     * las restricciones establecidas en el DTO.
     * </p>
     *
     * @param ex excepción de validación capturada
     * @return respuesta estructurada con estado HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja las excepciones de tipo {@link UnauthorizedException}.
     *
     * Esta excepción se lanza cuando ocurre un error de autenticación,
     * por ejemplo, cuando las credenciales proporcionadas por el usuario
     * no son válidas.
     *
     * Retorna una respuesta estructurada con código HTTP 401
     * (Unauthorized) y un mensaje descriptivo del error.
     *
     * @param ex excepción capturada durante el proceso de autenticación
     * @return ResponseEntity con el detalle del error y estado HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Maneja las excepciones de tipo {@link NotFoundException}.
     *
     *
     * Esta excepción se lanza cuando un recurso solicitado
     * no existe en el sistema, como por ejemplo un usuario
     * que no se encuentra registrado en la base de datos.
     *
     *
     * Retorna una respuesta estructurada con código HTTP 404
     * (Not Found) y un mensaje descriptivo del error.
     *
     * @param ex excepción capturada cuando no se encuentra el recurso
     * @return ResponseEntity con el detalle del error y estado HTTP 404
     */

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
