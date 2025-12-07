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
public class RechazarSolicitudRequest {

    @NotBlank(message = "El motivo de rechazo es obligatorio")
    private String motivo;

    private String observaciones;
}
