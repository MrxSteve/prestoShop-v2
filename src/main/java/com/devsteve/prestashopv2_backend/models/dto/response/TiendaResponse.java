package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiendaResponse {

    private Long id;
    private String nombre;
    private String telefono;
    private String logoUrl;
    private String direccionExacta;
    private Boolean activo;
    private MunicipioBasicResponse municipio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MunicipioBasicResponse {
        private Integer id;
        private String nombre;
        private DepartamentoBasicResponse departamento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartamentoBasicResponse {
        private Integer id;
        private String nombre;
    }
}
