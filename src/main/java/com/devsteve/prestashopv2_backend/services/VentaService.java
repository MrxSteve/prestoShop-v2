package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.request.DetalleVentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.VentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.VentaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import com.devsteve.prestashopv2_backend.models.enums.TipoVenta;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.services.email.VentaEmailService;
import com.devsteve.prestashopv2_backend.utils.mappers.DetalleVentaMapper;
import com.devsteve.prestashopv2_backend.utils.mappers.VentaMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CuentaClienteService cuentaClienteService;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final VentaEmailService ventaEmailService;
    private final MovimientoTiendaService movimientoTiendaService;

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        // Obtener usuario autenticado (encargado o empleado)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Obtener la tienda del empleado
        TiendaEntity tienda = obtenerTiendaDelEmpleado(solicitante);

        // Primero calcular totales basado en los productos y cantidades
        BigDecimal totalCalculado = calcularTotalVenta(request.getDetalleVentas(), tienda.getId());

        // Determinar el tipo de venta y procesar según corresponda
        if (request.getTipoVenta() == TipoVenta.CREDITO) {
            return procesarVentaCredito(request, totalCalculado, tienda);
        } else {
            return procesarVentaContado(request, totalCalculado, tienda);
        }
    }

    private VentaResponse procesarVentaCredito(VentaRequest request, BigDecimal totalCalculado, TiendaEntity tienda) {
        // Validar que se especifique una cuenta de cliente
        if (request.getCuentaClienteId() == null) {
            throw new RuntimeException("Para ventas a crédito se debe especificar una cuenta de cliente");
        }

        // Verificar que la cuenta existe y puede realizar la compra
        CuentaClienteEntity cuenta = cuentaClienteRepository.findById(request.getCuentaClienteId())
                .orElseThrow(() -> new RuntimeException("Cuenta de cliente no encontrada"));

        // Verificar que la cuenta pertenece a la tienda
        if (!cuenta.getTienda().getId().equals(tienda.getId())) {
            throw new RuntimeException("La cuenta de cliente no pertenece a esta tienda");
        }

        // Verificar que la cuenta puede realizar la compra
        if (!cuentaClienteService.puedeRealizarCompra(cuenta.getId(), totalCalculado)) {
            BigDecimal saldoDisponible = cuenta.getLimiteCredito().subtract(cuenta.getSaldoActual());
            throw new RuntimeException("La cuenta no tiene crédito suficiente para realizar esta compra. " +
                    "Saldo disponible: $" + saldoDisponible);
        }

        // Crear la venta
        VentaEntity venta = crearVentaBase(request, cuenta, null, totalCalculado, tienda);
        venta.setEstado(EstadoVenta.PENDIENTE); // Las ventas a crédito inician como PENDIENTE

        VentaEntity ventaGuardada = ventaRepository.save(venta);

        // Procesar detalles de venta
        procesarDetallesVenta(ventaGuardada, request.getDetalleVentas());

        // REGISTRAR MOVIMIENTO AUTOMÁTICAMENTE
        movimientoTiendaService.registrarEvento(
                TipoEvento.VENTA_REGISTRADA,
                "Venta registrada - " + venta.getTipoVenta() + " por $" + venta.getTotal(),
                venta.getTotal(),
                cuenta != null ? cuenta.getUsuario().getId() : null,
                ventaGuardada.getId(),
                "ventas"
        );

        // FACTURA POR CORREO (VENTA A CRÉDITO)
        ventaEmailService.enviarFacturaVenta(ventaGuardada);


        return ventaMapper.toResponse(ventaGuardada);
    }

    private VentaResponse procesarVentaContado(VentaRequest request, BigDecimal totalCalculado, TiendaEntity tienda) {
        CuentaClienteEntity cuenta = null;
        String clienteOcasional = null;

        // Puede ser cliente con cuenta o cliente ocasional
        if (request.getCuentaClienteId() != null) {
            cuenta = cuentaClienteRepository.findById(request.getCuentaClienteId())
                    .orElseThrow(() -> new RuntimeException("Cuenta de cliente no encontrada"));

            // Verificar que la cuenta pertenece a la tienda
            if (!cuenta.getTienda().getId().equals(tienda.getId())) {
                throw new RuntimeException("La cuenta de cliente no pertenece a esta tienda");
            }
        } else {
            // Validar que se especifique el nombre del cliente ocasional
            if (request.getClienteOcasional() == null || request.getClienteOcasional().trim().isEmpty()) {
                throw new RuntimeException("Para ventas al contado sin cuenta se debe especificar el nombre del cliente");
            }
            clienteOcasional = request.getClienteOcasional();
        }

        // Crear la venta
        VentaEntity venta = crearVentaBase(request, cuenta, clienteOcasional, totalCalculado, tienda);
        venta.setEstado(EstadoVenta.PAGADA); // Las ventas al contado se marcan como PAGADA inmediatamente

        VentaEntity ventaGuardada = ventaRepository.save(venta);

        // Procesar detalles de venta
        procesarDetallesVenta(ventaGuardada, request.getDetalleVentas());

        // REGISTRAR MOVIMIENTO AUTOMÁTICAMENTE
        movimientoTiendaService.registrarEvento(
                TipoEvento.VENTA_REGISTRADA,
                "Venta registrada - " + venta.getTipoVenta() + " por $" + venta.getTotal(),
                venta.getTotal(),
                cuenta != null ? cuenta.getUsuario().getId() : null,
                ventaGuardada.getId(),
                "ventas"
        );

        // FACTURA POR CORREO (VENTA DE CONTADO - SOLO SI TIENE CUENTA)
        if (cuenta != null) { // Solo enviar correo si el cliente tiene cuenta registrada
            ventaEmailService.enviarFacturaVenta(ventaGuardada);
        }


        return ventaMapper.toResponse(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public Optional<VentaResponse> buscarPorId(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);
        return Optional.of(ventaMapper.toResponse(venta));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentasDeTienda(Long tiendaId) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<VentaEntity> ventas = ventaRepository.findByTiendaIdOrderByFechaVentaDesc(tiendaId);
        return ventaMapper.toResponseList(ventas);
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentasPorEstado(Long tiendaId, EstadoVenta estado) {
        // Validar acceso a la tienda
        validarAccesoATienda(tiendaId);

        List<VentaEntity> ventas = ventaRepository.findByTiendaIdAndEstadoOrderByFechaVentaDesc(tiendaId, estado);
        return ventaMapper.toResponseList(ventas);
    }

    @Transactional
    public VentaResponse cancelarVenta(Long id) {
        VentaEntity venta = buscarEntidadPorId(id);

        // Validar acceso a la tienda
        validarAccesoATienda(venta.getTienda().getId());

        // Solo se pueden cancelar ventas pendientes
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new RuntimeException("Solo se pueden cancelar ventas en estado PENDIENTE");
        }

        // Si es venta a crédito, revertir el cargo
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCuentaCliente() != null) {
            cuentaClienteService.abonarSaldo(venta.getCuentaCliente().getId(),
                    venta.getTotal(), "Cancelación venta #" + venta.getId());
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        VentaEntity ventaActualizada = ventaRepository.save(venta);

        log.info("Venta #{} cancelada por {}",
                id, SecurityContextHolder.getContext().getAuthentication().getName());

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional(readOnly = true)
    public VentaEntity buscarEntidadPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    // MÉTODOS AUXILIARES

    private VentaEntity crearVentaBase(VentaRequest request, CuentaClienteEntity cuenta, String clienteOcasional,
                                      BigDecimal totalCalculado, TiendaEntity tienda) {
        VentaEntity venta = ventaMapper.toEntity(request);
        venta.setTienda(tienda);
        venta.setCuentaCliente(cuenta);
        venta.setClienteOcasional(clienteOcasional);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setTotal(totalCalculado);
        venta.setSubtotal(totalCalculado);

        return venta;
    }

    private void procesarDetallesVenta(VentaEntity venta, List<DetalleVentaRequest> detallesRequest) {
        List<DetalleVentaEntity> detalles = new ArrayList<>();
        BigDecimal subtotalVenta = BigDecimal.ZERO;

        for (DetalleVentaRequest detalleRequest : detallesRequest) {
            // Verificar que el producto existe
            ProductoEntity producto = productoRepository.findById(detalleRequest.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleRequest.getProductoId()));

            DetalleVentaEntity detalle = detalleVentaMapper.toEntity(detalleRequest);
            detalle.setVenta(venta);
            detalle.setProducto(producto);

            // Establecer el precio unitario del producto automáticamente
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            detalle.setCantidad(detalleRequest.getCantidad());

            // Calcular subtotal automáticamente
            BigDecimal subtotalDetalle = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
            detalle.setSubtotal(subtotalDetalle);

            subtotalVenta = subtotalVenta.add(subtotalDetalle);
            detalles.add(detalle);
        }

        // Guardar todos los detalles en la base de datos para que obtengan sus IDs
        List<DetalleVentaEntity> detallesGuardados = new ArrayList<>();
        for (DetalleVentaEntity detalle : detalles) {
            DetalleVentaEntity detalleGuardado = detalleVentaRepository.save(detalle);
            detallesGuardados.add(detalleGuardado);
        }

        // Actualizar la venta con los detalles guardados (que ya tienen IDs)
        venta.setDetalleVentas(detallesGuardados);
        venta.setSubtotal(subtotalVenta);

        // Guardar la venta actualizada con los detalles
        ventaRepository.save(venta);

        // Si es venta a crédito, cargar el saldo a la cuenta
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCuentaCliente() != null) {
            cuentaClienteService.cargarSaldo(venta.getCuentaCliente().getId(),
                    venta.getTotal(), "Compra venta #" + venta.getId());
        }
    }

    private BigDecimal calcularTotalVenta(List<DetalleVentaRequest> detallesRequest, Long tiendaId) {
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequest detalleRequest : detallesRequest) {
            // Obtener el producto para extraer su precio y verificar que pertenece a la tienda
            ProductoEntity producto = productoRepository.findByIdAndTiendaId(detalleRequest.getProductoId(), tiendaId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta tienda con ID: " + detalleRequest.getProductoId()));

            // Verificar que el producto esté activo
            if (!producto.getActivo()) {
                throw new RuntimeException("El producto " + producto.getNombre() + " no está disponible");
            }

            // Calcular subtotal usando el precio del producto
            BigDecimal subtotal = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
            total = total.add(subtotal);
        }

        return total;
    }

    private TiendaEntity obtenerTiendaDelEmpleado(UsuarioEntity empleado) {
        // Validar que sea encargado o empleado
        boolean esEncargadoOEmpleado = empleado.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()) || "EMPLEADO".equals(r.getNombre()));

        if (!esEncargadoOEmpleado) {
            throw new RuntimeException("Solo encargados y empleados pueden realizar ventas");
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

    // NUEVOS MÉTODOS PARA CLIENTES

    @Transactional(readOnly = true)
    public List<VentaResponse> listarMisCompras() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<VentaEntity> ventas = ventaRepository.findByCuentaClienteUsuarioIdOrderByFechaVentaDesc(usuario.getId());
        return ventaMapper.toResponseList(ventas);
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarMisComprasPorEstado(EstadoVenta estado) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<VentaEntity> ventas = ventaRepository.findByCuentaClienteUsuarioIdAndEstadoOrderByFechaVentaDesc(usuario.getId(), estado);
        return ventaMapper.toResponseList(ventas);
    }

    @Transactional(readOnly = true)
    public Optional<VentaResponse> obtenerDetalleCompra(Long ventaId) {
        VentaEntity venta = buscarEntidadPorId(ventaId);
        validarPropietarioVenta(venta);
        return Optional.of(ventaMapper.toResponse(venta));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarMisComprasPorTienda(Long tiendaId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<VentaEntity> ventas = ventaRepository.findByCuentaClienteUsuarioIdAndTiendaIdOrderByFechaVentaDesc(usuario.getId(), tiendaId);
        return ventaMapper.toResponseList(ventas);
    }

    @Transactional
    public VentaResponse marcarComoPagada(Long ventaId) {
        VentaEntity venta = buscarEntidadPorId(ventaId);
        validarAccesoPagoVenta(venta);

        if (venta.getEstado() != EstadoVenta.PENDIENTE && venta.getEstado() != EstadoVenta.PARCIAL) {
            throw new RuntimeException("Solo se pueden marcar como pagadas las ventas en estado PENDIENTE o PARCIAL");
        }

        // Si es venta a crédito, abonar el saldo de la cuenta
        if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCuentaCliente() != null) {
            cuentaClienteService.abonarSaldo(venta.getCuentaCliente().getId(),
                    venta.getTotal(), "Pago venta #" + venta.getId());
        }

        venta.setEstado(EstadoVenta.PAGADA);
        VentaEntity ventaActualizada = ventaRepository.save(venta);

        log.info("Venta #{} marcada como PAGADA por {}", ventaId, SecurityContextHolder.getContext().getAuthentication().getName());

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional
    public List<VentaResponse> marcarTodasMisVentasComoPagadas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<VentaEntity> ventasPendientes = ventaRepository.findByCuentaClienteUsuarioIdAndEstadoInOrderByFechaVentaDesc(
                usuario.getId(), List.of(EstadoVenta.PENDIENTE, EstadoVenta.PARCIAL));

        if (ventasPendientes.isEmpty()) {
            return List.of();
        }

        List<VentaEntity> ventasActualizadas = new ArrayList<>();
        BigDecimal totalAbonado = BigDecimal.ZERO;

        for (VentaEntity venta : ventasPendientes) {
            if (venta.getTipoVenta() == TipoVenta.CREDITO && venta.getCuentaCliente() != null) {
                totalAbonado = totalAbonado.add(venta.getTotal());
            }

            venta.setEstado(EstadoVenta.PAGADA);
            VentaEntity ventaActualizada = ventaRepository.save(venta);
            ventasActualizadas.add(ventaActualizada);
        }

        // Si hay ventas a crédito, hacer un solo abono por el total
        if (totalAbonado.compareTo(BigDecimal.ZERO) > 0 && !ventasPendientes.isEmpty()) {
            VentaEntity primeraVenta = ventasPendientes.get(0);
            if (primeraVenta.getCuentaCliente() != null) {
                cuentaClienteService.abonarSaldo(primeraVenta.getCuentaCliente().getId(),
                        totalAbonado, "Pago masivo de " + ventasPendientes.size() + " ventas");
            }
        }

        log.info("Usuario {} marcó {} ventas como PAGADAS por un total de ${}", email, ventasActualizadas.size(), totalAbonado);

        return ventaMapper.toResponseList(ventasActualizadas);
    }

    @Transactional
    public List<VentaResponse> marcarTodasVentasClienteComoPagadas(Long clienteId) {
        // Validar que el empleado tenga acceso (debe validar que es de la misma tienda)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity empleado = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Buscar todas las ventas pendientes y parciales del cliente
        List<VentaEntity> ventasPendientes = ventaRepository.findByCuentaClienteUsuarioIdAndEstadoInOrderByFechaVentaDesc(
                clienteId, List.of(EstadoVenta.PENDIENTE, EstadoVenta.PARCIAL));

        if (ventasPendientes.isEmpty()) {
            return List.of();
        }

        // Validar que el empleado tenga acceso a las tiendas donde están las ventas
        TiendaEntity tiendaEmpleado = obtenerTiendaDelEmpleado(empleado);
        boolean todasVentasSonDeLaTienda = ventasPendientes.stream()
                .allMatch(v -> v.getTienda().getId().equals(tiendaEmpleado.getId()));

        if (!todasVentasSonDeLaTienda) {
            throw new RuntimeException("No puedes marcar como pagadas ventas de otras tiendas");
        }

        List<VentaEntity> ventasActualizadas = new ArrayList<>();

        // Solo marcar como pagadas, sin hacer abonos a la cuenta
        for (VentaEntity venta : ventasPendientes) {
            venta.setEstado(EstadoVenta.PAGADA);
            VentaEntity ventaActualizada = ventaRepository.save(venta);
            ventasActualizadas.add(ventaActualizada);
        }

        log.info("Empleado {} marcó {} ventas del cliente {} como PAGADAS (sin abonos automáticos)",
                email, ventasActualizadas.size(), clienteId);

        return ventaMapper.toResponseList(ventasActualizadas);
    }

    // MÉTODOS AUXILIARES PARA NUEVAS FUNCIONALIDADES

    private void validarPropietarioVenta(VentaEntity venta) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return;
        }

        boolean esPropietario = venta.getCuentaCliente() != null &&
                venta.getCuentaCliente().getUsuario().getId().equals(usuario.getId());

        boolean esEmpleadoTienda = usuario.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(venta.getTienda().getId()) && et.getActivo());

        if (!esPropietario && !esEmpleadoTienda) {
            throw new RuntimeException("No tienes permisos para acceder a esta venta");
        }
    }

    private void validarAccesoPagoVenta(VentaEntity venta) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return;
        }

        boolean esPropietario = venta.getCuentaCliente() != null &&
                venta.getCuentaCliente().getUsuario().getId().equals(usuario.getId());

        boolean esEmpleadoTienda = usuario.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(venta.getTienda().getId()) && et.getActivo());

        if (!esPropietario && !esEmpleadoTienda) {
            throw new RuntimeException("No tienes permisos para marcar esta venta como pagada");
        }
    }
}
