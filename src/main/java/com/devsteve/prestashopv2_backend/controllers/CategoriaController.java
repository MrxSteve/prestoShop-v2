package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CategoriaResponse;
import com.devsteve.prestashopv2_backend.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías por tienda")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar categorías de una tienda", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<CategoriaResponse>> listarCategoriasDeTienda(@PathVariable Long tiendaId) {
        List<CategoriaResponse> categorias = categoriaService.listarCategoriasDeTienda(tiendaId);
        return ResponseEntity.ok(categorias);
    }

    @Operation(summary = "Obtener categoría por ID", description = "Público - no requiere autenticación")
    @GetMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResponse> obtenerCategoriaPorId(@PathVariable Long categoriaId) {
        CategoriaResponse categoria = categoriaService.obtenerCategoriaPorId(categoriaId);
        return ResponseEntity.ok(categoria);
    }

    @Operation(summary = "Buscar categorías por nombre", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}/buscar")
    public ResponseEntity<List<CategoriaResponse>> buscarCategoriasPorNombre(
            @PathVariable Long tiendaId,
            @RequestParam String nombre) {
        List<CategoriaResponse> categorias = categoriaService.buscarCategoriasPorNombre(tiendaId, nombre);
        return ResponseEntity.ok(categorias);
    }

    @Operation(summary = "Crear nueva categoría", description = "Solo encargados y empleados pueden crear categorías")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CrearCategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @Operation(summary = "Actualizar categoría", description = "Solo encargados y empleados pueden actualizar categorías")
    @PutMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<CategoriaResponse> actualizarCategoria(
            @PathVariable Long categoriaId,
            @Valid @RequestBody UpdateCategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.actualizarCategoria(categoriaId, request);
        return ResponseEntity.ok(categoria);
    }

    @Operation(summary = "Eliminar categoría", description = "Solo encargados y empleados pueden eliminar categorías")
    @DeleteMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long categoriaId) {
        categoriaService.eliminarCategoria(categoriaId);
        return ResponseEntity.noContent().build();
    }
}
