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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSADMIN')")
@Tag(name = "Administración - Usuarios", description = "Gestión de usuarios para administradores")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/usuarios/encargados")
    @Operation(summary = "Crear encargado", description = "Crear un nuevo encargado de tienda")
    public ResponseEntity<UsuarioResponse> crearEncargado(@Valid @RequestBody CrearUsuarioRequest request) {
        // Forzar el rol a ENCARGADO
        request.setRol("ENCARGADO");
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/usuarios/empleados")
    @Operation(summary = "Crear empleado", description = "Crear un nuevo empleado")
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearUsuarioRequest request) {
        // Forzar el rol a EMPLEADO
        request.setRol("EMPLEADO");
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/usuarios/sysadmin")
    @Operation(summary = "Crear administrador", description = "Crear un nuevo administrador del sistema")
    public ResponseEntity<UsuarioResponse> crearSysAdmin(@Valid @RequestBody CrearUsuarioRequest request) {
        // Forzar el rol a SYSADMIN
        request.setRol("SYSADMIN");
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios/empleados")
    @Operation(summary = "Listar empleados y encargados", description = "Obtener todos los empleados y encargados del sistema")
    public ResponseEntity<List<UsuarioResponse>> listarEmpleados() {
        List<UsuarioResponse> response = usuarioService.listarEmpleados();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualizar cualquier usuario (solo admin)")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Desactivar usuario", description = "Desactivar cualquier usuario")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
