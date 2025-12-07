package com.devsteve.prestashopv2_backend.utils.mappers;

import com.devsteve.prestashopv2_backend.models.dto.request.CrearCuentaClienteRequest;
import com.devsteve.prestashopv2_backend.models.dto.response.CuentaClienteResponse;
import com.devsteve.prestashopv2_backend.models.entities.CuentaClienteEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CuentaClienteMapper {

    @Mapping(target = "cliente.id", source = "usuario.id")
    @Mapping(target = "cliente.nombreCompleto", source = "usuario.nombreCompleto")
    @Mapping(target = "cliente.email", source = "usuario.email")
    @Mapping(target = "cliente.telefono", source = "usuario.telefono")
    @Mapping(target = "cliente.activo", source = "usuario.activo")
    @Mapping(target = "tienda.id", source = "tienda.id")
    @Mapping(target = "tienda.nombre", source = "tienda.nombre")
    @Mapping(target = "tienda.telefono", source = "tienda.telefono")
    @Mapping(target = "tienda.activo", source = "tienda.activo")
    @Mapping(target = "saldoDisponible", expression = "java(calcularSaldoDisponible(entity.getLimiteCredito(), entity.getSaldoActual()))")
    CuentaClienteResponse toResponse(CuentaClienteEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "tienda", ignore = true)
    @Mapping(target = "saldoActual", constant = "0")
    @Mapping(target = "saldoDisponible", ignore = true)
    @Mapping(target = "fechaApertura", ignore = true)
    @Mapping(target = "activa", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    @Mapping(target = "abonos", ignore = true)
    CuentaClienteEntity toEntity(CrearCuentaClienteRequest request);

    List<CuentaClienteResponse> toResponseList(List<CuentaClienteEntity> entities);

    default BigDecimal calcularSaldoDisponible(BigDecimal limiteCredito, BigDecimal saldoActual) {
        if (limiteCredito == null || saldoActual == null) {
            return BigDecimal.ZERO;
        }
        return limiteCredito.subtract(saldoActual);
    }
}
