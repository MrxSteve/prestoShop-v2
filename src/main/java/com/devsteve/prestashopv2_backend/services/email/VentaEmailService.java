package com.devsteve.prestashopv2_backend.services.email;

import com.devsteve.prestashopv2_backend.models.entities.VentaEntity;
import com.devsteve.prestashopv2_backend.models.entities.DetalleVentaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaEmailService {

    private final EmailService emailService;

    public void enviarFacturaVenta(VentaEntity venta) {
        try {
            // Solo enviar si el cliente tiene email (cuenta registrada)
            if (venta.getCuentaCliente() == null ||
                venta.getCuentaCliente().getUsuario() == null ||
                venta.getCuentaCliente().getUsuario().getEmail() == null) {
                log.info("No se envía factura por email - cliente sin cuenta registrada: Venta #{}", venta.getId());
                return;
            }

            String emailCliente = venta.getCuentaCliente().getUsuario().getEmail();
            String nombreCliente = venta.getCuentaCliente().getUsuario().getNombreCompleto();

            // Preparar variables para el template
            Map<String, Object> variables = new HashMap<>();
            variables.put("numeroVenta", venta.getId());
            variables.put("nombreCliente", nombreCliente);
            variables.put("nombreTienda", venta.getTienda().getNombre());
            variables.put("telefonoTienda", venta.getTienda().getTelefono());
            variables.put("fechaVenta", venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            variables.put("tipoVenta", venta.getTipoVenta().name());
            variables.put("estado", venta.getEstado().name());

            // Formatear montos
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "SV"));
            variables.put("subtotal", currencyFormat.format(venta.getSubtotal()));
            variables.put("total", currencyFormat.format(venta.getTotal()));

            if (venta.getObservaciones() != null && !venta.getObservaciones().trim().isEmpty()) {
                variables.put("observaciones", venta.getObservaciones());
            }

            // Preparar detalles de la venta
            variables.put("detalles", venta.getDetalleVentas());
            variables.put("currencyFormat", currencyFormat);

            // Determinar asunto según el tipo de venta
            String asunto;
            if (venta.getTipoVenta().name().equals("CREDITO")) {
                asunto = String.format("Factura de Compra a Crédito #%d - %s",
                    venta.getId(), venta.getTienda().getNombre());
            } else {
                asunto = String.format("Factura de Compra #%d - %s",
                    venta.getId(), venta.getTienda().getNombre());
            }

            // Enviar email
            emailService.enviarEmail(emailCliente, asunto, "factura-venta", variables);

            log.info("Factura enviada por email para venta #{} al cliente: {}",
                venta.getId(), emailCliente);

        } catch (Exception e) {
            log.error("Error al enviar factura por email para venta #{}: {}",
                venta.getId(), e.getMessage(), e);
        }
    }
}
