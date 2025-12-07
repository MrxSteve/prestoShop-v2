package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.UsuarioResponse;
import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "empleadoTiendas", ignore = true)
    @Mapping(target = "cuentasCliente", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UsuarioEntity toEntity(CrearUsuarioRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(usuario))")
    @Mapping(target = "tiendas", expression = "java(mapTiendas(usuario))")
    UsuarioResponse toResponse(UsuarioEntity usuario);

    void updateEntityFromRequest(UpdateUsuarioRequest request, @MappingTarget UsuarioEntity usuario);

    List<UsuarioResponse> toResponseList(List<UsuarioEntity> usuarios);

    default List<String> mapRoles(UsuarioEntity usuario) {
        return usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList());
    }

    default List<UsuarioResponse.TiendaBasicResponse> mapTiendas(UsuarioEntity usuario) {
        return usuario.getEmpleadoTiendas().stream()
                .map(et -> UsuarioResponse.TiendaBasicResponse.builder()
                        .id(et.getTienda().getId())
                        .nombre(et.getTienda().getNombre())
                        .activo(et.getActivo())
                        .build())
                .collect(Collectors.toList());
    }
}
