package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.AbonoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AbonoResponse;
import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.services.AbonoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/abonos")
@RequiredArgsConstructor
@Tag(name = "Abonos", description = "Gestión de abonos por tienda")
public class AbonoController {

    private final AbonoService abonoService;

    @Operation(summary = "Crear nuevo abono", description = "Solo encargados y empleados pueden crear abonos")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<AbonoResponse> crear(@Valid @RequestBody AbonoRequest request) {
        AbonoResponse abono = abonoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(abono);
    }

    @Operation(summary = "Obtener abono por ID", description = "Solo encargados y empleados pueden ver abonos de su tienda")
    @GetMapping("/{abonoId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<AbonoResponse> obtenerPorId(@PathVariable Long abonoId) {
        Optional<AbonoResponse> abono = abonoService.buscarPorId(abonoId);
        return abono.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los abonos de una tienda", description = "Solo empleados de la tienda pueden ver los abonos")
    @GetMapping("/tienda/{tiendaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<List<AbonoResponse>> listarAbonosDeTienda(@PathVariable Long tiendaId) {
        List<AbonoResponse> abonos = abonoService.listarAbonosDeTienda(tiendaId);
        return ResponseEntity.ok(abonos);
    }

    @Operation(summary = "Listar abonos por estado de una tienda", description = "Solo empleados de la tienda pueden ver los abonos")
    @GetMapping("/tienda/{tiendaId}/estado/{estado}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<List<AbonoResponse>> listarAbonosPorEstado(
            @PathVariable Long tiendaId,
            @PathVariable EstadoAbono estado) {
        List<AbonoResponse> abonos = abonoService.listarAbonosPorEstado(tiendaId, estado);
        return ResponseEntity.ok(abonos);
    }

    @Operation(summary = "Listar abonos de un cliente", description = "Solo empleados de la tienda pueden ver los abonos del cliente")
    @GetMapping("/cliente/{cuentaClienteId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO') or hasRole('SYSADMIN')")
    public ResponseEntity<List<AbonoResponse>> listarAbonosPorCliente(@PathVariable Long cuentaClienteId) {
        List<AbonoResponse> abonos = abonoService.listarAbonosPorCliente(cuentaClienteId);
        return ResponseEntity.ok(abonos);
    }

    @Operation(summary = "Cambiar estado de abono", description = "Solo encargados y empleados pueden cambiar estados")
    @PutMapping("/{abonoId}/estado/{nuevoEstado}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<AbonoResponse> cambiarEstado(
            @Parameter(description = "ID del abono") @PathVariable Long abonoId,
            @Parameter(description = "Nuevo estado del abono") @PathVariable EstadoAbono nuevoEstado) {
        AbonoResponse abono = abonoService.cambiarEstado(abonoId, nuevoEstado);
        return ResponseEntity.ok(abono);
    }
}
