package com.devsteve.prestashopv2_backend.models.dto.request.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLimiteCreditoRequest {

    @NotNull(message = "El nuevo límite de crédito es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El límite de crédito debe ser mayor que 0")
    private BigDecimal nuevoLimite;
}
