package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.AbonoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AbonoResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.services.email.AbonoEmailService;
import com.devsteve.prestashopv2_backend.utils.mappers.AbonoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaClienteService cuentaClienteService;
    private final AbonoMapper abonoMapper;
    private final AbonoEmailService abonoEmailService;
    private final MovimientoTiendaService movimientoTiendaService;

    @Transactional
    public AbonoResponse crear(AbonoRequest request) {
        // Obtener usuario autenticado (encargado o empleado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Verificar que la cuenta del cliente existe y pertenece a la tienda
        CuentaClienteEntity cuentaCliente = cuentaClienteRepository.findById(request.getCuentaClienteId())
                .orElseThrow(() -> new RuntimeException("Cuenta de cliente no encontrada"));

        if (!cuentaCliente.getTienda().getId().equals(tienda.getId())) {
            throw new RuntimeException("La cuenta de cliente no pertenece a esta tienda");
        }

        // Crear el abono
        AbonoEntity abono = abonoMapper.toEntity(request);
        abono.setCuentaCliente(cuentaCliente);
        abono.setTienda(tienda);
        abono.setFechaAbono(LocalDateTime.now());

        AbonoEntity abonoGuardado = abonoRepository.save(abono);

        // Si el abono está en estado APLICADO, aplicarlo automáticamente a la cuenta
        if (abonoGuardado.getEstado() == EstadoAbono.APLICADO) {
            cuentaClienteService.abonarSaldo(cuentaCliente.getId(), abono.getMonto(),
                    "Abono #" + abonoGuardado.getId());

            // Enviar comprobante por correo
            abonoEmailService.enviarComprobanteAbono(abonoGuardado);
        }

        // REGISTRAR MOVIMIENTO AUTOMÁTICAMENTE
        movimientoTiendaService.registrarEvento(
                TipoEvento.ABONO_REGISTRADO,
                "Abono registrado - " + abono.getMetodoPago() + " por $" + abono.getMonto(),
                abono.getMonto(),
                cuentaCliente.getUsuario().getId(),
                abonoGuardado.getId(),
                "abonos"
        );

        log.info("Abono #{} creado por {} para cliente {} por monto ${}",
                abonoGuardado.getId(), emailSolicitante, cuentaCliente.getUsuario().getEmail(), abono.getMonto());

        return abonoMapper.toResponse(abonoGuardado);
    }

    @Transactional(readOnly = true)
    public Optional<AbonoResponse> buscarPorId(Long id) {
        AbonoEntity abono = buscarEntidadPorId(id);

        // Validar acceso al abono
        validarAccesoAAbono(abono);

        return Optional.of(abonoMapper.toResponse(abono));
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarAbonosDeTienda(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<AbonoEntity> abonos = abonoRepository.findByTiendaIdOrderByFechaAbonoDesc(tiendaId);
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarAbonosPorEstado(Long tiendaId, EstadoAbono estado) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<AbonoEntity> abonos = abonoRepository.findByTiendaIdAndEstado(tiendaId, estado, null).getContent();
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarAbonosPorCliente(Long cuentaClienteId) {
        // Verificar que la cuenta existe y validar acceso
        CuentaClienteEntity cuenta = cuentaClienteRepository.findById(cuentaClienteId)
                .orElseThrow(() -> new RuntimeException("Cuenta de cliente no encontrada"));

        validarAccesoACuenta(cuenta);

        List<AbonoEntity> abonos = abonoRepository.findByCuentaClienteIdOrderByFechaAbonoDesc(cuentaClienteId);
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarMisAbonos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<AbonoEntity> abonos = abonoRepository.findByCuentaClienteUsuarioIdOrderByFechaAbonoDesc(usuario.getId());
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarMisAbonosPorEstado(EstadoAbono estado) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<AbonoEntity> abonos = abonoRepository.findByCuentaClienteUsuarioIdAndEstadoOrderByFechaAbonoDesc(usuario.getId(), estado);
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public List<AbonoResponse> listarMisAbonosPorTienda(Long tiendaId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<AbonoEntity> abonos = abonoRepository.findByCuentaClienteUsuarioIdAndTiendaIdOrderByFechaAbonoDesc(usuario.getId(), tiendaId);
        return abonoMapper.toResponseList(abonos);
    }

    @Transactional(readOnly = true)
    public Optional<AbonoResponse> obtenerMiAbono(Long abonoId) {
        AbonoEntity abono = buscarEntidadPorId(abonoId);

        // Validar que el abono pertenece al usuario autenticado
        validarPropietarioAbono(abono);

        return Optional.of(abonoMapper.toResponse(abono));
    }

    @Transactional
    public AbonoResponse cambiarEstado(Long id, EstadoAbono nuevoEstado) {
        AbonoEntity abono = buscarEntidadPorId(id);

        // Validar acceso al abono
        validarAccesoAAbono(abono);

        EstadoAbono estadoAnterior = abono.getEstado();

        // Validar transiciones de estado válidas
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        abono.setEstado(nuevoEstado);
        AbonoEntity abonoActualizado = abonoRepository.save(abono);

        // Si se cambia a APLICADO, aplicar el abono
        if (nuevoEstado == EstadoAbono.APLICADO && estadoAnterior != EstadoAbono.APLICADO) {
            cuentaClienteService.abonarSaldo(abono.getCuentaCliente().getId(), abono.getMonto(),
                    "Aplicación abono #" + abono.getId());

            // Enviar comprobante por correo
            abonoEmailService.enviarComprobanteAbono(abonoActualizado);
        }

        log.info("Estado de abono #{} cambiado de {} a {} por {}",
                id, estadoAnterior, nuevoEstado, SecurityContextHolder.getContext().getAuthentication().getName());

        return abonoMapper.toResponse(abonoActualizado);
    }

    @Transactional
    public AbonoResponse aplicar(Long id) {
        return cambiarEstado(id, EstadoAbono.APLICADO);
    }

    @Transactional
    public AbonoResponse marcarComoPendiente(Long id) {
        return cambiarEstado(id, EstadoAbono.PENDIENTE);
    }

    @Transactional
    public AbonoResponse rechazar(Long id) {
        return cambiarEstado(id, EstadoAbono.RECHAZADO);
    }


    // MÉTODOS AUXILIARES

    private AbonoEntity buscarEntidadPorId(Long id) {
        return abonoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abono no encontrado con ID: " + id));
    }

    private TiendaEntity obtenerTiendaDelEmpleado(UsuarioEntity empleado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = empleado.getRoles().stream()
                .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden crear abonos");
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

    private void validarAccesoAAbono(AbonoEntity abono) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return;
        }

        boolean esPropietario = abono.getCuentaCliente() != null &&
                abono.getCuentaCliente().getUsuario().getId().equals(usuario.getId());

        boolean esEmpleadoTienda = usuario.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(abono.getTienda().getId()) && et.getActivo());

        if (!esPropietario && !esEmpleadoTienda) {
            throw new RuntimeException("No tienes permisos para acceder a este abono");
        }
    }

    private void validarPropietarioAbono(AbonoEntity abono) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esPropietario = abono.getCuentaCliente() != null &&
                abono.getCuentaCliente().getUsuario().getId().equals(usuario.getId());

        if (!esPropietario) {
            throw new RuntimeException("No tienes permisos para ver este abono");
        }
    }

    private void validarTransicionEstado(EstadoAbono estadoActual, EstadoAbono nuevoEstado) {
        // Definir transiciones válidas
        boolean transicionValida = switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoAbono.APLICADO || nuevoEstado == EstadoAbono.RECHAZADO;
            case APLICADO -> false; // Los abonos aplicados no se pueden cambiar
            case RECHAZADO -> nuevoEstado == EstadoAbono.PENDIENTE; // Se puede reactivar un abono rechazado
        };

        if (!transicionValida) {
            throw new RuntimeException(String.format("No se puede cambiar el estado de %s a %s",
                    estadoActual, nuevoEstado));
        }
    }
}
