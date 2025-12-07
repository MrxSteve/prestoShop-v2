package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.CuentaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CuentaClienteRepository extends JpaRepository<CuentaClienteEntity, Long> {

    Optional<CuentaClienteEntity> findByUsuarioIdAndTiendaId(@Param("usuarioId") Long usuarioId, @Param("tiendaId") Long tiendaId);
}
