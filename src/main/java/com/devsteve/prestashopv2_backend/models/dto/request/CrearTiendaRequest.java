package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearTiendaRequest {

    @NotBlank(message = "El nombre de la tienda es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    private String telefono;

    @Size(max = 300, message = "La dirección exacta no puede exceder 300 caracteres")
    private String direccionExacta;

    private String logoUrl;

    @NotNull(message = "El municipio es requerido")
    private Integer municipioId;
}
