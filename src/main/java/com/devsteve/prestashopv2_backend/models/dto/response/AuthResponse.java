package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tipo = "Bearer";
    private Long userId;
    private String nombreCompleto;
    private String email;
    private List<String> roles;
    private List<TiendaBasicaDto> tiendas; // Tiendas donde es empleado

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TiendaBasicaDto {
        private Long id;
        private String nombre;
        private String logoUrl;
    }
}
