package com.devsteve.prestashopv2_backend.models.dto.request;

import com.devsteve.prestashopv2_backend.models.enums.TipoVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {

    @NotNull(message = "El tipo de venta es requerido")
    private TipoVenta tipoVenta;

    private Long cuentaClienteId; // Para ventas a crédito o contado con cliente registrado

    @Size(max = 100, message = "El nombre del cliente ocasional no puede exceder 100 caracteres")
    private String clienteOcasional; // Para ventas al contado sin cliente registrado

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un producto en la venta")
    @Valid
    private List<DetalleVentaRequest> detalles;
}
