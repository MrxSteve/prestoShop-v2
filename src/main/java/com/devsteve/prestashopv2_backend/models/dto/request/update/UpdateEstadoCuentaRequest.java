package com.devsteve.prestashopv2_backend.models.dto.request.update;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEstadoCuentaRequest {

    @NotNull(message = "El estado de la cuenta es requerido")
    private Boolean activa;
}
