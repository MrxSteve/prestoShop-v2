package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.TiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.TiendaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TiendaService {

    private final TiendaRepository tiendaRepository;
    private final MunicipioRepository municipioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TiendaMapper tiendaMapper;

    @Transactional(readOnly = true)
    public List<TiendaResponse> listarTiendasActivas() {
        List<TiendaEntity> tiendas = tiendaRepository.findByActivoTrueOrderByNombre();
        return tiendaMapper.toResponseList(tiendas);
    }

    @Transactional(readOnly = true)
    public List<TiendaResponse> listarTiendasPorMunicipio(Integer municipioId) {
        List<TiendaEntity> tiendas = tiendaRepository.findByMunicipioIdAndActivoTrueOrderByNombre(municipioId);
        return tiendaMapper.toResponseList(tiendas);
    }

    @Transactional(readOnly = true)
    public List<TiendaResponse> listarTiendasPorDepartamento(Integer departamentoId) {
        List<TiendaEntity> tiendas = tiendaRepository.findByMunicipioDepartamentoIdAndActivoTrueOrderByNombre(departamentoId);
        return tiendaMapper.toResponseList(tiendas);
    }

    @Transactional(readOnly = true)
    public TiendaResponse obtenerTienda(Long tiendaId) {
        TiendaEntity tienda = tiendaRepository.findByIdAndActivoTrue(tiendaId)
            .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));
        return tiendaMapper.toResponse(tienda);
    }

    @Transactional
    public TiendaResponse crearTienda(CrearTiendaRequest request) {
        // Validar que solo admin puede crear tiendas
        validarEsSysAdmin();

        // Verificar que el municipio existe
        MunicipioEntity municipio = municipioRepository.findById(request.getMunicipioId())
            .orElseThrow(() -> new RuntimeException("Municipio no encontrado"));

        // Crear la tienda
        TiendaEntity nuevaTienda = tiendaMapper.toEntity(request);
        nuevaTienda.setMunicipio(municipio);
        nuevaTienda.setActivo(true);
        nuevaTienda = tiendaRepository.save(nuevaTienda);

        log.info("Tienda creada: {} en municipio: {}", request.getNombre(), municipio.getNombre());

        return tiendaMapper.toResponse(nuevaTienda);
    }

    @Transactional
    public TiendaResponse actualizarMiTienda(UpdateTiendaRequest request) {
        // Obtener usuario autenticado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = usuario.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden actualizar información de tienda");
        }

        // Obtener la tienda del usuario (primera tienda activa donde trabaja)
        TiendaEntity miTienda = usuario.getEmpleadoTiendas().stream()
            .filter(et -> et.getActivo())
            .findFirst()
            .map(et -> et.getTienda())
            .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));

        // Validar municipio si se está cambiando
        if (request.getMunicipioId() != null && !request.getMunicipioId().equals(miTienda.getMunicipio().getId())) {
            MunicipioEntity nuevoMunicipio = municipioRepository.findById(request.getMunicipioId())
                .orElseThrow(() -> new RuntimeException("Municipio no encontrado"));
            miTienda.setMunicipio(nuevoMunicipio);
        }

        // Actualizar usando mapper
        tiendaMapper.updateEntityFromRequest(request, miTienda);
        miTienda = tiendaRepository.save(miTienda);

        log.info("Tienda actualizada por {}: {}", email, miTienda.getNombre());

        return tiendaMapper.toResponse(miTienda);
    }

    @Transactional(readOnly = true)
    public TiendaResponse obtenerMiTienda() {
        // Obtener usuario autenticado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del usuario
        TiendaEntity miTienda = usuario.getEmpleadoTiendas().stream()
            .filter(et -> et.getActivo())
            .findFirst()
            .map(et -> et.getTienda())
            .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));

        return tiendaMapper.toResponse(miTienda);
    }

    private void validarEsSysAdmin() {
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (!esAdmin) {
            throw new RuntimeException("Solo administradores pueden realizar esta acción");
        }
    }
}
