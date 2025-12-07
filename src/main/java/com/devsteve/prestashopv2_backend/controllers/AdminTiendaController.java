package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearTiendaRequest;
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
@RequestMapping("/api/admin/tiendas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSADMIN')")
@Tag(name = "Administración - Tiendas", description = "Gestión de tiendas para administradores")
public class AdminTiendaController {

    private final TiendaService tiendaService;

    @PostMapping
    @Operation(summary = "Crear tienda", description = "Crear una nueva tienda en el sistema")
    public ResponseEntity<TiendaResponse> crearTienda(@Valid @RequestBody CrearTiendaRequest request) {
        TiendaResponse response = tiendaService.crearTienda(request);
        return ResponseEntity.ok(response);
    }
}
