package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.ChangePasswordRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.LoginRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RegistroUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.SolicitudTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AuthResponse;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.models.dto.response.UsuarioResponse;
import com.devsteve.prestashopv2_backend.services.auth.AuthService;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

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

    @GetMapping("/profile")
    @Operation(summary = "Ver mi perfil", description = "Obtener información del perfil del usuario autenticado")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil() {
        UsuarioResponse response = authService.obtenerMiPerfil();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Actualizar mi perfil", description = "Actualizar información del perfil del usuario autenticado")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(@Valid @RequestBody UpdateUsuarioRequest request) {
        UsuarioResponse response = authService.actualizarMiPerfil(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-tiendas")
    @Operation(summary = "Ver mis tiendas", description = "Obtener todas las tiendas donde estoy registrado")
    public ResponseEntity<List<UsuarioResponse.TiendaBasicResponse>> obtenerMisTiendas() {
        List<UsuarioResponse.TiendaBasicResponse> response = usuarioService.obtenerMisTiendas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tiendas-disponibles")
    @Operation(summary = "Ver tiendas disponibles", description = "Obtener tiendas donde puedo registrarme como cliente")
    public ResponseEntity<List<UsuarioResponse.TiendaBasicResponse>> listarTiendasDisponibles() {
        List<UsuarioResponse.TiendaBasicResponse> response = usuarioService.listarTiendasDisponibles();
        return ResponseEntity.ok(response);
    }
}
