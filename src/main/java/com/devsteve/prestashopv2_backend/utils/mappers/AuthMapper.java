package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.RegistroUsuarioRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AuthResponse;
import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import com.devsteve.prestashopv2_backend.models.entities.EmpleadoTiendaEntity;
import com.devsteve.prestashopv2_backend.models.entities.RolEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Se encripta por separado
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "empleadoTiendas", ignore = true)
    @Mapping(target = "cuentasCliente", ignore = true)
    @Mapping(target = "movimientosComoOperador", ignore = true)
    @Mapping(target = "movimientosComoCliente", ignore = true)
    @Mapping(target = "notificaciones", ignore = true)
    UsuarioEntity toEntity(RegistroUsuarioRequest request);

    @Mapping(target = "token", ignore = true) // Se asigna por separado
    @Mapping(target = "tipo", constant = "Bearer")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    @Mapping(target = "tiendas", source = "empleadoTiendas", qualifiedByName = "mapTiendas")
    AuthResponse toAuthResponse(UsuarioEntity usuario);

    @Named("mapRoles")
    default List<String> mapRoles(java.util.Set<RolEntity> roles) {
        return roles.stream()
                .map(RolEntity::getNombre)
                .collect(Collectors.toList());
    }

    @Named("mapTiendas")
    default List<AuthResponse.TiendaBasicaDto> mapTiendas(java.util.Set<EmpleadoTiendaEntity> empleadoTiendas) {
        return empleadoTiendas.stream()
                .filter(EmpleadoTiendaEntity::getActivo)
                .map(et -> AuthResponse.TiendaBasicaDto.builder()
                        .id(et.getTienda().getId())
                        .nombre(et.getTienda().getNombre())
                        .logoUrl(et.getTienda().getLogoUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
