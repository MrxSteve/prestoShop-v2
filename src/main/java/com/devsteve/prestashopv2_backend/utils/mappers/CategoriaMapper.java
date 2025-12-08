package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.request.update.UpdateCategoriaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CategoriaResponse;
import com.devsteve.prestashopv2_backend.models.entities.CategoriaEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoriaMapper {

    @Mapping(target = "tienda.id", source = "tienda.id")
    @Mapping(target = "tienda.nombre", source = "tienda.nombre")
    CategoriaResponse toResponse(CategoriaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "productos", ignore = true)
    CategoriaEntity toEntity(CrearCategoriaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "productos", ignore = true)
    void updateEntityFromRequest(UpdateCategoriaRequest request, @MappingTarget CategoriaEntity entity);

    List<CategoriaResponse> toResponseList(List<CategoriaEntity> entities);
}
