package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.TiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.SolicitudTiendaEntity;
import com.devsteve.prestashopv2_backend.models.entities.TiendaEntity;
import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TiendaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "nombreTienda")
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "direccionExacta", source = "direccionExacta")
    @Mapping(target = "municipio", source = "municipio")
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "empleados", ignore = true)
    @Mapping(target = "categorias", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "cuentasCliente", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    @Mapping(target = "abonos", ignore = true)
    @Mapping(target = "movimientos", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    TiendaEntity fromSolicitudToTienda(SolicitudTiendaEntity solicitud);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombreCompleto", source = "nombreEncargado")
    @Mapping(target = "email", source = "emailEncargado")
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "dui", source = "duiEncargado")
    @Mapping(target = "password", ignore = true) // Se asigna por separado
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "empleadoTiendas", ignore = true)
    @Mapping(target = "cuentasCliente", ignore = true)
    @Mapping(target = "movimientosComoOperador", ignore = true)
    @Mapping(target = "movimientosComoCliente", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    UsuarioEntity fromSolicitudToEncargado(SolicitudTiendaEntity solicitud);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "municipio", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TiendaEntity toEntity(CrearTiendaRequest request);

    @Mapping(target = "municipio.departamento.id", source = "municipio.departamento.id")
    @Mapping(target = "municipio.departamento.nombre", source = "municipio.departamento.nombre")
    TiendaResponse toResponse(TiendaEntity tienda);

    void updateEntityFromRequest(UpdateTiendaRequest request, @MappingTarget TiendaEntity tienda);

    List<TiendaResponse> toResponseList(List<TiendaEntity> tiendas);
}
