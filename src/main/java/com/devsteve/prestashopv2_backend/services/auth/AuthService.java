package com.devsteve.prestashopv2_backend.services.auth;

import com.devsteve.prestashopv2_backend.models.dto.request.ChangePasswordRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.LoginRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RegistroUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.SolicitudTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AuthResponse;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.security.JwtService;
import com.devsteve.prestashopv2_backend.services.email.SolicitudTiendaEmailService;
import com.devsteve.prestashopv2_backend.utils.mappers.AuthMapper;
import com.devsteve.prestashopv2_backend.utils.mappers.SolicitudTiendaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TiendaRepository tiendaRepository;
    private final SolicitudTiendaRepository solicitudTiendaRepository;
    private final MunicipioRepository municipioRepository;
    private final EmpleadoTiendaRepository empleadoTiendaRepository;
    private final CuentaClienteRepository cuentaClienteRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final AuthMapper authMapper;
    private final SolicitudTiendaMapper solicitudTiendaMapper;

    private final SolicitudTiendaEmailService emailService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!usuario.getActivo()) {
                throw new RuntimeException("Usuario inactivo");
            }

            String token = jwtService.generateTokenForUser(usuario);

            return buildAuthResponse(usuario, token);

        } catch (Exception e) {
            log.error("Error en login para usuario: {}", request.getEmail(), e);
            throw new RuntimeException("Credenciales incorrectas");
        }
    }

    @Transactional
    public AuthResponse registrarUsuario(RegistroUsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con este email");
        }

        // Obtener el usuario autenticado (debe ser admin o encargado de tienda)
        String emailSolicitante = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity solicitante = usuarioRepository.findByEmailWithRolesAndTiendas(emailSolicitante)
            .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Validar permisos
        validarPermisosRegistro(solicitante, request.getTiendaId());

        // Obtener la tienda
        TiendaEntity tienda = tiendaRepository.findById(request.getTiendaId())
            .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        // Obtener el rol
        RolEntity rol = rolRepository.findByNombre(request.getRol())
            .orElseThrow(() -> new RuntimeException("Rol no válido"));

        // Crear usuario usando mapper
        UsuarioEntity nuevoUsuario = authMapper.toEntity(request);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.getRoles().add(rol);
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        // Crear relación empleado-tienda o cuenta cliente según el rol
        if ("EMPLEADO".equals(request.getRol()) || "ENCARGADO".equals(request.getRol())) {
            EmpleadoTiendaEntity empleado = EmpleadoTiendaEntity.builder()
                .usuario(nuevoUsuario)
                .tienda(tienda)
                .activo(true)
                .build();
            empleadoTiendaRepository.save(empleado);
        }

        if ("CLIENTE".equals(request.getRol())) {
            CuentaClienteEntity cuenta = CuentaClienteEntity.builder()
                .usuario(nuevoUsuario)
                .tienda(tienda)
                .activa(true)
                .build();
            cuentaClienteRepository.save(cuenta);
        }

        // Recargar usuario con relaciones
        nuevoUsuario = usuarioRepository.findByEmailWithRolesAndTiendas(nuevoUsuario.getEmail())
            .orElseThrow();

        // Generar token
        String token = jwtService.generateTokenForUser(nuevoUsuario);

        log.info("Usuario registrado exitosamente: {} en tienda: {}", request.getEmail(), tienda.getNombre());

        return buildAuthResponse(nuevoUsuario, token);
    }

    @Transactional
    public SolicitudTiendaResponse crearSolicitudTienda(SolicitudTiendaRequest request) {
        if (usuarioRepository.findByEmail(request.getEmailEncargado()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario registrado con este email");
        }

        if (solicitudTiendaRepository.existsByEmailEncargadoAndEstado(
                request.getEmailEncargado(), SolicitudTiendaEntity.EstadoSolicitud.PENDIENTE)) {
            throw new RuntimeException("Ya existe una solicitud pendiente con este email");
        }

        MunicipioEntity municipio = municipioRepository.findById(request.getMunicipioId())
            .orElseThrow(() -> new RuntimeException("Municipio no encontrado"));

        SolicitudTiendaEntity solicitud = solicitudTiendaMapper.toEntity(request);
        solicitud.setMunicipio(municipio);
        solicitud.setEstado(SolicitudTiendaEntity.EstadoSolicitud.PENDIENTE);
        solicitud = solicitudTiendaRepository.save(solicitud);

        enviarConfirmacionSolicitud(solicitud);

        log.info("Solicitud de tienda creada: {} para encargado: {}",
                request.getNombreTienda(), request.getEmailEncargado());

        return SolicitudTiendaResponse.builder()
            .id(solicitud.getId())
            .nombreTienda(solicitud.getNombreTienda())
            .nombreEncargado(solicitud.getNombreEncargado())
            .emailEncargado(solicitud.getEmailEncargado())
            .estado(solicitud.getEstado())
            .createdAt(solicitud.getCreatedAt())
            .build();
    }

    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        String token = jwtService.generateTokenForUser(usuario);

        log.info("Contraseña cambiada exitosamente para usuario: {}", email);

        return buildAuthResponse(usuario, token);
    }

    private AuthResponse buildAuthResponse(UsuarioEntity usuario, String token) {
        AuthResponse response = authMapper.toAuthResponse(usuario);
        response.setToken(token);
        return response;
    }

    private void validarPermisosRegistro(UsuarioEntity solicitante, Long tiendaId) {
        boolean esAdmin = solicitante.getRoles().stream()
            .anyMatch(rol -> "SYSADMIN".equals(rol.getNombre()));

        if (esAdmin) {
            return; // Los admins pueden registrar en cualquier tienda
        }

        // Si no es admin, debe ser encargado de la tienda específica
        boolean esEncargadoDeTienda = solicitante.getEmpleadoTiendas().stream()
            .anyMatch(et -> et.getTienda().getId().equals(tiendaId) &&
                           et.getActivo() &&
                           solicitante.getRoles().stream()
                               .anyMatch(rol -> "ENCARGADO".equals(rol.getNombre())));

        if (!esEncargadoDeTienda) {
            throw new RuntimeException("Sin permisos para registrar usuarios en esta tienda");
        }
    }

    private void enviarConfirmacionSolicitud(SolicitudTiendaEntity solicitud) {
        emailService.enviarConfirmacionSolicitud(solicitud);
    }
}
