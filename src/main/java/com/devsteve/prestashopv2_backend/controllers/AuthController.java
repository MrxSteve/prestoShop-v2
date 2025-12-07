package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.ChangePasswordRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.LoginRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RegistroUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.SolicitudTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AuthResponse;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario", description = "Registrar nuevo empleado o cliente (requiere estar autenticado)")
    @PreAuthorize("hasRole('SYSADMIN') or hasRole('ENCARGADO')")
    public ResponseEntity<AuthResponse> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request) {
        AuthResponse response = authService.registrarUsuario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solicitud-tienda")
    @Operation(summary = "Solicitar apertura de tienda", description = "Crear solicitud para que una tienda use la plataforma")
    public ResponseEntity<SolicitudTiendaResponse> crearSolicitudTienda(@Valid @RequestBody SolicitudTiendaRequest request) {
        SolicitudTiendaResponse response = authService.crearSolicitudTienda(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambiar la contraseña del usuario autenticado")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        AuthResponse response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }
}
