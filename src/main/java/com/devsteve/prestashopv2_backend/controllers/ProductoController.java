package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.ProductoResponse;
import com.devsteve.prestashopv2_backend.services.ProductoService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos por tienda")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar todos los productos de una tienda", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<ProductoResponse>> listarProductosDeTienda(@PathVariable Long tiendaId) {
        List<ProductoResponse> productos = productoService.listarProductosDeTienda(tiendaId);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Listar productos activos de una tienda", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}/activos")
    public ResponseEntity<List<ProductoResponse>> listarProductosActivosDeTienda(@PathVariable Long tiendaId) {
        List<ProductoResponse> productos = productoService.listarProductosActivosDeTienda(tiendaId);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Obtener producto por ID", description = "Público - no requiere autenticación")
    @GetMapping("/{productoId}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Long productoId) {
        ProductoResponse producto = productoService.obtenerProductoPorId(productoId);
        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Buscar productos por nombre", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarProductosPorNombre(
            @PathVariable Long tiendaId,
            @RequestParam String nombre) {
        List<ProductoResponse> productos = productoService.buscarProductosPorNombre(tiendaId, nombre);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Buscar productos activos por nombre", description = "Público - no requiere autenticación")
    @GetMapping("/tienda/{tiendaId}/buscar/activos")
    public ResponseEntity<List<ProductoResponse>> buscarProductosActivosPorNombre(
            @PathVariable Long tiendaId,
            @RequestParam String nombre) {
        List<ProductoResponse> productos = productoService.buscarProductosActivosPorNombre(tiendaId, nombre);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Crear nuevo producto", description = "Solo encargados y empleados pueden crear productos")
    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody CrearProductoRequest request) {
        ProductoResponse producto = productoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @Operation(summary = "Actualizar producto", description = "Solo encargados y empleados pueden actualizar productos")
    @PutMapping("/{productoId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long productoId,
            @Valid @RequestBody UpdateProductoRequest request) {
        ProductoResponse producto = productoService.actualizarProducto(productoId, request);
        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Eliminar producto", description = "Solo encargados y empleados pueden eliminar productos")
    @DeleteMapping("/{productoId}")
    @PreAuthorize("hasRole('ENCARGADO') or hasRole('EMPLEADO')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long productoId) {
        productoService.eliminarProducto(productoId);
        return ResponseEntity.noContent().build();
    }
}
