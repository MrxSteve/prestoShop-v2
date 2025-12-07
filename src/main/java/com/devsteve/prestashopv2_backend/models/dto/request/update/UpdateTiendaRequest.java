package com.devsteve.prestashopv2_backend.models.dto.request.update;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTiendaRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    private String telefono;

    @Size(max = 300, message = "La dirección exacta no puede exceder 300 caracteres")
    private String direccionExacta;

    private String logoUrl;

    private Integer municipioId;
}
