package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.TiendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TiendaRepository extends JpaRepository<TiendaEntity, Long> {

    List<TiendaEntity> findByActivoTrue();

    List<TiendaEntity> findByActivoTrueOrderByNombre();

    List<TiendaEntity> findByMunicipioIdAndActivoTrueOrderByNombre(@Param("municipioId") Integer municipioId);

    @Query("SELECT t FROM TiendaEntity t " +
           "JOIN t.municipio m " +
           "JOIN m.departamento d " +
           "WHERE d.id = :departamentoId AND t.activo = true " +
           "ORDER BY t.nombre")
    List<TiendaEntity> findByMunicipioDepartamentoIdAndActivoTrueOrderByNombre(@Param("departamentoId") Integer departamentoId);

    Optional<TiendaEntity> findByIdAndActivoTrue(@Param("id") Long id);
}
