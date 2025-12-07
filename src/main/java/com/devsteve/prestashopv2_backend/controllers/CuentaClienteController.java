package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCuentaClienteRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateEstadoCuentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateLimiteCreditoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CuentaClienteResponse;
import com.devsteve.prestashopv2_backend.services.CuentaClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas-cliente")
@RequiredArgsConstructor
@Tag(name = "Cuentas Cliente", description = "Gestión de cuentas de cliente")
public class CuentaClienteController {

    private final CuentaClienteService cuentaClienteService;

    @Operation(summary = "Crear cuenta de cliente", description = "Solo encargados y empleados pueden crear cuentas")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CuentaClienteResponse> crearCuenta(@Valid @RequestBody CrearCuentaClienteRequest request) {
        CuentaClienteResponse cuenta = cuentaClienteService.crearCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuenta);
    }

    @Operation(summary = "Listar todas las cuentas de la tienda", description = "Solo empleados de la tienda pueden ver las cuentas")
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasRole('SYSADMIN') or hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<List<CuentaClienteResponse>> listarCuentasDeTienda(@PathVariable Long tiendaId) {
        List<CuentaClienteResponse> cuentas = cuentaClienteService.listarCuentasDeTienda(tiendaId);
        return ResponseEntity.ok(cuentas);
    }

    @Operation(summary = "Listar cuentas activas de la tienda", description = "Solo empleados de la tienda pueden ver las cuentas activas")
    @GetMapping("/tienda/{tiendaId}/activas")
    @PreAuthorize("hasRole('SYSADMIN') or hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<List<CuentaClienteResponse>> listarCuentasActivasDeTienda(@PathVariable Long tiendaId) {
        List<CuentaClienteResponse> cuentas = cuentaClienteService.listarCuentasActivasDeTienda(tiendaId);
        return ResponseEntity.ok(cuentas);
    }

    @Operation(summary = "Obtener cuenta por ID", description = "Solo el propietario, empleados de la tienda o admin pueden ver la cuenta")
    @GetMapping("/{cuentaId}")
    @PreAuthorize("hasRole('SYSADMIN') or hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('CLIENTE')")
    public ResponseEntity<CuentaClienteResponse> obtenerCuentaPorId(@PathVariable Long cuentaId) {
        CuentaClienteResponse cuenta = cuentaClienteService.obtenerCuentaPorId(cuentaId);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Actualizar límite de crédito", description = "Solo encargados y empleados pueden actualizar límites")
    @PutMapping("/{cuentaId}/limite-credito")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CuentaClienteResponse> actualizarLimiteCredito(
            @PathVariable Long cuentaId,
            @Valid @RequestBody UpdateLimiteCreditoRequest request) {
        CuentaClienteResponse cuenta = cuentaClienteService.actualizarLimiteCredito(cuentaId, request);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Actualizar estado de cuenta", description = "Solo encargados y empleados pueden cambiar el estado")
    @PutMapping("/{cuentaId}/estado")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CuentaClienteResponse> actualizarEstado(
            @PathVariable Long cuentaId,
            @Valid @RequestBody UpdateEstadoCuentaRequest request) {
        CuentaClienteResponse cuenta = cuentaClienteService.actualizarEstado(cuentaId, request);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Verificar si puede realizar compra", description = "Verificar si la cuenta tiene saldo suficiente para una compra")
    @GetMapping("/{cuentaId}/puede-comprar")
    @PreAuthorize("hasRole('SYSADMIN') or hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<Boolean> puedeRealizarCompra(
            @PathVariable Long cuentaId,
            @RequestParam BigDecimal monto) {
        boolean puedeComprar = cuentaClienteService.puedeRealizarCompra(cuentaId, monto);
        return ResponseEntity.ok(puedeComprar);
    }

    @Operation(summary = "Cargar saldo a cuenta", description = "Solo encargados y empleados pueden cargar saldo")
    @PostMapping("/{cuentaId}/cargar-saldo")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CuentaClienteResponse> cargarSaldo(
            @PathVariable Long cuentaId,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false, defaultValue = "Carga de saldo") String concepto) {
        CuentaClienteResponse cuenta = cuentaClienteService.cargarSaldo(cuentaId, monto, concepto);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Abonar saldo de cuenta", description = "Solo encargados y empleados pueden realizar abonos")
    @PostMapping("/{cuentaId}/abonar-saldo")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CuentaClienteResponse> abonarSaldo(
            @PathVariable Long cuentaId,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false, defaultValue = "Abono a cuenta") String concepto) {
        CuentaClienteResponse cuenta = cuentaClienteService.abonarSaldo(cuentaId, monto, concepto);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Obtener mis cuentas", description = "El cliente puede ver sus propias cuentas")
    @GetMapping("/mis-cuentas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<CuentaClienteResponse>> obtenerMisCuentas() {
        List<CuentaClienteResponse> cuentas = cuentaClienteService.obtenerMisCuentas();
        return ResponseEntity.ok(cuentas);
    }
}
