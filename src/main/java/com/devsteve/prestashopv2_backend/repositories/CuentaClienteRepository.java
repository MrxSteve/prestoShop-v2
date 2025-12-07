package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.CuentaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaClienteRepository extends JpaRepository<CuentaClienteEntity, Long> {

    Optional<CuentaClienteEntity> findByUsuarioIdAndTiendaId(@Param("usuarioId") Long usuarioId, @Param("tiendaId") Long tiendaId);

    boolean existsByUsuarioIdAndTiendaId(@Param("usuarioId") Long usuarioId, @Param("tiendaId") Long tiendaId);

    List<CuentaClienteEntity> findByTiendaIdOrderByUsuarioNombreCompletoAsc(@Param("tiendaId") Long tiendaId);

    List<CuentaClienteEntity> findByTiendaIdAndActivaTrueOrderByUsuarioNombreCompletoAsc(@Param("tiendaId") Long tiendaId);

    List<CuentaClienteEntity> findByUsuarioIdOrderByTiendaNombreAsc(@Param("usuarioId") Long usuarioId);
}
