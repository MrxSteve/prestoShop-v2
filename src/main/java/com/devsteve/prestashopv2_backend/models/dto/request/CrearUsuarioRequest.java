package com.devsteve.prestashopv2_backend.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearUsuarioRequest {

    @NotBlank(message = "El nombre completo es requerido")
    private String nombreCompleto;

    @Email(message = "Email debe tener formato válido")
    @NotBlank(message = "El email es requerido")
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    private String telefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "La tienda es requerida")
    private Long tiendaId;

    @NotBlank(message = "El rol es requerido")
    private String rol; // CLIENTE, EMPLEADO, ENCARGADO, SYSADMIN
}
