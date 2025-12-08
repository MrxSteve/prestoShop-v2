package com.devsteve.prestashopv2_backend.services.email;

import com.devsteve.prestashopv2_backend.models.entities.AbonoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbonoEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@prestashop.com}")
    private String fromEmail;

    public void enviarComprobanteAbono(AbonoEntity abono) {
        try {
            if (abono.getCuentaCliente() == null || abono.getCuentaCliente().getUsuario() == null) {
                log.warn("No se puede enviar comprobante de abono {} - Cliente o cuenta no disponible", abono.getId());
                return;
            }

            String destinatario = abono.getCuentaCliente().getUsuario().getEmail();
            String asunto = "Comprobante de Abono #" + abono.getId() + " - " + abono.getTienda().getNombre();

            // Preparar el contexto para Thymeleaf
            Context context = new Context(Locale.getDefault());
            context.setVariable("abono", abono);
            context.setVariable("cliente", abono.getCuentaCliente().getUsuario());
            context.setVariable("tienda", abono.getTienda());
            context.setVariable("fechaFormateada", abono.getFechaAbono().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("montoFormateado", String.format("$%.2f", abono.getMonto()));
            context.setVariable("estadoTexto", getEstadoTexto(abono.getEstado().name()));
            context.setVariable("metodoPagoTexto", getMetodoPagoTexto(abono.getMetodoPago().name()));

            // Generar el HTML del email usando la plantilla
            String contenidoHtml = templateEngine.process("comprobante-abono", context);

            // Crear y enviar el email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);

            log.info("Comprobante de abono #{} enviado exitosamente a {}", abono.getId(), destinatario);

        } catch (MessagingException e) {
            log.error("Error al enviar comprobante de abono #{}: {}", abono.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar comprobante de abono #{}: {}", abono.getId(), e.getMessage());
        }
    }

    private String getEstadoTexto(String estado) {
        return switch (estado) {
            case "APLICADO" -> "Aplicado";
            case "PENDIENTE" -> "Pendiente";
            case "RECHAZADO" -> "Rechazado";
            default -> estado;
        };
    }

    private String getMetodoPagoTexto(String metodoPago) {
        return switch (metodoPago) {
            case "EFECTIVO" -> "Efectivo";
            case "TARJETA" -> "Tarjeta";
            case "TRANSFERENCIA" -> "Transferencia";
            case "CHEQUE" -> "Cheque";
            default -> metodoPago;
        };
    }
}
