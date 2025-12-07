package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaClienteResponse {

    private Long id;
    private ClienteBasicResponse cliente;
    private TiendaBasicResponse tienda;
    private BigDecimal limiteCredito;
    private BigDecimal saldoActual;
    private BigDecimal saldoDisponible;
    private LocalDate fechaApertura;
    private Boolean activa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteBasicResponse {
        private Long id;
        private String nombreCompleto;
        private String email;
        private String telefono;
        private Boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TiendaBasicResponse {
        private Long id;
        private String nombre;
        private String telefono;
        private Boolean activo;
    }
}
