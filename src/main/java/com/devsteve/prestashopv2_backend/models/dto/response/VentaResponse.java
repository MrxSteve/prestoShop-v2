package com.devsteve.prestashopv2_backend.models.dto.response;

import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.models.enums.TipoVenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponse {

    private Long id;
    private TiendaBasicResponse tienda;
    private ClienteBasicResponse cliente;
    private String clienteOcasional;
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal;
    private BigDecimal total;
    private TipoVenta tipoVenta;
    private EstadoVenta estado;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DetalleVentaResponse> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TiendaBasicResponse {
        private Long id;
        private String nombre;
        private String telefono;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteBasicResponse {
        private Long id;
        private String nombreCompleto;
        private String email;
        private String telefono;
    }
}
