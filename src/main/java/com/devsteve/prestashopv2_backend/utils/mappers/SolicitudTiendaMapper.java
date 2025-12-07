package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.SolicitudTiendaRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.SolicitudTiendaResponse;
import com.devsteve.prestashopv2_backend.models.entities.SolicitudTiendaEntity;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SolicitudTiendaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "motivoRechazo", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "fechaAprobacion", ignore = true)
    @Mapping(target = "aprobadoPor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "municipio", ignore = true)
    SolicitudTiendaEntity toEntity(SolicitudTiendaRequest request);

    @Mapping(target = "municipio", source = "municipio.nombre")
    @Mapping(target = "departamento", source = "municipio.departamento.nombre")
    SolicitudTiendaResponse toResponse(SolicitudTiendaEntity entity);

//    @Mapping(target = "municipio", ignore = true)
//    @Mapping(target = "departamento", ignore = true)
    SolicitudTiendaResponse.Basica toBasicaResponse(SolicitudTiendaEntity entity);

}
