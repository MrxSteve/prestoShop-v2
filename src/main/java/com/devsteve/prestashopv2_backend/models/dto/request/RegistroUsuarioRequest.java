package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @Email(message = "El email debe tener un formato válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private String telefono;

    private String dui;

    @NotNull(message = "La tienda es obligatoria")
    private Long tiendaId;

    @NotBlank(message = "El rol es obligatorio")
    private String rol; // EMPLEADO o CLIENTE
}
