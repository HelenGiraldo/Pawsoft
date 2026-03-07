package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.enums.ResultadoCodigo;
import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Servicio encargado de gestionar el segundo factor de autenticación (2FA).
 *
 * Responsabilidades:
 * - Generar códigos de verificación seguros de 6 dígitos.
 * - Crear y persistir un nuevo código 2FA para un usuario, invalidando el anterior si existe.
 * - Validar el código ingresado por el usuario aplicando reglas de expiración y fuerza bruta.
 * - Registrar el resultado de cada intento (EXITOSO, FALLIDO, EXPIRADO, INVALIDADO)
 *   junto con la IP de origen para mantener trazabilidad completa en la base de datos.
 *
 * Reglas de negocio:
 * - Cada código es válido por {@value #MINUTOS_EXPIRACION} minutos desde su creación.
 * - El usuario debe esperar al menos {@value #SEGUNDOS_ENTRE_REENVIOS} segundos antes
 *   de poder solicitar un nuevo código.
 * - Se permiten máximo {@value #MAX_REENVIOS} reenvíos por sesión de login.
 *   Superado ese límite, el usuario queda bloqueado temporalmente.
 * - El bloqueo por reenvíos es gradual: cada vez que se alcanza el límite,
 *   el tiempo de espera se incrementa en {@value #MINUTOS_BLOQUEO_BASE} minutos
 *   multiplicados por el número de veces que se ha bloqueado (1er bloqueo = 5 min,
 *   2do = 10 min, 3er = 15 min, y así sucesivamente).
 * - Se permiten máximo {@value #MAX_INTENTOS} intentos fallidos de verificación por código.
 *   Tras agotar los intentos, se aplica el mismo esquema de bloqueo gradual.
 * - Si se solicita un nuevo código con uno activo, el anterior se invalida y queda
 *   registrado con resultado {@code INVALIDADO} para fines de auditoría.
 *
 * Captura de IP:
 * - La IP de origen se recibe como parámetro desde el controlador, que tiene acceso
 *   al {@code HttpServletRequest}. El servicio no depende de la capa HTTP directamente,
 *   lo que mantiene la separación de responsabilidades.
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
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    /** Número máximo de intentos fallidos de verificación antes de bloquear el código. */
    private static final int MAX_INTENTOS = 3;

    /** Minutos de validez de un código desde su creación. */
    private static final int MINUTOS_EXPIRACION = 3;

    /**
     * Segundos mínimos que deben transcurrir entre un reenvío y el siguiente.
     * Evita que el usuario solicite códigos en ráfaga.
     */
    private static final long SEGUNDOS_ENTRE_REENVIOS = 60;

    /**
     * Número máximo de reenvíos permitidos antes de aplicar un bloqueo gradual.
     * Al superar este límite, el usuario no puede solicitar más códigos por un tiempo.
     */
    private static final int MAX_REENVIOS = 5;

    /**
     * Unidad base en minutos para el cálculo del bloqueo gradual.
     *
     * El tiempo de bloqueo se calcula como:
     * {@code MINUTOS_BLOQUEO_BASE × numeroDeBloqueosAcumulados}
     *
     * Ejemplos:
     * - 1er bloqueo: 5 min
     * - 2do bloqueo: 10 min
     * - 3er bloqueo: 15 min
     */
    private static final long MINUTOS_BLOQUEO_BASE = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Codigo2FARepository codigo2FARepository;

    // ── Generación ────────────────────────────────────────────────────────────

    /**
     * Genera un código numérico aleatorio de 6 dígitos usando {@link SecureRandom}.
     *
     * Se usa {@code SecureRandom} en lugar de {@code Random} para garantizar
     * imprevisibilidad criptográfica y reducir el riesgo de predicción del código.
     *
     * @return cadena de 6 dígitos con cero a la izquierda si es necesario (ej: "007542")
     */
    public String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    // ── Creación / Reenvío ────────────────────────────────────────────────────

    /**
     * Crea y persiste un nuevo código 2FA para el usuario indicado.
     *
     * Antes de generar un nuevo código se aplican las siguientes validaciones:
     *
     * 1. Si existe un código activo con bloqueo vigente (por exceso de reenvíos
     *    o de intentos fallidos), se lanza excepción y no se genera nada.
     * 2. Si existe un código activo creado hace menos de {@value #SEGUNDOS_ENTRE_REENVIOS}
     *    segundos, se lanza excepción indicando cuántos segundos debe esperar el usuario.
     * 3. Si el número de reenvíos ya alcanzó {@value #MAX_REENVIOS}, se aplica un bloqueo
     *    gradual incrementando el contador de bloqueos acumulados, y se lanza excepción.
     * 4. Si existe un código activo que pasa todas las validaciones anteriores,
     *    se invalida y queda registrado con resultado {@code INVALIDADO}.
     *
     * La IP se almacena en el nuevo código para saber desde dónde se solicitó.
     *
     * @param usuario   usuario para quien se genera el código
     * @param ipOrigen  dirección IP desde donde se realizó la solicitud;
     *                  puede ser {@code null} si no está disponible en el contexto
     * @return entidad {@link Codigo2FA} recién creada y persistida
     * @throws UnauthorizedException si hay bloqueo activo, se supera el cooldown de reenvío
     *                               o se alcanzan los reenvíos máximos
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Codigo2FA crearCodigo(User usuario, String ipOrigen) {

        codigo2FARepository.findByUsuarioAndUsadoFalse(usuario)
                .ifPresent(codigoActivo -> {

                    // 1. Bloqueo activo — no se puede solicitar ningún código aún
                    verificarBloqueoActivo(codigoActivo);

                    // 2. Cooldown entre reenvíos — demasiado pronto para uno nuevo
                    long segundosTranscurridos = java.time.Duration
                            .between(codigoActivo.getCreadoEn(), LocalDateTime.now())
                            .getSeconds();

                    if (segundosTranscurridos < SEGUNDOS_ENTRE_REENVIOS) {
                        long espera = SEGUNDOS_ENTRE_REENVIOS - segundosTranscurridos;
                        throw new UnauthorizedException(
                                "Debes esperar " + espera + " segundo(s) antes de solicitar un nuevo código."
                        );
                    }

                    // 3. Límite de reenvíos alcanzado — aplica bloqueo gradual
                    int reenvios = codigoActivo.getCantidadReenvios() + 1;
                    if (reenvios > MAX_REENVIOS) {
                        int bloqueosAcumulados = codigoActivo.getBloqueosAcumulados() + 1;
                        long minutosBloqueo = MINUTOS_BLOQUEO_BASE * bloqueosAcumulados;

                        codigoActivo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
                        codigoActivo.setBloqueosAcumulados(bloqueosAcumulados);
                        codigoActivo.setUsado(true);
                        codigoActivo.setResultado(ResultadoCodigo.INVALIDADO);
                        codigoActivo.setFechaUso(LocalDateTime.now());
                        codigoActivo.setIpOrigen(ipOrigen);
                        codigo2FARepository.save(codigoActivo);

                        throw new UnauthorizedException(
                                "Has superado el límite de reenvíos. " +
                                        "Intenta nuevamente en " + minutosBloqueo + " minuto(s)."
                        );
                    }

                    // 4. Código activo válido — se invalida para dar paso al nuevo
                    codigoActivo.setCantidadReenvios(reenvios);
                    codigoActivo.setUsado(true);
                    codigoActivo.setResultado(ResultadoCodigo.INVALIDADO);
                    codigoActivo.setFechaUso(LocalDateTime.now());
                    codigoActivo.setIpOrigen(ipOrigen);
                    codigo2FARepository.save(codigoActivo);
                });

        // Recupera el contador de bloqueos acumulados del último código del usuario
        // para mantener la progresión del bloqueo gradual entre sesiones
        int bloqueosAcumulados = codigo2FARepository
                .findTopByUsuarioOrderByIdDesc(usuario)
                .map(Codigo2FA::getBloqueosAcumulados)
                .orElse(0);

        // Crea el nuevo código con todos sus campos inicializados
        Codigo2FA nuevo = new Codigo2FA();
        nuevo.setUsuario(usuario);
        nuevo.setCodigo(generateCode());
        nuevo.setCreadoEn(LocalDateTime.now());
        nuevo.setExpiraEn(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
        nuevo.setUsado(false);
        nuevo.setIntentosFallidos(0);
        nuevo.setCantidadReenvios(0);
        nuevo.setBloqueadoHasta(null);
        nuevo.setBloqueosAcumulados(bloqueosAcumulados); // mantiene progresión gradual
        nuevo.setResultado(null);                        // null mientras esté activo y pendiente
        nuevo.setFechaUso(null);                         // null hasta que sea procesado
        nuevo.setIpOrigen(ipOrigen);

        return codigo2FARepository.save(nuevo);
    }

    // ── Validación ────────────────────────────────────────────────────────────

    /**
     * Valida el código ingresado por el usuario aplicando todas las reglas de seguridad.
     *
     * Flujo de validación:
     * 1. Busca el código activo (no usado) del usuario; si no existe lanza excepción.
     * 2. Verifica si hay un bloqueo por fuerza bruta activo.
     * 3. Verifica si el código ha expirado; si es así lo marca como EXPIRADO.
     * 4. Compara el código ingresado con el almacenado:
     *    - Si no coincide: incrementa intentos fallidos, registra FALLIDO con IP,
     *      aplica bloqueo gradual si se alcanzó el límite y lanza excepción
     *      con el número de intentos restantes.
     *    - Si coincide: marca el código como EXITOSO con IP y finaliza sin excepción.
     *
     * Todos los resultados (FALLIDO, EXPIRADO, EXITOSO) quedan registrados en BD
     * junto con {@code fechaUso} e {@code ipOrigen} para auditoría completa.
     *
     * Nota sobre {@code noRollbackFor}:
     * {@code UnauthorizedException} es un error controlado de negocio. Sin esta anotación,
     * Spring haría rollback al lanzarla y los cambios de auditoría (intentos fallidos,
     * resultado, IP) se perderían. Con {@code noRollbackFor} esos cambios se persisten
     * aunque la transacción termine con excepción.
     *
     * @param usuario   usuario que intenta verificar el código
     * @param ingresado código de 6 dígitos ingresado por el usuario
     * @param ipOrigen  dirección IP desde donde se realizó el intento de verificación
     * @throws UnauthorizedException si no hay código activo, está bloqueado, expiró o es incorrecto
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public void validarCodigo(User usuario, String ingresado, String ipOrigen) {
        Codigo2FA codigo = codigo2FARepository
                .findByUsuarioAndUsadoFalse(usuario)
                .orElseThrow(() -> new UnauthorizedException(
                        "No existe un código activo. Solicita uno nuevo."
                ));

        // Verifica bloqueo activo antes de cualquier otra validación
        verificarBloqueoActivo(codigo);

        // Verifica si el código ya expiró por tiempo
        if (codigo.getExpiraEn().isBefore(LocalDateTime.now())) {
            codigo.setUsado(true);
            codigo.setResultado(ResultadoCodigo.EXPIRADO);
            codigo.setFechaUso(LocalDateTime.now());
            codigo.setIpOrigen(ipOrigen);
            codigo2FARepository.save(codigo);
            throw new UnauthorizedException(
                    "El código ha expirado. Solicita uno nuevo."
            );
        }

        // Verifica si el código ingresado es incorrecto
        if (!codigo.getCodigo().equals(ingresado)) {
            int nuevosFallos = codigo.getIntentosFallidos() + 1;
            codigo.setIntentosFallidos(nuevosFallos);
            codigo.setResultado(ResultadoCodigo.FALLIDO);
            codigo.setFechaUso(LocalDateTime.now());
            codigo.setIpOrigen(ipOrigen);

            // Si se alcanza el máximo de intentos, aplica bloqueo gradual
            if (nuevosFallos >= MAX_INTENTOS) {
                int bloqueosAcumulados = codigo.getBloqueosAcumulados() + 1;
                long minutosBloqueo = MINUTOS_BLOQUEO_BASE * bloqueosAcumulados;

                codigo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
                codigo.setBloqueosAcumulados(bloqueosAcumulados);
                codigo2FARepository.save(codigo);

                throw new UnauthorizedException(
                        "Demasiados intentos fallidos. " +
                                "Intenta nuevamente en " + minutosBloqueo + " minuto(s)."
                );
            }

            codigo2FARepository.save(codigo);
            int restantes = MAX_INTENTOS - nuevosFallos;
            throw new UnauthorizedException("Código incorrecto. Intentos restantes: " + restantes);
        }

        // Código correcto — registra éxito con IP y marca como usado
        codigo.setUsado(true);
        codigo.setResultado(ResultadoCodigo.EXITOSO);
        codigo.setFechaUso(LocalDateTime.now());
        codigo.setIpOrigen(ipOrigen);
        codigo2FARepository.save(codigo);
    }

    // ── Política de bloqueo ───────────────────────────────────────────────────

    /**
     * Verifica si el código tiene un bloqueo activo (por exceso de reenvíos
     * o por exceso de intentos fallidos de verificación).
     *
     * Si el bloqueo ya expiró (la fecha de bloqueo es anterior a ahora), lo resetea
     * automáticamente para permitir nuevos intentos sin intervención manual.
     * El contador de bloqueos acumulados NO se resetea, para que el bloqueo
     * siguiente siga siendo más largo (esquema gradual).
     *
     * Si el bloqueo sigue vigente, lanza {@link UnauthorizedException} indicando
     * cuántos minutos faltan aproximadamente para que se libere.
     *
     * @param codigo código 2FA a evaluar
     * @throws UnauthorizedException si el bloqueo sigue activo
     */
    private void verificarBloqueoActivo(Codigo2FA codigo) {
        LocalDateTime bloqueadoHasta = codigo.getBloqueadoHasta();
        if (bloqueadoHasta == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (bloqueadoHasta.isAfter(now)) {
            long minutosRestantes = java.time.Duration
                    .between(now, bloqueadoHasta)
                    .toMinutes() + 1; // +1 para redondear hacia arriba
            throw new UnauthorizedException(
                    "Cuenta bloqueada temporalmente. " +
                            "Intenta nuevamente en aproximadamente " + minutosRestantes + " minuto(s)."
            );
        }

        // El bloqueo ya expiró — resetea el cooldown pero mantiene el contador de bloqueos
        codigo.setBloqueadoHasta(null);
        codigo.setIntentosFallidos(0);
        codigo2FARepository.save(codigo);
    }
}