package com.devsteve.prestashopv2_backend.models.dto.request.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsuarioRequest {

    private String nombreCompleto;

    @Email(message = "Email debe tener formato válido")
    private String email;

    private String telefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    private Boolean activo;
}
