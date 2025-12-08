package com.devsteve.prestashopv2_backend.models.dto.response;

import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.models.enums.MetodoPago;
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
public class AbonoResponse {

    private Long id;
    private Long tiendaId;
    private String tiendaNombre;
    private Long cuentaClienteId;
    private String clienteNombreCompleto;
    private String clienteEmail;
    private BigDecimal monto;
    private LocalDateTime fechaAbono;
    private MetodoPago metodoPago;
    private String observaciones;
    private EstadoAbono estado;
}
