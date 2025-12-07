package com.devsteve.prestashopv2_backend.services.auth;

import com.devsteve.prestashopv2_backend.models.dto.request.AprobarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.RechazarSolicitudRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.*;
import com.devsteve.prestashopv2_backend.repositories.*;
import com.devsteve.prestashopv2_backend.services.email.SolicitudTiendaEmailService;
import com.devsteve.prestashopv2_backend.utils.mappers.SolicitudTiendaMapper;
import com.devsteve.prestashopv2_backend.utils.mappers.TiendaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSolicitudesService {
    private final SolicitudTiendaRepository solicitudTiendaRepository;
    private final TiendaRepository tiendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpleadoTiendaRepository empleadoTiendaRepository;
    private final PasswordEncoder passwordEncoder;

    private final SolicitudTiendaMapper solicitudTiendaMapper;
    private final TiendaMapper tiendaMapper;

    private final SolicitudTiendaEmailService emailService;

    public List<SolicitudTiendaResponse.Basica> obtenerSolicitudesPendientes() {
        return solicitudTiendaRepository
            .findByEstadoOrderByCreatedAtDesc(SolicitudTiendaEntity.EstadoSolicitud.PENDIENTE)
            .stream()
            .map(solicitudTiendaMapper::toBasicaResponse)
            .collect(Collectors.toList());
    }

    public SolicitudTiendaResponse obtenerSolicitudDetalle(Long solicitudId) {
        SolicitudTiendaEntity solicitud = solicitudTiendaRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        return solicitudTiendaMapper.toResponse(solicitud);
    }

    @Transactional
    public SolicitudTiendaResponse aprobarSolicitud(Long solicitudId, AprobarSolicitudRequest request, String adminEmail) {
        SolicitudTiendaEntity solicitud = solicitudTiendaRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudTiendaEntity.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        if (usuarioRepository.findByEmail(solicitud.getEmailEncargado()).isPresent()) {
            throw new RuntimeException("El email del encargado ya está registrado en el sistema");
        }

        try {
            // Forzar carga de relaciones lazy ANTES de continuar
            String municipioNombre = solicitud.getMunicipio().getNombre();
            String departamentoNombre = solicitud.getMunicipio().getDepartamento().getNombre();

            // 1. Crear la tienda usando mapper
            TiendaEntity nuevaTienda = tiendaMapper.fromSolicitudToTienda(solicitud);
            nuevaTienda = tiendaRepository.save(nuevaTienda);

            // 2. Crear usuario encargado usando mapper
            String passwordTemporal = generarPasswordTemporal();

            RolEntity rolEncargado = rolRepository.findByNombre("ENCARGADO")
                .orElseThrow(() -> new RuntimeException("Rol ENCARGADO no encontrado"));

            UsuarioEntity nuevoEncargado = tiendaMapper.fromSolicitudToEncargado(solicitud);
            nuevoEncargado.setPassword(passwordEncoder.encode(passwordTemporal));

            // Guardar usuario primero
            nuevoEncargado = usuarioRepository.save(nuevoEncargado);

            // Agregar rol después de que el usuario esté persistido
            nuevoEncargado.getRoles().add(rolEncargado);
            nuevoEncargado = usuarioRepository.save(nuevoEncargado);

            // 3. Crear relación empleado-tienda
            EmpleadoTiendaEntity empleado = EmpleadoTiendaEntity.builder()
                .usuario(nuevoEncargado)
                .tienda(nuevaTienda)
                .activo(true)
                .build();
            empleadoTiendaRepository.save(empleado);

            // 4. Actualizar solicitud
            solicitud.setEstado(SolicitudTiendaEntity.EstadoSolicitud.APROBADA);
            solicitud.setTienda(nuevaTienda);
            solicitud.setFechaAprobacion(LocalDateTime.now());
            solicitud.setAprobadoPor(adminEmail);
            solicitud = solicitudTiendaRepository.save(solicitud);

            // 5. Preparar respuesta ANTES del email (dentro de la transacción)
            SolicitudTiendaResponse response = SolicitudTiendaResponse.builder()
                .id(solicitud.getId())
                .nombreTienda(solicitud.getNombreTienda())
                .telefono(solicitud.getTelefono())
                .municipio(municipioNombre)
                .departamento(departamentoNombre)
                .direccionExacta(solicitud.getDireccionExacta())
                .nombreEncargado(solicitud.getNombreEncargado())
                .emailEncargado(solicitud.getEmailEncargado())
                .duiEncargado(solicitud.getDuiEncargado())
                .estado(solicitud.getEstado())
                .descripcion(solicitud.getDescripcion())
                .motivoRechazo(solicitud.getMotivoRechazo())
                .createdAt(solicitud.getCreatedAt())
                .fechaAprobacion(solicitud.getFechaAprobacion())
                .aprobadoPor(solicitud.getAprobadoPor())
                .build();

            // 6. Enviar email con credenciales DESPUÉS de construir la respuesta
            enviarCredencialesEncargado(nuevoEncargado, passwordTemporal, nuevaTienda, request);

            log.info("Solicitud aprobada exitosamente. Tienda: {} - Encargado: {}",
                    nuevaTienda.getNombre(), nuevoEncargado.getEmail());

            return response;

        } catch (Exception e) {
            log.error("Error al aprobar solicitud {}", solicitudId, e);
            throw new RuntimeException("Error al procesar la aprobación: " + e.getMessage());
        }
    }

    @Transactional
    public SolicitudTiendaResponse rechazarSolicitud(Long solicitudId, RechazarSolicitudRequest request, String adminEmail) {
        SolicitudTiendaEntity solicitud = solicitudTiendaRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudTiendaEntity.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        solicitud.setEstado(SolicitudTiendaEntity.EstadoSolicitud.RECHAZADA);
        solicitud.setMotivoRechazo(request.getMotivo());
        solicitud.setAprobadoPor(adminEmail);
        solicitud = solicitudTiendaRepository.save(solicitud);

        // Enviar email de rechazo
        enviarNotificacionRechazo(solicitud, request);

        log.info("Solicitud rechazada. ID: {} - Motivo: {}", solicitudId, request.getMotivo());

        return solicitudTiendaMapper.toResponse(solicitud);
    }

    private String generarPasswordTemporal() {
        return "TMP_" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void enviarCredencialesEncargado(UsuarioEntity encargado, String passwordTemporal,
                                           TiendaEntity tienda, AprobarSolicitudRequest request) {
        String municipioNombre = tienda.getMunicipio().getNombre();
        String departamentoNombre = tienda.getMunicipio().getDepartamento().getNombre();

        emailService.enviarCredencialesEncargadoConDatos(
            encargado.getNombreCompleto(),
            encargado.getEmail(),
            passwordTemporal,
            tienda.getNombre(),
            tienda.getTelefono(),
            tienda.getDireccionExacta(),
            municipioNombre,
            departamentoNombre,
            request
        );
    }

    private void enviarNotificacionRechazo(SolicitudTiendaEntity solicitud, RechazarSolicitudRequest request) {
        emailService.enviarNotificacionRechazo(solicitud, request);
    }
}
