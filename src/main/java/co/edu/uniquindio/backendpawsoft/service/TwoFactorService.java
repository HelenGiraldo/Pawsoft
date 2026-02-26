package co.edu.uniquindio.backendpawsoft.service;

import co.edu.uniquindio.backendpawsoft.exception.UnauthorizedException;
import co.edu.uniquindio.backendpawsoft.model.Codigo2FA;
import co.edu.uniquindio.backendpawsoft.model.User;
import co.edu.uniquindio.backendpawsoft.repository.Codigo2FARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_EXPIRACION = 10;
    private static final long MINUTOS_BLOQUEO = 1;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Codigo2FARepository codigo2FARepository;

    public String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    @Transactional
    public Codigo2FA crearCodigo(User usuario) {
        codigo2FARepository.findByUsuarioAndUsadoFalse(usuario)
                .ifPresent(codigoActivo -> {
                    aplicarPoliticaDeBloqueoSiCorresponde(codigoActivo);
                    codigoActivo.setUsado(true);
                    codigo2FARepository.save(codigoActivo);
                });

        Codigo2FA nuevo = new Codigo2FA();
        nuevo.setUsuario(usuario);
        nuevo.setCodigo(generateCode());
        nuevo.setCreadoEn(LocalDateTime.now());
        nuevo.setExpiraEn(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
        nuevo.setUsado(false);
        nuevo.setIntentosFallidos(0);
        nuevo.setBloqueadoHasta(null);

        return codigo2FARepository.save(nuevo);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public void validarCodigo(User usuario, String ingresado) {
        Codigo2FA codigo = codigo2FARepository
                .findByUsuarioAndUsadoFalse(usuario)
                .orElseThrow(() -> new UnauthorizedException(
                        "No existe un código activo. Solicita uno nuevo."
                ));

        aplicarPoliticaDeBloqueoSiCorresponde(codigo);

        if (codigo.getExpiraEn().isBefore(LocalDateTime.now())) {
            codigo.setUsado(true);
            codigo2FARepository.save(codigo);
            throw new UnauthorizedException("El código ha expirado. Inicia sesión nuevamente.");
        }

        if (!codigo.getCodigo().equals(ingresado)) {
            int nuevosFallos = codigo.getIntentosFallidos() + 1;
            codigo.setIntentosFallidos(nuevosFallos);

            if (nuevosFallos >= MAX_INTENTOS) {
                codigo.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
                codigo2FARepository.save(codigo);
                throw new UnauthorizedException("Demasiados intentos fallidos. Intenta nuevamente en 1 minuto.");
            }

            codigo2FARepository.save(codigo);
            int restantes = MAX_INTENTOS - nuevosFallos;
            throw new UnauthorizedException("Código incorrecto. Intentos restantes: " + restantes);
        }

        codigo.setUsado(true);
        codigo2FARepository.save(codigo);
    }

    private void aplicarPoliticaDeBloqueoSiCorresponde(Codigo2FA codigo) {
        LocalDateTime bloqueadoHasta = codigo.getBloqueadoHasta();
        if (bloqueadoHasta == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (bloqueadoHasta.isAfter(now)) {
            throw new UnauthorizedException("Demasiados intentos. Espera 1 minuto antes de volver a intentarlo.");
        }

        codigo.setBloqueadoHasta(null);
        codigo.setIntentosFallidos(0);
        codigo2FARepository.save(codigo);
    }
}