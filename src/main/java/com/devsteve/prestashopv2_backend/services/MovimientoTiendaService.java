package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.MovimientoTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.MovimientoTiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.MovimientoTiendaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoTiendaService {

    private final MovimientoTiendaRepository movimientoTiendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoTiendaMapper movimientoTiendaMapper;

    @Transactional
    public MovimientoTiendaResponse crear(MovimientoTiendaRequest request) {
        // Obtener usuario autenticado (encargado o empleado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity operador = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(operador);

        // Crear el movimiento
        MovimientoTiendaEntity movimiento = movimientoTiendaMapper.toEntity(request);
        movimiento.setTienda(tienda);
        movimiento.setUsuarioOperador(operador);
        movimiento.setFechaEvento(LocalDateTime.now());

        // Si se especifica un cliente, verificar que exista
        if (request.getClienteUsuarioId() != null) {
            UsuarioEntity cliente = usuarioRepository.findById(request.getClienteUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            movimiento.setClienteUsuario(cliente);
        }

        MovimientoTiendaEntity movimientoGuardado = movimientoTiendaRepository.save(movimiento);

        log.info("Movimiento #{} creado por {} en tienda {} - Evento: {}",
                movimientoGuardado.getId(), emailSolicitante, tienda.getNombre(), request.getTipoEvento());

        return movimientoTiendaMapper.toResponse(movimientoGuardado);
    }

    @Transactional(readOnly = true)
    public Optional<MovimientoTiendaResponse> buscarPorId(Long id) {
        MovimientoTiendaEntity movimiento = buscarEntidadPorId(id);

        // Validar acceso al movimiento
        validarAccesoAMovimiento(movimiento);

        return Optional.of(movimientoTiendaMapper.toResponse(movimiento));
    }

    @Transactional(readOnly = true)
    public Page<MovimientoTiendaResponse> listarMovimientosDeTienda(Long tiendaId, Pageable pageable) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        Page<MovimientoTiendaEntity> movimientos = movimientoTiendaRepository.findByTiendaId(tiendaId, pageable);
        return movimientos.map(movimientoTiendaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoTiendaResponse> listarMovimientosPorTipo(Long tiendaId, TipoEvento tipoEvento, Pageable pageable) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        Page<MovimientoTiendaEntity> movimientos = movimientoTiendaRepository.findByTiendaIdAndTipoEvento(tiendaId, tipoEvento, pageable);
        return movimientos.map(movimientoTiendaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoTiendaResponse> listarMovimientosPorFecha(Long tiendaId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        Page<MovimientoTiendaEntity> movimientos = movimientoTiendaRepository.findByTiendaIdAndFechaEventoBetween(tiendaId, fechaInicio, fechaFin, pageable);
        return movimientos.map(movimientoTiendaMapper::toResponse);
    }

    // MÉTODOS DE ESTADÍSTICAS

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalVentasDelDia(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(23, 59, 59);

        return movimientoTiendaRepository.findVentasDelDia(tiendaId, inicioDelDia, finDelDia)
                .stream()
                .map(MovimientoTiendaEntity::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAbonosDelDia(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(23, 59, 59);

        return movimientoTiendaRepository.findAbonosDelDia(tiendaId, inicioDelDia, finDelDia)
                .stream()
                .map(MovimientoTiendaEntity::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalVentasDelMes(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioDelMes = hoy.withDayOfMonth(1);
        LocalDate finDelMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        LocalDateTime inicioDelMesDateTime = inicioDelMes.atStartOfDay();
        LocalDateTime finDelMesDateTime = finDelMes.atTime(23, 59, 59);

        return movimientoTiendaRepository.findVentasDelMes(tiendaId, inicioDelMesDateTime, finDelMesDateTime)
                .stream()
                .map(MovimientoTiendaEntity::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalAbonosDelMes(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioDelMes = hoy.withDayOfMonth(1);
        LocalDate finDelMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        LocalDateTime inicioDelMesDateTime = inicioDelMes.atStartOfDay();
        LocalDateTime finDelMesDateTime = finDelMes.atTime(23, 59, 59);

        return movimientoTiendaRepository.findAbonosDelMes(tiendaId, inicioDelMesDateTime, finDelMesDateTime)
                .stream()
                .map(MovimientoTiendaEntity::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // MÉTODO PARA REGISTRAR EVENTOS AUTOMÁTICAMENTE

    @Transactional
    public void registrarEvento(TipoEvento tipoEvento, String descripcion, BigDecimal monto, Long clienteId, Long referenciaId, String referenciaTabla) {
        try {
            String emailOperador = SecurityContextHolder.getContext().getAuthentication().getName();
            UsuarioEntity operador = usuarioRepository.findByEmailWithRolesAndTiendas(emailOperador)
                    .orElse(null);

            if (operador == null) {
                return; // No registrar si no hay usuario autenticado
            }

            TiendaEntity tienda = obtenerTiendaDelEmpleado(operador);

            UsuarioEntity cliente = null;
            if (clienteId != null) {
                cliente = usuarioRepository.findById(clienteId).orElse(null);
            }

            MovimientoTiendaEntity movimiento = MovimientoTiendaEntity.builder()
                    .tienda(tienda)
                    .usuarioOperador(operador)
                    .clienteUsuario(cliente)
                    .tipoEvento(tipoEvento)
                    .descripcion(descripcion)
                    .monto(monto)
                    .referenciaId(referenciaId)
                    .referenciaTabla(referenciaTabla)
                    .fechaEvento(LocalDateTime.now())
                    .build();

            movimientoTiendaRepository.save(movimiento);

            log.info("Evento automático registrado: {} en tienda {} por operador {}",
                    tipoEvento, tienda.getNombre(), emailOperador);
        } catch (Exception e) {
            log.warn("Error al registrar evento automático: {}", e.getMessage());
        }
    }

    // MÉTODOS AUXILIARES

    private MovimientoTiendaEntity buscarEntidadPorId(Long id) {
        return movimientoTiendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + id));
    }

    private TiendaEntity obtenerTiendaDelEmpleado(UsuarioEntity empleado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = empleado.getRoles().stream()
                .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden gestionar movimientos");
        }

        return empleado.getEmpleadoTiendas().stream()
                .filter(et -> et.getActivo())
                .findFirst()
                .map(EmpleadoTiendaEntity::getTienda)
                .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));
    }

    private void validarAccesoATienda(Long tiendaId) {
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = solicitante.getRoles().stream()
                .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return;
        }

        boolean tieneAcceso = solicitante.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(tiendaId) && et.getActivo());

        if (!tieneAcceso) {
            throw new RuntimeException("Sin acceso a esta tienda");
        }
    }

    private void validarAccesoAMovimiento(MovimientoTiendaEntity movimiento) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return;
        }

        boolean esEmpleadoTienda = usuario.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(movimiento.getTienda().getId()) && et.getActivo());

        if (!esEmpleadoTienda) {
            throw new RuntimeException("No tienes permisos para acceder a este movimiento");
        }
    }
}
