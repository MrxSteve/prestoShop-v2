package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.MovimientoTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.MovimientoTiendaResponse;
import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import com.devsteve.prestashopv2_backend.services.MovimientoTiendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Tienda", description = "Gestión de movimientos y estadísticas por tienda")
public class MovimientoTiendaController {

    private final MovimientoTiendaService movimientoTiendaService;

    @Operation(summary = "Crear nuevo movimiento", description = "Solo encargados y empleados pueden crear movimientos")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<MovimientoTiendaResponse> crear(@Valid @RequestBody MovimientoTiendaRequest request) {
        MovimientoTiendaResponse movimiento = movimientoTiendaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }

    @Operation(summary = "Obtener movimiento por ID", description = "Solo encargados y empleados pueden ver movimientos de su tienda")
    @GetMapping("/{movimientoId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<MovimientoTiendaResponse> obtenerPorId(@PathVariable Long movimientoId) {
        Optional<MovimientoTiendaResponse> movimiento = movimientoTiendaService.buscarPorId(movimientoId);
        return movimiento.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los movimientos de una tienda", description = "Solo empleados de la tienda pueden ver los movimientos")
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<Page<MovimientoTiendaResponse>> listarMovimientosDeTienda(
            @PathVariable Long tiendaId,
            Pageable pageable) {
        Page<MovimientoTiendaResponse> movimientos = movimientoTiendaService.listarMovimientosDeTienda(tiendaId, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @Operation(summary = "Listar movimientos por tipo de evento", description = "Filtra movimientos por tipo de evento")
    @GetMapping("/tienda/{tiendaId}/tipo/{tipoEvento}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<Page<MovimientoTiendaResponse>> listarMovimientosPorTipo(
            @PathVariable Long tiendaId,
            @PathVariable TipoEvento tipoEvento,
            Pageable pageable) {
        Page<MovimientoTiendaResponse> movimientos = movimientoTiendaService.listarMovimientosPorTipo(tiendaId, tipoEvento, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @Operation(summary = "Listar movimientos por rango de fechas", description = "Filtra movimientos por rango de fechas")
    @GetMapping("/tienda/{tiendaId}/fecha")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<Page<MovimientoTiendaResponse>> listarMovimientosPorFecha(
            @PathVariable Long tiendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {
        Page<MovimientoTiendaResponse> movimientos = movimientoTiendaService.listarMovimientosPorFecha(tiendaId, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(movimientos);
    }

    // ENDPOINTS DE ESTADÍSTICAS

    @Operation(summary = "Obtener estadísticas del día", description = "Obtiene totales de ventas y abonos del día actual")
    @GetMapping("/tienda/{tiendaId}/estadisticas/dia")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> obtenerEstadisticasDelDia(@PathVariable Long tiendaId) {
        BigDecimal totalVentas = movimientoTiendaService.obtenerTotalVentasDelDia(tiendaId);
        BigDecimal totalAbonos = movimientoTiendaService.obtenerTotalAbonosDelDia(tiendaId);

        Map<String, BigDecimal> estadisticas = Map.of(
            "totalVentasDelDia", totalVentas,
            "totalAbonosDelDia", totalAbonos,
            "totalDelDia", totalVentas.add(totalAbonos)
        );

        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Obtener estadísticas del mes", description = "Obtiene totales de ventas y abonos del mes actual")
    @GetMapping("/tienda/{tiendaId}/estadisticas/mes")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> obtenerEstadisticasDelMes(@PathVariable Long tiendaId) {
        BigDecimal totalVentas = movimientoTiendaService.obtenerTotalVentasDelMes(tiendaId);
        BigDecimal totalAbonos = movimientoTiendaService.obtenerTotalAbonosDelMes(tiendaId);

        Map<String, BigDecimal> estadisticas = Map.of(
            "totalVentasDelMes", totalVentas,
            "totalAbonosDelMes", totalAbonos,
            "totalDelMes", totalVentas.add(totalAbonos)
        );

        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Obtener total de ventas del día", description = "Obtiene el total de ventas registradas en el día")
    @GetMapping("/tienda/{tiendaId}/ventas/dia")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<BigDecimal> obtenerTotalVentasDelDia(@PathVariable Long tiendaId) {
        BigDecimal total = movimientoTiendaService.obtenerTotalVentasDelDia(tiendaId);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Obtener total de abonos del día", description = "Obtiene el total de abonos registrados en el día")
    @GetMapping("/tienda/{tiendaId}/abonos/dia")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<BigDecimal> obtenerTotalAbonosDelDia(@PathVariable Long tiendaId) {
        BigDecimal total = movimientoTiendaService.obtenerTotalAbonosDelDia(tiendaId);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Obtener total de ventas del mes", description = "Obtiene el total de ventas registradas en el mes")
    @GetMapping("/tienda/{tiendaId}/ventas/mes")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<BigDecimal> obtenerTotalVentasDelMes(@PathVariable Long tiendaId) {
        BigDecimal total = movimientoTiendaService.obtenerTotalVentasDelMes(tiendaId);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Obtener total de abonos del mes", description = "Obtiene el total de abonos registrados en el mes")
    @GetMapping("/tienda/{tiendaId}/abonos/mes")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<BigDecimal> obtenerTotalAbonosDelMes(@PathVariable Long tiendaId) {
        BigDecimal total = movimientoTiendaService.obtenerTotalAbonosDelMes(tiendaId);
        return ResponseEntity.ok(total);
    }
}
