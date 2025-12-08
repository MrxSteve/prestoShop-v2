package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.AbonoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.AbonoResponse;
import com.devsteve.prestashopv2_backend.models.entities.AbonoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AbonoMapper {

    @Mapping(target = "tiendaId", source = "tienda.id")
    @Mapping(target = "tiendaNombre", source = "tienda.nombre")
    @Mapping(target = "cuentaClienteId", source = "cuentaCliente.id")
    @Mapping(target = "clienteNombreCompleto", source = "cuentaCliente.usuario.nombreCompleto")
    @Mapping(target = "clienteEmail", source = "cuentaCliente.usuario.email")
    AbonoResponse toResponse(AbonoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "fechaAbono", ignore = true) // Se establece automáticamente
    AbonoEntity toEntity(AbonoRequest request);

    List<AbonoResponse> toResponseList(List<AbonoEntity> entities);
}
