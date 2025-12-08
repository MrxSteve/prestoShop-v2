package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CategoriaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategoriasDeTienda(Long tiendaId) {
        List<CategoriaEntity> categorias = categoriaRepository.findByTiendaIdOrderByNombreAsc(tiendaId);
        return categoriaMapper.toResponseList(categorias);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerCategoriaPorId(Long categoriaId) {
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return categoriaMapper.toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> buscarCategoriasPorNombre(Long tiendaId, String nombre) {
        List<CategoriaEntity> categorias = categoriaRepository
            .findByTiendaIdAndNombreContainingIgnoreCaseOrderByNombreAsc(tiendaId, nombre);
        return categoriaMapper.toResponseList(categorias);
    }

    @Transactional
    public CategoriaResponse crearCategoria(CrearCategoriaRequest request) {
        // Obtener usuario autenticado (debe ser encargado o empleado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Verificar que no exista una categoría con el mismo nombre en la tienda
        boolean yaExisteCategoria = categoriaRepository.existsByNombreAndTiendaId(
            request.getNombre(), tienda.getId());

        if (yaExisteCategoria) {
            throw new RuntimeException("Ya existe una categoría con este nombre en la tienda");
        }

        // Crear la nueva categoría
        CategoriaEntity nuevaCategoria = categoriaMapper.toEntity(request);
        nuevaCategoria.setTienda(tienda);
        nuevaCategoria = categoriaRepository.save(nuevaCategoria);

        log.info("Categoría creada: {} en tienda {} por {}",
                request.getNombre(), tienda.getNombre(), emailSolicitante);

        return categoriaMapper.toResponse(nuevaCategoria);
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(Long categoriaId, UpdateCategoriaRequest request) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Buscar la categoría en la tienda
        CategoriaEntity categoria = categoriaRepository.findByIdAndTiendaId(categoriaId, tienda.getId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada en tu tienda"));

        // Verificar que no exista otra categoría con el mismo nombre
        boolean yaExisteOtraCategoria = categoriaRepository.existsByNombreAndTiendaIdAndIdNot(
            request.getNombre(), tienda.getId(), categoriaId);

        if (yaExisteOtraCategoria) {
            throw new RuntimeException("Ya existe otra categoría con este nombre en la tienda");
        }

        // Actualizar la categoría
        categoriaMapper.updateEntityFromRequest(request, categoria);
        categoria = categoriaRepository.save(categoria);

        log.info("Categoría actualizada: {} en tienda {} por {}",
                request.getNombre(), tienda.getNombre(), emailSolicitante);

        return categoriaMapper.toResponse(categoria);
    }

    @Transactional
    public void eliminarCategoria(Long categoriaId) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Buscar la categoría en la tienda
        CategoriaEntity categoria = categoriaRepository.findByIdAndTiendaId(categoriaId, tienda.getId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada en tu tienda"));

        // Verificar que no tenga productos asociados
        if (!categoria.getProductos().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);

        log.info("Categoría eliminada: {} de tienda {} por {}",
                categoria.getNombre(), tienda.getNombre(), emailSolicitante);
    }

    private TiendaEntity obtenerTiendaDelEmpleado(UsuarioEntity empleado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = empleado.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden gestionar categorías");
        }

        return empleado.getEmpleadoTiendas().stream()
            .filter(et -> et.getActivo())
            .findFirst()
            .map(EmpleadoTiendaEntity::getTienda)
            .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));
    }
}
