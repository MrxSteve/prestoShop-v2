package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.VentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.VentaResponse;
import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas por tienda")
public class VentaController {

    private final VentaService ventaService;

    @Operation(summary = "Crear nueva venta", description = "Solo encargados y empleados pueden crear ventas")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest request) {
        VentaResponse venta = ventaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @Operation(summary = "Obtener venta por ID", description = "Solo encargados y empleados pueden ver ventas de su tienda")
    @GetMapping("/{ventaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long ventaId) {
        Optional<VentaResponse> venta = ventaService.buscarPorId(ventaId);
        return venta.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todas las ventas de una tienda", description = "Solo empleados de la tienda pueden ver las ventas")
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<List<VentaResponse>> listarVentasDeTienda(@PathVariable Long tiendaId) {
        List<VentaResponse> ventas = ventaService.listarVentasDeTienda(tiendaId);
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Listar ventas por estado de una tienda", description = "Solo empleados de la tienda pueden ver las ventas")
    @GetMapping("/tienda/{tiendaId}/estado/{estado}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<List<VentaResponse>> listarVentasPorEstado(
            @PathVariable Long tiendaId,
            @PathVariable EstadoVenta estado) {
        List<VentaResponse> ventas = ventaService.listarVentasPorEstado(tiendaId, estado);
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Cancelar venta", description = "Solo encargados y empleados pueden cancelar ventas pendientes")
    @PutMapping("/{ventaId}/cancelar")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<VentaResponse> cancelarVenta(@PathVariable Long ventaId) {
        VentaResponse venta = ventaService.cancelarVenta(ventaId);
        return ResponseEntity.ok(venta);
    }

    @Operation(summary = "Marcar venta como pagada", description = "Solo encargados y empleados pueden marcar ventas como pagadas")
    @PutMapping("/{ventaId}/marcar-pagada")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<VentaResponse> marcarComoPagada(@PathVariable Long ventaId) {
        VentaResponse venta = ventaService.marcarComoPagada(ventaId);
        return ResponseEntity.ok(venta);
    }

    @Operation(summary = "Marcar todas las ventas de un cliente como pagadas", description = "Solo encargados y empleados pueden marcar ventas masivas como pagadas")
    @PutMapping("/cliente/{clienteId}/marcar-todas-pagadas")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<List<VentaResponse>> marcarTodasVentasClienteComoPagadas(@PathVariable Long clienteId) {
        List<VentaResponse> ventas = ventaService.marcarTodasVentasClienteComoPagadas(clienteId);
        return ResponseEntity.ok(ventas);
    }
}
