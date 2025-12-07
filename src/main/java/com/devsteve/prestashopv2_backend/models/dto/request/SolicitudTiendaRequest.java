package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudTiendaRequest {

    @NotBlank(message = "El nombre de la tienda es obligatorio")
    private String nombreTienda;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotNull(message = "El municipio es obligatorio")
    private Integer municipioId;

    @NotBlank(message = "La dirección exacta es obligatoria")
    private String direccionExacta;

    @NotBlank(message = "El nombre del encargado es obligatorio")
    private String nombreEncargado;

    @Email(message = "El email debe tener un formato válido")
    @NotBlank(message = "El email del encargado es obligatorio")
    private String emailEncargado;

    private String duiEncargado;

    private String descripcion;
}
