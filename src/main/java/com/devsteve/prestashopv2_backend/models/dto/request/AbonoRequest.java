package com.devsteve.prestashopv2_backend.models.dto.request;

import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.models.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonoRequest {

    @NotNull(message = "La cuenta del cliente es requerida")
    private Long cuentaClienteId;

    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    private String observaciones;

    private EstadoAbono estado = EstadoAbono.APLICADO;
}
