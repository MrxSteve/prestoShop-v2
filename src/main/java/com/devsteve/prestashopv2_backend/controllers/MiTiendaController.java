package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.TiendaResponse;
import com.devsteve.prestashopv2_backend.services.TiendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tienda/mi-tienda")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
@Tag(name = "Mi Tienda", description = "Gestión de tienda para encargados y empleados")
public class MiTiendaController {

    private final TiendaService tiendaService;

    @GetMapping
    @Operation(summary = "Ver mi tienda", description = "Obtener información de la tienda donde trabajo")
    public ResponseEntity<TiendaResponse> obtenerMiTienda() {
        TiendaResponse response = tiendaService.obtenerMiTienda();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Actualizar mi tienda", description = "Actualizar información de la tienda donde trabajo")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<TiendaResponse> actualizarMiTienda(@Valid @RequestBody UpdateTiendaRequest request) {
        TiendaResponse response = tiendaService.actualizarMiTienda(request);
        return ResponseEntity.ok(response);
    }
}
