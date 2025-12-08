package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateProductoRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.ProductoResponse;
import com.devsteve.prestashopv2_backend.models.entities.ProductoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductoMapper {

    @Mapping(target = "tienda.id", source = "tienda.id")
    @Mapping(target = "tienda.nombre", source = "tienda.nombre")
    @Mapping(target = "categoria.id", source = "categoria.id")
    @Mapping(target = "categoria.nombre", source = "categoria.nombre")
    ProductoResponse toResponse(ProductoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "detallesVenta", ignore = true)
    ProductoEntity toEntity(CrearProductoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "detallesVenta", ignore = true)
    void updateEntityFromRequest(UpdateProductoRequest request, @MappingTarget ProductoEntity entity);

    List<ProductoResponse> toResponseList(List<ProductoEntity> entities);
}
