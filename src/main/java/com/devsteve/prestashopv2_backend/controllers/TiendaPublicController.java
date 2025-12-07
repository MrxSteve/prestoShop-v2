package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.response.TiendaResponse;
import com.devsteve.prestashopv2_backend.services.TiendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/tiendas")
@RequiredArgsConstructor
@Tag(name = "Tiendas Públicas", description = "Endpoints públicos para consultar tiendas activas")
public class TiendaPublicController {

    private final TiendaService tiendaService;

    @GetMapping
    @Operation(summary = "Listar todas las tiendas activas", description = "Obtener todas las tiendas activas ordenadas por nombre")
    public ResponseEntity<List<TiendaResponse>> listarTiendasActivas() {
        List<TiendaResponse> response = tiendaService.listarTiendasActivas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/municipio/{municipioId}")
    @Operation(summary = "Listar tiendas por municipio", description = "Obtener tiendas activas de un municipio específico")
    public ResponseEntity<List<TiendaResponse>> listarTiendasPorMunicipio(@PathVariable Integer municipioId) {
        List<TiendaResponse> response = tiendaService.listarTiendasPorMunicipio(municipioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/departamento/{departamentoId}")
    @Operation(summary = "Listar tiendas por departamento", description = "Obtener tiendas activas de un departamento específico")
    public ResponseEntity<List<TiendaResponse>> listarTiendasPorDepartamento(@PathVariable Integer departamentoId) {
        List<TiendaResponse> response = tiendaService.listarTiendasPorDepartamento(departamentoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tiendaId}")
    @Operation(summary = "Obtener tienda", description = "Obtener información detallada de una tienda específica")
    public ResponseEntity<TiendaResponse> obtenerTienda(@PathVariable Long tiendaId) {
        TiendaResponse response = tiendaService.obtenerTienda(tiendaId);
        return ResponseEntity.ok(response);
    }
}
