package co.edu.uniquindio.backendpawsoft.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable del envío de correos electrónicos del sistema.
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

    private final JavaMailSender mailSender;

    // ── Plantilla base HTML ──────────────────────────────────────────────────
    private String wrapTemplate(String contenido) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:12px;overflow:hidden;
                                box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,#2d6a4f,#52b788);
                                 padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:28px;
                                   font-weight:700;letter-spacing:1px;">
                          🐾 PawSoft
                        </h1>
                        <p style="margin:6px 0 0;color:#d8f3dc;font-size:13px;">
                          Sistema de Gestión Veterinaria
                        </p>
                      </td>
                    </tr>

                    <!-- Contenido -->
                    <tr>
                      <td style="padding:36px 40px;">
                        %s
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#f8f9fa;padding:20px 40px;
                                 text-align:center;border-top:1px solid #e9ecef;">
                        <p style="margin:0;color:#adb5bd;font-size:12px;">
                          © 2026 PawSoft · Universidad del Quindío<br/>
                          Si no realizaste esta acción, ignora este correo.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(contenido);
    }

    private void enviar(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        }
    }

    // ── Verificación de cuenta ───────────────────────────────────────────────
    public void sendVerificationEmail(String to, String token) {
        String link = "http://localhost:8080/auth/verify-email?token=" + token;

        String contenido = """
            <h2 style="color:#2d6a4f;margin:0 0 12px;">¡Bienvenido a PawSoft! 🎉</h2>
            <p style="color:#495057;font-size:15px;line-height:1.6;margin:0 0 24px;">
              Gracias por registrarte. Para activar tu cuenta y comenzar a usar
              nuestros servicios, confirma tu correo electrónico haciendo clic
              en el botón de abajo.
            </p>
            <div style="text-align:center;margin:28px 0;">
              <a href="%s"
                 style="background:linear-gradient(135deg,#2d6a4f,#52b788);
                        color:#ffffff;text-decoration:none;padding:14px 36px;
                        border-radius:8px;font-size:15px;font-weight:600;
                        display:inline-block;">
                ✅ Verificar mi cuenta
              </a>
            </div>
            <p style="color:#868e96;font-size:13px;text-align:center;margin:0;">
              Este enlace expira en <strong>24 horas</strong>.
            </p>
            """.formatted(link);

        enviar(to, "PawSoft — Verifica tu cuenta", wrapTemplate(contenido));
    }

    // ── Código 2FA ───────────────────────────────────────────────────────────
    public void enviarCodigo2FA(String destinatario, String codigo) {
        String contenido = """
            <h2 style="color:#2d6a4f;margin:0 0 12px;">Código de verificación</h2>
            <p style="color:#495057;font-size:15px;line-height:1.6;margin:0 0 24px;">
              Usa el siguiente código para completar tu inicio de sesión en PawSoft.
            </p>
            <div style="text-align:center;margin:28px 0;">
              <div style="display:inline-block;background:#f1f8f4;border:2px dashed #52b788;
                          border-radius:12px;padding:20px 48px;">
                <span style="font-size:36px;font-weight:700;color:#2d6a4f;
                             letter-spacing:8px;">%s</span>
              </div>
            </div>
            <p style="color:#868e96;font-size:13px;text-align:center;margin:0;">
              Expira en <strong> 3 minutos</strong>. No compartas este código con nadie.
            </p>
            """.formatted(codigo);

        enviar(destinatario, "PawSoft — Código de verificación", wrapTemplate(contenido));
    }

    // ── Contraseña temporal ──────────────────────────────────────────────────
    public void sendTemporaryPassword(String destinatario, String password) {
        String contenido = """
            <h2 style="color:#2d6a4f;margin:0 0 12px;">Tu cuenta ha sido creada 🐾</h2>
            <p style="color:#495057;font-size:15px;line-height:1.6;margin:0 0 24px;">
              Un administrador ha creado tu cuenta en PawSoft.
              Tu contraseña temporal es:
            </p>
            <div style="text-align:center;margin:28px 0;">
              <div style="display:inline-block;background:#fff3cd;border:2px solid #ffc107;
                          border-radius:10px;padding:16px 40px;">
                <span style="font-size:22px;font-weight:700;color:#856404;
                             letter-spacing:3px;">%s</span>
              </div>
            </div>
            <p style="color:#495057;font-size:14px;text-align:center;margin:0 0 8px;">
              Deberás cambiarla en tu primer inicio de sesión.
            </p>
            <p style="color:#868e96;font-size:13px;text-align:center;margin:0;">
              Por seguridad, no compartas esta contraseña con nadie.
            </p>
            """.formatted(password);

        enviar(destinatario, "PawSoft — Contraseña temporal", wrapTemplate(contenido));
    }

    // ── Recuperación de contraseña ───────────────────────────────────────────
    public void sendPasswordResetEmail(String to, String resetLink) {
        String contenido = """
            <h2 style="color:#2d6a4f;margin:0 0 12px;">Recupera tu contraseña 🔐</h2>
            <p style="color:#495057;font-size:15px;line-height:1.6;margin:0 0 24px;">
              Recibimos una solicitud para restablecer la contraseña de tu cuenta.
              Haz clic en el botón de abajo para continuar.
            </p>
            <div style="text-align:center;margin:28px 0;">
              <a href="%s"
                 style="background:linear-gradient(135deg,#e63946,#c1121f);
                        color:#ffffff;text-decoration:none;padding:14px 36px;
                        border-radius:8px;font-size:15px;font-weight:600;
                        display:inline-block;">
                🔑 Restablecer contraseña
              </a>
            </div>
            <p style="color:#868e96;font-size:13px;text-align:center;margin:0;">
              Este enlace expira en <strong>5 minutos</strong>.
              Si no solicitaste este cambio, ignora este correo.
            </p>
            """.formatted(resetLink);

        enviar(to, "PawSoft — Recuperación de contraseña", wrapTemplate(contenido));
    }
}