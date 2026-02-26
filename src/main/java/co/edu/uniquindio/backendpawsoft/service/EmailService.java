package co.edu.uniquindio.backendpawsoft.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable del envío de correos electrónicos del sistema.
 *
 * Centraliza la comunicación por correo usada en procesos de seguridad y notificaciones,
 * como:
 * - envío de códigos de verificación 2FA
 * - envío de contraseñas temporales para usuarios staff
 *
 * Requiere configuración previa en application.properties:
 * - spring.mail.host
 * - spring.mail.port
 * - spring.mail.username
 * - spring.mail.password
 * - spring.mail.properties.mail.smtp.auth
 * - spring.mail.properties.mail.smtp.starttls.enable
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
public class EmailService {

    /**
     * Componente de Spring encargado del envío de correos.
     */
    private final JavaMailSender mailSender;

    /**
     * Envía un código de verificación 2FA al correo del usuario.
     *
     * @param destinatario correo del usuario
     * @param codigo código de verificación generado
     */
    public void enviarCodigo2FA(String destinatario, String codigo) {

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("PawSoft — Código de verificación");
        mensaje.setText(
                "Tu código de verificación es: " + codigo +
                        "\n\nEste código expira en 10 minutos." +
                        "\nSi no solicitaste este código, ignora este correo."
        );

        mailSender.send(mensaje);
    }

    /**
     * Envía una contraseña temporal a un usuario staff recién creado.
     *
     * Esta contraseña se utiliza únicamente para el primer inicio de sesión y debe ser
     * cambiada por el usuario según la regla de primer acceso.
     *
     * @param destinatario correo del usuario
     * @param password contraseña temporal en texto plano
     */
    public void sendTemporaryPassword(String destinatario, String password) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("PawSoft — Contraseña temporal");
        mensaje.setText(
                "Se ha creado tu cuenta de PawSoft.\n" +
                        "Tu contraseña temporal es: " + password + "\n" +
                        "Recuerda cambiarla en tu primer inicio de sesión."
        );
        mailSender.send(mensaje);
    }

    public void sendPasswordResetEmail(String to, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - PawSoft");
        message.setText(
                "Has solicitado recuperar tu contraseña.\n\n" +
                        "Haz clic en el siguiente enlace para continuar:\n\n" +
                        resetLink +
                        "\n\nEste enlace expira en 5 minutos.\n" +
                        "Si no solicitaste este cambio, ignora este correo."
        );

        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String token) {
        String link = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verifica tu cuenta");
    }
}