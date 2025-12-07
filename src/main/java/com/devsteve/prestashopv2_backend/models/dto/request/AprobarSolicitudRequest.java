package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprobarSolicitudRequest {
    private String observaciones;

    @NotBlank(message = "El mensaje de bienvenida es obligatorio")
    private String mensajeBienvenida;

    // Opcional: límite de crédito inicial para los clientes de la tienda
    private Double limiteCreditoInicial;
}
