package com.devsteve.prestashopv2_backend.services.auth;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.UsuarioResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.utils.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TiendaRepository tiendaRepository;
    private final EmpleadoTiendaRepository empleadoTiendaRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarClientesDeTienda(Long tiendaId) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Validar que sea encargado de la tienda o sysadmin
        validarAccesoATienda(solicitante, tiendaId);

        List<UsuarioEntity> clientes = usuarioRepository.findClientesByTiendaId(tiendaId);
        return usuarioMapper.toResponseList(clientes);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarEmpleadosDeTienda(Long tiendaId) {
        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Validar que sea encargado/empleado de la tienda o sysadmin
        validarAccesoATienda(solicitante, tiendaId);

        List<UsuarioEntity> empleados = usuarioRepository.findEmpleadosByTiendaId(tiendaId);
        return usuarioMapper.toResponseList(empleados);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarEmpleados() {
        // Solo sysadmin puede ver todos los empleados
        validarEsSysAdmin();

        List<UsuarioEntity> empleados = usuarioRepository.findEmpleadosAndEncargados();
        return usuarioMapper.toResponseList(empleados);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findByIdWithRolesAndTiendas(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar permisos para ver el usuario
        validarPermisosLectura(usuario);

        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {
        // Validar que el email no esté en uso
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con este email");
        }

        // Obtener usuario autenticado
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Validar permisos
        validarPermisosCreacion(solicitante, request.getTiendaId(), request.getRol());

        // Obtener tienda y rol
        TiendaEntity tienda = tiendaRepository.findById(request.getTiendaId())
            .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        RolEntity rol = rolRepository.findByNombre(request.getRol())
            .orElseThrow(() -> new RuntimeException("Rol no válido"));

        // Crear usuario
        UsuarioEntity nuevoUsuario = usuarioMapper.toEntity(request);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.getRoles().add(rol);
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        // Crear relación con tienda
        crearRelacionConTiendaUsuario(nuevoUsuario, tienda, request.getRol());

        // Recargar usuario con relaciones
        nuevoUsuario = usuarioRepository.findByIdWithRolesAndTiendas(nuevoUsuario.getId())
            .orElseThrow(() -> new RuntimeException("Error al recargar usuario"));

        log.info("Usuario creado: {} con rol {} en tienda: {}",
                request.getEmail(), request.getRol(), tienda.getNombre());

        return usuarioMapper.toResponse(nuevoUsuario);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long usuarioId, UpdateUsuarioRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByIdWithRolesAndTiendas(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar permisos de actualización
        validarPermisosActualizacion(usuario);

        // Validar email único si se está cambiando
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Ya existe un usuario con este email");
            }
        }

        usuarioMapper.updateEntityFromRequest(request, usuario);
        usuario = usuarioRepository.save(usuario);

        log.info("Usuario actualizado: {}", usuario.getEmail());

        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public void activarUsuario(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar permisos
        validarPermisosActivacion(usuario);

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        log.info("Usuario activado: {}", usuario.getEmail());
    }

    @Transactional
    public void activarEmpleadoEnTienda(Long usuarioId, Long tiendaId) {
        // Validar acceso a la tienda
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        validarAccesoATienda(solicitante, tiendaId);

        // Encontrar la relación empleado-tienda
        EmpleadoTiendaEntity empleadoTienda = empleadoTiendaRepository
            .findByUsuarioIdAndTiendaId(usuarioId, tiendaId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado en esta tienda"));

        empleadoTienda.setActivo(true);
        empleadoTiendaRepository.save(empleadoTienda);

        log.info("Empleado activado en tienda: usuarioId={}, tiendaId={}", usuarioId, tiendaId);
    }

    @Transactional
    public void desactivarEmpleadoEnTienda(Long usuarioId, Long tiendaId) {
        // Validar acceso a la tienda
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        validarAccesoATienda(solicitante, tiendaId);

        // Encontrar la relación empleado-tienda
        EmpleadoTiendaEntity empleadoTienda = empleadoTiendaRepository
            .findByUsuarioIdAndTiendaId(usuarioId, tiendaId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado en esta tienda"));

        empleadoTienda.setActivo(false);
        empleadoTiendaRepository.save(empleadoTienda);

        log.info("Empleado desactivado en tienda: usuarioId={}, tiendaId={}", usuarioId, tiendaId);
    }

    @Transactional
    public void activarClienteEnTienda(Long usuarioId, Long tiendaId) {
        // Validar acceso a la tienda
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        validarAccesoATienda(solicitante, tiendaId);

        // Encontrar la cuenta cliente
        CuentaClienteEntity cuentaCliente = cuentaClienteRepository
            .findByUsuarioIdAndTiendaId(usuarioId, tiendaId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado en esta tienda"));

        cuentaCliente.setActiva(true);
        cuentaClienteRepository.save(cuentaCliente);

        log.info("Cliente activado en tienda: usuarioId={}, tiendaId={}", usuarioId, tiendaId);
    }

    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar permisos
        validarPermisosActualizacion(usuario);

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        log.info("Usuario desactivado: {}", usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse.TiendaBasicResponse> obtenerMisTiendas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si es cliente, mostrar tiendas donde tiene cuenta de cliente
        List<UsuarioResponse.TiendaBasicResponse> tiendas = new ArrayList<>();

        // Tiendas como cliente
        usuario.getCuentasCliente().forEach(cc -> {
            tiendas.add(UsuarioResponse.TiendaBasicResponse.builder()
                    .id(cc.getTienda().getId())
                    .nombre(cc.getTienda().getNombre())
                    .activo(cc.getActiva())
                    .build());
        });

        // Tiendas como empleado/encargado
        usuario.getEmpleadoTiendas().forEach(et -> {
            // Evitar duplicados si es empleado y cliente de la misma tienda
            boolean yaExiste = tiendas.stream()
                    .anyMatch(t -> t.getId().equals(et.getTienda().getId()));

            if (!yaExiste) {
                tiendas.add(UsuarioResponse.TiendaBasicResponse.builder()
                        .id(et.getTienda().getId())
                        .nombre(et.getTienda().getNombre())
                        .activo(et.getActivo())
                        .build());
            }
        });

        return tiendas;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse.TiendaBasicResponse> listarTiendasDisponibles() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener IDs de tiendas donde ya está registrado
        List<Long> tiendasRegistrado = usuario.getCuentasCliente().stream()
            .map(cc -> cc.getTienda().getId())
            .collect(java.util.stream.Collectors.toList());

        // Obtener todas las tiendas activas
        List<TiendaEntity> todasLasTiendas = tiendaRepository.findByActivoTrue();

        // Filtrar las tiendas donde NO está registrado
        return todasLasTiendas.stream()
            .filter(tienda -> !tiendasRegistrado.contains(tienda.getId()))
            .map(tienda -> UsuarioResponse.TiendaBasicResponse.builder()
                    .id(tienda.getId())
                    .nombre(tienda.getNombre())
                    .activo(true)
                    .build())
            .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UsuarioResponse registrarClienteExistentePorEmail(Long tiendaId, String emailCliente) {
        // Obtener encargado autenticado
        String emailEncargado = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity encargado = usuarioRepository.findByEmailWithRolesAndTiendas(emailEncargado)
            .orElseThrow(() -> new RuntimeException("Encargado autenticado no encontrado"));

        // Validar que sea encargado de la tienda específica
        validarAccesoATienda(encargado, tiendaId);

        // Buscar el cliente por email
        UsuarioEntity cliente = usuarioRepository.findByEmailWithRolesAndTiendas(emailCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + emailCliente));

        // Verificar que la tienda existe
        TiendaEntity tienda = tiendaRepository.findById(tiendaId)
            .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        // Verificar que el cliente no esté ya registrado en esta tienda
        boolean yaRegistrado = cliente.getCuentasCliente().stream()
            .anyMatch(cc -> cc.getTienda().getId().equals(tiendaId));

        if (yaRegistrado) {
            throw new RuntimeException("El cliente ya está registrado en esta tienda");
        }

        // Crear cuenta de cliente en la nueva tienda
        CuentaClienteEntity nuevaCuenta = CuentaClienteEntity.builder()
            .usuario(cliente)
            .tienda(tienda)
            .activa(true)
            .build();
        cuentaClienteRepository.save(nuevaCuenta);

        // Recargar cliente con relaciones actualizadas
        cliente = usuarioRepository.findByEmailWithRolesAndTiendas(emailCliente)
            .orElseThrow(() -> new RuntimeException("Error al recargar cliente"));

        log.info("Encargado {} registró cliente {} en tienda: {}",
                emailEncargado, emailCliente, tienda.getNombre());

        return usuarioMapper.toResponse(cliente);
    }

    private void validarPermisosCreacion(UsuarioEntity solicitante, Long tiendaId, String rol) {
        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        boolean esEncargado = solicitante.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre()));

        // SYSADMIN puede crear cualquier tipo de usuario en cualquier tienda
        if (esAdmin) {
            return;
        }

        // ENCARGADO solo puede crear CLIENTES y EMPLEADOS en SU tienda
        if (esEncargado) {
            // Verificar que el rol solicitado sea válido para encargados
            if ("SYSADMIN".equals(rol) || "ENCARGADO".equals(rol)) {
                throw new RuntimeException("Los encargados no pueden crear administradores ni otros encargados");
            }

            // Verificar que sea encargado de la tienda específica
            boolean esEncargadoDeTienda = solicitante.getEmpleadoTiendas().stream()
                .anyMatch(et -> et.getTienda().getId().equals(tiendaId) && et.getActivo());

            if (!esEncargadoDeTienda) {
                throw new RuntimeException("Solo puedes crear usuarios en las tiendas que administras");
            }

            // Permitir solo CLIENTE y EMPLEADO
            if (!"CLIENTE".equals(rol) && !"EMPLEADO".equals(rol)) {
                throw new RuntimeException("Solo puedes crear clientes o empleados");
            }

            return;
        }

        // Si no es SYSADMIN ni ENCARGADO, no tiene permisos
        throw new RuntimeException("Sin permisos para crear usuarios");
    }

    private void validarPermisosActualizacion(UsuarioEntity usuario) {
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        boolean esMismoUsuario = solicitante.getId().equals(usuario.getId());

        if (!esAdmin && !esMismoUsuario) {
            // Verificar si es encargado y el usuario es cliente de su tienda
            boolean puedeActualizar = solicitante.getRoles().stream()
                .anyMatch(r -> "ENCARGADO".equals(r.getNombre())) &&
                usuario.getCuentasCliente().stream()
                    .anyMatch(cc -> cc.getActiva() &&
                                  solicitante.getEmpleadoTiendas().stream()
                                      .anyMatch(et -> et.getTienda().getId().equals(cc.getTienda().getId()) && et.getActivo()));

            if (!puedeActualizar) {
                throw new RuntimeException("Sin permisos para actualizar este usuario");
            }
        }
    }

    private void validarPermisosLectura(UsuarioEntity usuario) {
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        boolean esMismoUsuario = solicitante.getId().equals(usuario.getId());

        if (esAdmin || esMismoUsuario) {
            return; // Admin o mismo usuario puede ver
        }

        // Verificar si es empleado/encargado y puede ver al usuario
        boolean puedeVer = solicitante.getEmpleadoTiendas().stream()
            .anyMatch(et -> et.getActivo() &&
                          usuario.getCuentasCliente().stream()
                              .anyMatch(cc -> cc.getActiva() && cc.getTienda().getId().equals(et.getTienda().getId())));

        if (!puedeVer) {
            throw new RuntimeException("Sin permisos para ver este usuario");
        }
    }

    private void validarAccesoATienda(UsuarioEntity solicitante, Long tiendaId) {
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

    private void validarPermisosActivacion(UsuarioEntity usuario) {
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(r -> "SYSADMIN".equals(r.getNombre()));

        if (esAdmin) {
            return; // Admin puede activar cualquier usuario
        }

        // Verificar si es encargado y el usuario es de su tienda
        boolean puedeActivar = solicitante.getRoles().stream()
            .anyMatch(r -> "ENCARGADO".equals(r.getNombre())) &&
            (usuario.getCuentasCliente().stream()
                .anyMatch(cc -> solicitante.getEmpleadoTiendas().stream()
                    .anyMatch(et -> et.getTienda().getId().equals(cc.getTienda().getId()) && et.getActivo())) ||
            usuario.getEmpleadoTiendas().stream()
                .anyMatch(et -> solicitante.getEmpleadoTiendas().stream()
                    .anyMatch(etSol -> etSol.getTienda().getId().equals(et.getTienda().getId()) && etSol.getActivo())));

        if (!puedeActivar) {
            throw new RuntimeException("Sin permisos para activar este usuario");
        }
    }

    private void crearRelacionConTiendaUsuario(UsuarioEntity usuario, TiendaEntity tienda, String rol) {
        if ("EMPLEADO".equals(rol) || "ENCARGADO".equals(rol)) {
            EmpleadoTiendaEntity empleado = EmpleadoTiendaEntity.builder()
                .usuario(usuario)
                .tienda(tienda)
                .activo(true)
                .build();
            empleadoTiendaRepository.save(empleado);
        } else if ("CLIENTE".equals(rol)) {
            CuentaClienteEntity cuenta = CuentaClienteEntity.builder()
                .usuario(usuario)
                .tienda(tienda)
                .activa(true)
                .build();
            cuentaClienteRepository.save(cuenta);
        }
    }
}
