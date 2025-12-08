package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.response.VentaResponse;
import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cliente/compras")
@RequiredArgsConstructor
@Tag(name = "Cliente Compras", description = "Endpoints para que los clientes consulten sus compras")
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteComprasController {

    private final VentaService ventaService;

    @GetMapping
    @Operation(summary = "Listar todas mis compras",
              description = "Obtiene todas las compras del cliente autenticado")
    public ResponseEntity<List<VentaResponse>> listarMisCompras() {
        List<VentaResponse> compras = ventaService.listarMisCompras();
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar mis compras por estado",
              description = "Obtiene las compras del cliente filtradas por estado")
    public ResponseEntity<List<VentaResponse>> listarMisComprasPorEstado(
            @Parameter(description = "Estado de la venta") @PathVariable EstadoVenta estado) {
        List<VentaResponse> compras = ventaService.listarMisComprasPorEstado(estado);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/{ventaId}")
    @Operation(summary = "Obtener detalle de una compra",
              description = "Obtiene el detalle completo de una compra específica")
    public ResponseEntity<VentaResponse> obtenerDetalleCompra(
            @Parameter(description = "ID de la venta") @PathVariable Long ventaId) {
        Optional<VentaResponse> compra = ventaService.obtenerDetalleCompra(ventaId);
        return compra.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tienda/{tiendaId}")
    @Operation(summary = "Listar mis compras por tienda",
              description = "Obtiene todas las compras del cliente en una tienda específica")
    public ResponseEntity<List<VentaResponse>> listarMisComprasPorTienda(
            @Parameter(description = "ID de la tienda") @PathVariable Long tiendaId) {
        List<VentaResponse> compras = ventaService.listarMisComprasPorTienda(tiendaId);
        return ResponseEntity.ok(compras);
    }
}
