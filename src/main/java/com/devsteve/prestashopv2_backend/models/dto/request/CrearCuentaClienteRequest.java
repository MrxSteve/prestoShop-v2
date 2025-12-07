package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearCuentaClienteRequest {

    @NotNull(message = "El ID del usuario es requerido")
    private Long usuarioId;

    @NotNull(message = "El límite de crédito es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El límite de crédito debe ser mayor que 0")
    private BigDecimal limiteCredito;
}
