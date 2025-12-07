package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCuentaClienteRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateEstadoCuentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateLimiteCreditoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CuentaClienteResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.CuentaClienteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaClienteService {

    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TiendaRepository tiendaRepository;
    private final CuentaClienteMapper cuentaClienteMapper;

    @Transactional(readOnly = true)
    public List<CuentaClienteResponse> listarCuentasDeTienda(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<CuentaClienteEntity> cuentas = cuentaClienteRepository.findByTiendaIdOrderByUsuarioNombreCompletoAsc(tiendaId);
        return cuentaClienteMapper.toResponseList(cuentas);
    }

    @Transactional(readOnly = true)
    public List<CuentaClienteResponse> listarCuentasActivasDeTienda(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<CuentaClienteEntity> cuentas = cuentaClienteRepository.findByTiendaIdAndActivaTrueOrderByUsuarioNombreCompletoAsc(tiendaId);
        return cuentaClienteMapper.toResponseList(cuentas);
    }

    @Transactional(readOnly = true)
    public CuentaClienteResponse obtenerCuentaPorId(Long cuentaId) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Validar que el usuario tenga acceso a esta cuenta
        validarAccesoACuenta(cuenta);

        return cuentaClienteMapper.toResponse(cuenta);
    }

    @Transactional
    public CuentaClienteResponse crearCuenta(CrearCuentaClienteRequest request) {
        // Obtener usuario autenticado (debe ser encargado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del encargado
        TiendaEntity tienda = obtenerTiendaDelEncargado(solicitante);

        // Verificar que el usuario cliente existe
        UsuarioEntity cliente = usuarioRepository.findById(request.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Verificar que el cliente no tenga ya una cuenta en esta tienda
        boolean yaExisteCuenta = cuentaClienteRepository.existsByUsuarioIdAndTiendaId(
            request.getUsuarioId(), tienda.getId());

        if (yaExisteCuenta) {
            throw new RuntimeException("El cliente ya tiene una cuenta en esta tienda");
        }

        // Crear la nueva cuenta
        CuentaClienteEntity nuevaCuenta = CuentaClienteEntity.builder()
            .usuario(cliente)
            .tienda(tienda)
            .limiteCredito(request.getLimiteCredito())
            .saldoActual(BigDecimal.ZERO)
            .activa(true)
            .build();

        nuevaCuenta = cuentaClienteRepository.save(nuevaCuenta);

        log.info("Cuenta creada para cliente {} en tienda {} por {}",
                cliente.getEmail(), tienda.getNombre(), emailSolicitante);

        return cuentaClienteMapper.toResponse(nuevaCuenta);
    }

    @Transactional
    public CuentaClienteResponse actualizarLimiteCredito(Long cuentaId, UpdateLimiteCreditoRequest request) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Validar acceso
        validarAccesoACuenta(cuenta);

        BigDecimal limiteAnterior = cuenta.getLimiteCredito();
        cuenta.setLimiteCredito(request.getNuevoLimite());
        cuenta = cuentaClienteRepository.save(cuenta);

        log.info("Límite de crédito actualizado de {} a {} para cuenta {}",
                limiteAnterior, request.getNuevoLimite(), cuentaId);

        return cuentaClienteMapper.toResponse(cuenta);
    }

    @Transactional
    public CuentaClienteResponse actualizarEstado(Long cuentaId, UpdateEstadoCuentaRequest request) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Validar acceso
        validarAccesoACuenta(cuenta);

        Boolean estadoAnterior = cuenta.getActiva();
        cuenta.setActiva(request.getActiva());
        cuenta = cuentaClienteRepository.save(cuenta);

        log.info("Estado de cuenta {} cambiado de {} a {}",
                cuentaId, estadoAnterior, request.getActiva());

        return cuentaClienteMapper.toResponse(cuenta);
    }

    @Transactional(readOnly = true)
    public boolean puedeRealizarCompra(Long cuentaId, BigDecimal montoCompra) {
        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Verificar que la cuenta esté activa
        if (!cuenta.getActiva()) {
            return false;
        }

        // Calcular saldo disponible: límite de crédito - saldo actual
        BigDecimal saldoDisponible = cuenta.getLimiteCredito().subtract(cuenta.getSaldoActual());

        // Verificar que el saldo disponible sea suficiente
        return saldoDisponible.compareTo(montoCompra) >= 0;
    }

    @Transactional
    public CuentaClienteResponse cargarSaldo(Long cuentaId, BigDecimal monto, String concepto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a cargar debe ser mayor que cero");
        }

        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Validar acceso
        validarAccesoACuenta(cuenta);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoNuevo = saldoAnterior.add(monto);

        cuenta.setSaldoActual(saldoNuevo);
        cuenta = cuentaClienteRepository.save(cuenta);

        log.info("Saldo cargado: {} a cuenta {}. Saldo anterior: {}, nuevo: {}",
                monto, cuentaId, saldoAnterior, saldoNuevo);

        return cuentaClienteMapper.toResponse(cuenta);
    }

    @Transactional
    public CuentaClienteResponse abonarSaldo(Long cuentaId, BigDecimal monto, String concepto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a abonar debe ser mayor que cero");
        }

        CuentaClienteEntity cuenta = buscarEntidadPorId(cuentaId);

        // Validar acceso
        validarAccesoACuenta(cuenta);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoNuevo = saldoAnterior.subtract(monto);

        // Verificar que no quede saldo negativo
        if (saldoNuevo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El abono no puede ser mayor que el saldo actual");
        }

        cuenta.setSaldoActual(saldoNuevo);
        cuenta = cuentaClienteRepository.save(cuenta);

        log.info("Saldo abonado: {} a cuenta {}. Saldo anterior: {}, nuevo: {}",
                monto, cuentaId, saldoAnterior, saldoNuevo);

        return cuentaClienteMapper.toResponse(cuenta);
    }

    @Transactional(readOnly = true)
    public List<CuentaClienteResponse> obtenerMisCuentas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<CuentaClienteEntity> cuentas = cuentaClienteRepository.findByUsuarioIdOrderByTiendaNombreAsc(usuario.getId());
        return cuentaClienteMapper.toResponseList(cuentas);
    }

    private CuentaClienteEntity buscarEntidadPorId(Long cuentaId) {
        return cuentaClienteRepository.findById(cuentaId)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
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

    private void validarAccesoACuenta(CuentaClienteEntity cuenta) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        boolean esPropietario = cuenta.getUsuario().getId().equals(usuario.getId());

        boolean esEmpleadoTienda = usuario.getEmpleadoTiendas().stream()
            .anyMatch(et -> et.getTienda().getId().equals(cuenta.getTienda().getId()) && et.getActivo());

        if (!esAdmin && !esPropietario && !esEmpleadoTienda) {
            throw new RuntimeException("Sin permisos para acceder a esta cuenta");
        }
    }

    private TiendaEntity obtenerTiendaDelEncargado(UsuarioEntity encargado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = encargado.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden crear cuentas");
        }

        return encargado.getEmpleadoTiendas().stream()
            .filter(et -> et.getActivo())
            .findFirst()
            .map(EmpleadoTiendaEntity::getTienda)
            .orElseThrow(() -> new RuntimeException("No tienes una tienda asignada"));
    }
}
