package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByTiendaIdOrderByNombreAsc(@Param("tiendaId") Long tiendaId);

    List<ProductoEntity> findByTiendaIdAndActivoTrueOrderByNombreAsc(@Param("tiendaId") Long tiendaId);

    List<ProductoEntity> findByTiendaIdAndNombreContainingIgnoreCaseOrderByNombreAsc(
        @Param("tiendaId") Long tiendaId, @Param("nombre") String nombre);

    List<ProductoEntity> findByTiendaIdAndActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(
        @Param("tiendaId") Long tiendaId, @Param("nombre") String nombre);

    Optional<ProductoEntity> findByIdAndTiendaId(@Param("id") Long id, @Param("tiendaId") Long tiendaId);

    List<ProductoEntity> findByCategoriaIdOrderByNombreAsc(@Param("categoriaId") Long categoriaId);
}
