package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.VentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.VentaResponse;
import com.devsteve.prestashopv2_backend.models.entities.VentaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = DetalleVentaMapper.class)
public interface VentaMapper {

    @Mapping(target = "tienda.id", source = "tienda.id")
    @Mapping(target = "tienda.nombre", source = "tienda.nombre")
    @Mapping(target = "tienda.telefono", source = "tienda.telefono")
    @Mapping(target = "cliente.id", source = "cuentaCliente.usuario.id")
    @Mapping(target = "cliente.nombreCompleto", source = "cuentaCliente.usuario.nombreCompleto")
    @Mapping(target = "cliente.email", source = "cuentaCliente.usuario.email")
    @Mapping(target = "cliente.telefono", source = "cuentaCliente.usuario.telefono")
    @Mapping(target = "detalles", source = "detalleVentas")
    VentaResponse toResponse(VentaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "cuentaCliente", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true) // Se establece automáticamente
    @Mapping(target = "subtotal", ignore = true) // Se calcula automáticamente
    @Mapping(target = "total", ignore = true) // Se calcula automáticamente
    @Mapping(target = "estado", ignore = true) // Se establece según el tipo de venta
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    VentaEntity toEntity(VentaRequest request);

    List<VentaResponse> toResponseList(List<VentaEntity> entities);
}
