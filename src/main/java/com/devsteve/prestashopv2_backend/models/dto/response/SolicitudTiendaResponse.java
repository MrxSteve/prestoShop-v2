package com.devsteve.prestashopv2_backend.models.dto.response;

import com.devsteve.prestashopv2_backend.models.entities.SolicitudTiendaEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudTiendaResponse {

    private Long id;
    private String nombreTienda;
    private String telefono;
    private String municipio;
    private String departamento;
    private String direccionExacta;
    private String nombreEncargado;
    private String emailEncargado;
    private String duiEncargado;
    private SolicitudTiendaEntity.EstadoSolicitud estado;
    private String descripcion;
    private String motivoRechazo;
    private LocalDateTime createdAt;
    private LocalDateTime fechaAprobacion;
    private String aprobadoPor;

    // DTO simplificado para listas
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Basica {
        private Long id;
        private String nombreTienda;
        private String nombreEncargado;
        private String emailEncargado;
        private SolicitudTiendaEntity.EstadoSolicitud estado;
        private LocalDateTime createdAt;
    }
}
