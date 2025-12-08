package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precioVenta;
    private BigDecimal precioUnitario;
    private Boolean activo;
    private LocalDateTime createdAt;
    private TiendaBasicResponse tienda;
    private CategoriaBasicResponse categoria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TiendaBasicResponse {
        private Long id;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaBasicResponse {
        private Long id;
        private String nombre;
    }
}
