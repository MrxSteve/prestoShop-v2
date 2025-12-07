package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.UsuarioResponse;
import com.devsteve.prestashopv2_backend.services.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
@Tag(name = "Gestión de Tienda", description = "Gestión de usuarios para encargados y empleados")
public class TiendaUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/clientes")
    @Operation(summary = "Crear cliente", description = "Crear un nuevo cliente para la tienda")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<UsuarioResponse> crearCliente(@Valid @RequestBody CrearUsuarioRequest request) {
        request.setRol("CLIENTE");
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/empleados")
    @Operation(summary = "Crear empleado", description = "Crear un nuevo empleado para la tienda")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearUsuarioRequest request) {
        request.setRol("EMPLEADO");
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tiendaId}/clientes")
    @Operation(summary = "Listar clientes de tienda", description = "Obtener todos los clientes de una tienda específica")
    public ResponseEntity<List<UsuarioResponse>> listarClientesDeTienda(@PathVariable Long tiendaId) {
        List<UsuarioResponse> response = usuarioService.listarClientesDeTienda(tiendaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tiendaId}/empleados")
    @Operation(summary = "Listar empleados de tienda", description = "Obtener todos los empleados de una tienda específica")
    public ResponseEntity<List<UsuarioResponse>> listarEmpleadosDeTienda(@PathVariable Long tiendaId) {
        List<UsuarioResponse> response = usuarioService.listarEmpleadosDeTienda(tiendaId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/clientes/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualizar información de un cliente")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<UsuarioResponse> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tiendaId}/clientes/{usuarioId}/activar")
    @Operation(summary = "Activar cliente en tienda", description = "Activar un cliente específico en una tienda")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<Void> activarClienteEnTienda(
            @PathVariable Long tiendaId,
            @PathVariable Long usuarioId) {
        usuarioService.activarClienteEnTienda(usuarioId, tiendaId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tiendaId}/empleados/{usuarioId}/activar")
    @Operation(summary = "Activar empleado en tienda", description = "Activar un empleado específico en una tienda")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<Void> activarEmpleadoEnTienda(
            @PathVariable Long tiendaId,
            @PathVariable Long usuarioId) {
        usuarioService.activarEmpleadoEnTienda(usuarioId, tiendaId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tiendaId}/empleados/{usuarioId}/desactivar")
    @Operation(summary = "Desactivar empleado en tienda", description = "Desactivar un empleado específico en una tienda")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<Void> desactivarEmpleadoEnTienda(
            @PathVariable Long tiendaId,
            @PathVariable Long usuarioId) {
        usuarioService.desactivarEmpleadoEnTienda(usuarioId, tiendaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clientes/{id}")
    @Operation(summary = "Desactivar cliente", description = "Desactivar un cliente")
    @PreAuthorize("hasRole('ENCARGADO')")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
