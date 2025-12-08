package com.devsteve.prestashopv2_backend.models.dto.request;

import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoTiendaRequest {

    @NotNull(message = "El tipo de evento es requerido")
    private TipoEvento tipoEvento;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    private BigDecimal monto;

    private Long clienteUsuarioId;

    private Long referenciaId;

    private String referenciaTabla;
}
