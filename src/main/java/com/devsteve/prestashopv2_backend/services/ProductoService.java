package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.ProductoResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.ProductoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosDeTienda(Long tiendaId) {
        List<ProductoEntity> productos = productoRepository.findByTiendaIdOrderByNombreAsc(tiendaId);
        return productoMapper.toResponseList(productos);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosActivosDeTienda(Long tiendaId) {
        List<ProductoEntity> productos = productoRepository.findByTiendaIdAndActivoTrueOrderByNombreAsc(tiendaId);
        return productoMapper.toResponseList(productos);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(Long productoId) {
        ProductoEntity producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return productoMapper.toResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductosPorNombre(Long tiendaId, String nombre) {
        List<ProductoEntity> productos = productoRepository
            .findByTiendaIdAndNombreContainingIgnoreCaseOrderByNombreAsc(tiendaId, nombre);
        return productoMapper.toResponseList(productos);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductosActivosPorNombre(Long tiendaId, String nombre) {
        List<ProductoEntity> productos = productoRepository
            .findByTiendaIdAndActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(tiendaId, nombre);
        return productoMapper.toResponseList(productos);
    }

    @Transactional
    public ProductoResponse crearProducto(CrearProductoRequest request) {
        // Obtener usuario autenticado (debe ser encargado o empleado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Crear el nuevo producto
        ProductoEntity nuevoProducto = productoMapper.toEntity(request);
        nuevoProducto.setTienda(tienda);

        // Asignar categoría si se proporciona
        if (request.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findByIdAndTiendaId(
                request.getCategoriaId(), tienda.getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada en tu tienda"));
            nuevoProducto.setCategoria(categoria);
        }

        nuevoProducto = productoRepository.save(nuevoProducto);

        log.info("Producto creado: {} en tienda {} por {}",
                request.getNombre(), tienda.getNombre(), emailSolicitante);

        return productoMapper.toResponse(nuevoProducto);
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long productoId, UpdateProductoRequest request) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Buscar el producto en la tienda
        ProductoEntity producto = productoRepository.findByIdAndTiendaId(productoId, tienda.getId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado en tu tienda"));

        // Actualizar el producto
        productoMapper.updateEntityFromRequest(request, producto);

        // Actualizar categoría si se proporciona
        if (request.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findByIdAndTiendaId(
                request.getCategoriaId(), tienda.getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada en tu tienda"));
            producto.setCategoria(categoria);
        } else {
            producto.setCategoria(null);
        }

        producto = productoRepository.save(producto);

        log.info("Producto actualizado: {} en tienda {} por {}",
                request.getNombre(), tienda.getNombre(), emailSolicitante);

        return productoMapper.toResponse(producto);
    }

    @Transactional
    public void eliminarProducto(Long productoId) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Buscar el producto en la tienda
        ProductoEntity producto = productoRepository.findByIdAndTiendaId(productoId, tienda.getId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado en tu tienda"));

        // Verificar que no tenga ventas asociadas
        if (!producto.getDetallesVenta().isEmpty()) {
            throw new RuntimeException("No se puede eliminar el producto porque tiene ventas asociadas");
        }

        productoRepository.delete(producto);

        log.info("Producto eliminado: {} de tienda {} por {}",
                producto.getNombre(), tienda.getNombre(), emailSolicitante);
    }

    private TiendaEntity obtenerTiendaDelEmpleado(UsuarioEntity empleado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = empleado.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden gestionar productos");
        }

        return empleado.getEmpleadoTiendas().stream()
            .filter(et -> et.getActivo())
            .findFirst()
            .map(EmpleadoTiendaEntity::getTienda)
            .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));
    }
}
