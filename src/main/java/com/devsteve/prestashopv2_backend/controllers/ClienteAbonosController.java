package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.response.AbonoResponse;
import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.services.AbonoService;
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
@RequestMapping("/api/cliente/abonos")
@RequiredArgsConstructor
@Tag(name = "Cliente Abonos", description = "Endpoints para que los clientes consulten sus abonos")
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteAbonosController {

    private final AbonoService abonoService;

    @GetMapping
    @Operation(summary = "Listar todos mis abonos",
              description = "Obtiene todos los abonos del cliente autenticado")
    public ResponseEntity<List<AbonoResponse>> listarMisAbonos() {
        List<AbonoResponse> abonos = abonoService.listarMisAbonos();
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar mis abonos por estado",
              description = "Obtiene los abonos del cliente filtrados por estado")
    public ResponseEntity<List<AbonoResponse>> listarMisAbonosPorEstado(
            @Parameter(description = "Estado del abono") @PathVariable EstadoAbono estado) {
        List<AbonoResponse> abonos = abonoService.listarMisAbonosPorEstado(estado);
        return ResponseEntity.ok(abonos);
    }

    @GetMapping("/{abonoId}")
    @Operation(summary = "Obtener detalle de un abono",
              description = "Obtiene el detalle completo de un abono específico")
    public ResponseEntity<AbonoResponse> obtenerMiAbono(
            @Parameter(description = "ID del abono") @PathVariable Long abonoId) {
        Optional<AbonoResponse> abono = abonoService.obtenerMiAbono(abonoId);
        return abono.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tienda/{tiendaId}")
    @Operation(summary = "Listar mis abonos por tienda",
              description = "Obtiene todos los abonos del cliente en una tienda específica")
    public ResponseEntity<List<AbonoResponse>> listarMisAbonosPorTienda(
            @Parameter(description = "ID de la tienda") @PathVariable Long tiendaId) {
        List<AbonoResponse> abonos = abonoService.listarMisAbonosPorTienda(tiendaId);
        return ResponseEntity.ok(abonos);
    }
}
