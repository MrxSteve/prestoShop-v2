package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private List<String> roles;
    private List<TiendaBasicResponse> tiendas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TiendaBasicResponse {
        private Long id;
        private String nombre;
        private Boolean activo; // Si está activo en esta tienda
    }
}
