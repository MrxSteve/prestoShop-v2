package com.devsteve.prestashopv2_backend.models.dto.response;

import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
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
public class MovimientoTiendaResponse {

    private Long id;
    private Long tiendaId;
    private String tiendaNombre;
    private Long usuarioOperadorId;
    private String usuarioOperadorNombre;
    private String usuarioOperadorEmail;
    private Long clienteUsuarioId;
    private String clienteUsuarioNombre;
    private String clienteUsuarioEmail;
    private TipoEvento tipoEvento;
    private String descripcion;
    private BigDecimal monto;
    private Long referenciaId;
    private String referenciaTabla;
    private LocalDateTime fechaEvento;
}
