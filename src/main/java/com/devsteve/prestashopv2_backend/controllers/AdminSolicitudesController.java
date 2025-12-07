package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.AprobarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RechazarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.services.auth.AdminSolicitudesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/solicitudes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSADMIN')")
@Tag(name = "Administración de Solicitudes", description = "Endpoints para que los administradores gestionen solicitudes de tiendas")
public class AdminSolicitudesController {
    private final AdminSolicitudesService adminSolicitudesService;

    @GetMapping("/pendientes")
    @Operation(summary = "Listar solicitudes pendientes", description = "Obtener todas las solicitudes en estado PENDIENTE")
    public ResponseEntity<List<SolicitudTiendaResponse.Basica>> obtenerSolicitudesPendientes() {
        List<SolicitudTiendaResponse.Basica> solicitudes = adminSolicitudesService.obtenerSolicitudesPendientes();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{solicitudId}")
    @Operation(summary = "Ver detalle de solicitud", description = "Obtener información completa de una solicitud específica")
    public ResponseEntity<SolicitudTiendaResponse> obtenerSolicitudDetalle(@PathVariable Long solicitudId) {
        SolicitudTiendaResponse solicitud = adminSolicitudesService.obtenerSolicitudDetalle(solicitudId);
        return ResponseEntity.ok(solicitud);
    }

    @PostMapping("/{solicitudId}/aprobar")
    @Operation(summary = "Aprobar solicitud", description = "Aprobar una solicitud y crear automáticamente la tienda y usuario encargado")
    public ResponseEntity<SolicitudTiendaResponse> aprobarSolicitud(
            @PathVariable Long solicitudId,
            @Valid @RequestBody AprobarSolicitudRequest request,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        SolicitudTiendaResponse resultado = adminSolicitudesService.aprobarSolicitud(solicitudId, request, adminEmail);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{solicitudId}/rechazar")
    @Operation(summary = "Rechazar solicitud", description = "Rechazar una solicitud con motivo específico")
    public ResponseEntity<SolicitudTiendaResponse> rechazarSolicitud(
            @PathVariable Long solicitudId,
            @Valid @RequestBody RechazarSolicitudRequest request,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        SolicitudTiendaResponse resultado = adminSolicitudesService.rechazarSolicitud(solicitudId, request, adminEmail);
        return ResponseEntity.ok(resultado);
    }
}
