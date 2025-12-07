package com.devsteve.prestashopv2_backend.services.email;

import com.devsteve.prestashopv2_backend.models.dto.request.AprobarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RechazarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.entities.SolicitudTiendaEntity;
import com.devsteve.prestashopv2_backend.models.entities.TiendaEntity;
import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudTiendaEmailService {
    private final EmailService emailService;

    /**
     * Envía las credenciales de acceso al encargado con datos ya resueltos
     */
    @Async
    public void enviarCredencialesEncargadoConDatos(String nombreEncargado, String emailEncargado,
                                                   String passwordTemporal, String nombreTienda,
                                                   String telefonoTienda, String direccionTienda,
                                                   String municipio, String departamento,
                                                   AprobarSolicitudRequest request) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombreEncargado", nombreEncargado);
            variables.put("emailEncargado", emailEncargado);
            variables.put("passwordTemporal", passwordTemporal);
            variables.put("nombreTienda", nombreTienda);
            variables.put("telefonoTienda", telefonoTienda);
            variables.put("direccionTienda", direccionTienda);
            variables.put("municipio", municipio);
            variables.put("departamento", departamento);
            variables.put("mensajeBienvenida", request.getMensajeBienvenida());
            variables.put("observaciones", request.getObservaciones());
            variables.put("limiteCreditoInicial", request.getLimiteCreditoInicial());

            String asunto = "¡Bienvenido! Tu tienda \"" + nombreTienda + "\" ha sido aprobada";

            emailService.enviarEmail(
                emailEncargado,
                asunto,
                "solicitud-aprobada",
                variables
            );

            log.info("Email de credenciales enviado exitosamente a: {} para tienda: {}",
                    emailEncargado, nombreTienda);

        } catch (Exception e) {
            log.error("Error enviando credenciales a {}: {}", emailEncargado, e.getMessage(), e);
            // No lanzamos la excepción para no afectar el proceso principal de aprobación
        }
    }

    /**
     * Envía notificación de rechazo de solicitud
     */
    @Async
    public void enviarNotificacionRechazo(SolicitudTiendaEntity solicitud, RechazarSolicitudRequest request) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombreEncargado", solicitud.getNombreEncargado());
            variables.put("nombreTienda", solicitud.getNombreTienda());
            variables.put("motivoRechazo", request.getMotivo());
            variables.put("observaciones", request.getObservaciones());
            variables.put("emailSoporte", "soporte@shopmoney.com"); // Configurable

            String asunto = "Solicitud de tienda \"" + solicitud.getNombreTienda() + "\" - Información importante";

            emailService.enviarEmail(
                solicitud.getEmailEncargado(),
                asunto,
                "solicitud-rechazada",
                variables
            );

            log.info("Email de rechazo enviado exitosamente a: {} para tienda: {}",
                    solicitud.getEmailEncargado(), solicitud.getNombreTienda());

        } catch (Exception e) {
            log.error("Error enviando notificación de rechazo a {}: {}",
                    solicitud.getEmailEncargado(), e.getMessage(), e);
            // No lanzamos la excepción para no afectar el proceso principal
        }
    }

    /**
     * Envía confirmación de recepción de solicitud
     */
    @Async
    public void enviarConfirmacionSolicitud(SolicitudTiendaEntity solicitud) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombreEncargado", solicitud.getNombreEncargado());
            variables.put("nombreTienda", solicitud.getNombreTienda());
            variables.put("numeroSolicitud", solicitud.getId());
            variables.put("fechaSolicitud", solicitud.getCreatedAt());
            variables.put("emailSoporte", "soporte@shopmoney.com");

            String asunto = "Solicitud recibida - Tienda \"" + solicitud.getNombreTienda() + "\"";

            emailService.enviarEmail(
                solicitud.getEmailEncargado(),
                asunto,
                "solicitud-recibida",
                variables
            );

            log.info("Email de confirmación enviado exitosamente a: {} para solicitud: {}",
                    solicitud.getEmailEncargado(), solicitud.getId());

        } catch (Exception e) {
            log.error("Error enviando confirmación de solicitud a {}: {}",
                    solicitud.getEmailEncargado(), e.getMessage(), e);
        }
    }
}
