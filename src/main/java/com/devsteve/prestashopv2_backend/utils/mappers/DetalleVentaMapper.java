package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.DetalleVentaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.DetalleVentaResponse;
import com.devsteve.prestashopv2_backend.models.entities.DetalleVentaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleVentaMapper {

    @Mapping(target = "producto.id", source = "producto.id")
    @Mapping(target = "producto.nombre", source = "producto.nombre")
    @Mapping(target = "producto.descripcion", source = "producto.descripcion")
    @Mapping(target = "producto.imagenUrl", source = "producto.imagenUrl")
    DetalleVentaResponse toResponse(DetalleVentaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true) // Se establece desde el producto
    @Mapping(target = "subtotal", ignore = true) // Se calcula automáticamente
    DetalleVentaEntity toEntity(DetalleVentaRequest request);

    List<DetalleVentaResponse> toResponseList(List<DetalleVentaEntity> entities);
}
