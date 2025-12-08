package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {

    List<CategoriaEntity> findByTiendaIdOrderByNombreAsc(@Param("tiendaId") Long tiendaId);

    List<CategoriaEntity> findByTiendaIdAndNombreContainingIgnoreCaseOrderByNombreAsc(
        @Param("tiendaId") Long tiendaId, @Param("nombre") String nombre);

    Optional<CategoriaEntity> findByIdAndTiendaId(@Param("id") Long id, @Param("tiendaId") Long tiendaId);

    boolean existsByNombreAndTiendaId(@Param("nombre") String nombre, @Param("tiendaId") Long tiendaId);

    boolean existsByNombreAndTiendaIdAndIdNot(@Param("nombre") String nombre, @Param("tiendaId") Long tiendaId, @Param("id") Long id);
}
