package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.EmpleadoTiendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpleadoTiendaRepository extends JpaRepository<EmpleadoTiendaEntity, Long> {
    Optional<EmpleadoTiendaEntity> findByUsuarioIdAndTiendaId(@Param("usuarioId") Long usuarioId, @Param("tiendaId") Long tiendaId);
}
