package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.MovimientoTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.MovimientoTiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.MovimientoTiendaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovimientoTiendaMapper {

    @Mapping(target = "tiendaId", source = "tienda.id")
    @Mapping(target = "tiendaNombre", source = "tienda.nombre")
    @Mapping(target = "usuarioOperadorId", source = "usuarioOperador.id")
    @Mapping(target = "usuarioOperadorNombre", source = "usuarioOperador.nombreCompleto")
    @Mapping(target = "usuarioOperadorEmail", source = "usuarioOperador.email")
    @Mapping(target = "clienteUsuarioId", source = "clienteUsuario.id")
    @Mapping(target = "clienteUsuarioNombre", source = "clienteUsuario.nombreCompleto")
    @Mapping(target = "clienteUsuarioEmail", source = "clienteUsuario.email")
    MovimientoTiendaResponse toResponse(MovimientoTiendaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "usuarioOperador", ignore = true)
    @Mapping(target = "clienteUsuario", ignore = true)
    @Mapping(target = "fechaEvento", ignore = true) // Se establece automáticamente
    MovimientoTiendaEntity toEntity(MovimientoTiendaRequest request);

    List<MovimientoTiendaResponse> toResponseList(List<MovimientoTiendaEntity> entities);
}
