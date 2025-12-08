package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.AbonoEntity;
import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AbonoRepository extends JpaRepository<AbonoEntity, Long> {

    // Consultas por cuenta de cliente
    Page<AbonoEntity> findByCuentaClienteId(@Param("cuentaClienteId") Long cuentaClienteId, Pageable pageable);

    List<AbonoEntity> findByCuentaClienteIdOrderByFechaAbonoDesc(@Param("cuentaClienteId") Long cuentaClienteId);

    Page<AbonoEntity> findByCuentaClienteIdAndEstado(
            @Param("cuentaClienteId") Long cuentaClienteId,
            @Param("estado") EstadoAbono estado,
            Pageable pageable);

    // Consultas por tienda
    Page<AbonoEntity> findByTiendaId(@Param("tiendaId") Long tiendaId, Pageable pageable);

    List<AbonoEntity> findByTiendaIdOrderByFechaAbonoDesc(@Param("tiendaId") Long tiendaId);

    Page<AbonoEntity> findByTiendaIdAndEstado(
            @Param("tiendaId") Long tiendaId,
            @Param("estado") EstadoAbono estado,
            Pageable pageable);

    // Consultas por estado
    Page<AbonoEntity> findByEstado(@Param("estado") EstadoAbono estado, Pageable pageable);

    // Consultas por fecha
    Page<AbonoEntity> findByFechaAbonoBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Consultas para clientes
    List<AbonoEntity> findByCuentaClienteUsuarioIdOrderByFechaAbonoDesc(@Param("usuarioId") Long usuarioId);

    List<AbonoEntity> findByCuentaClienteUsuarioIdAndEstadoOrderByFechaAbonoDesc(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoAbono estado);

    // Consulta con joins para optimización
    @Query("SELECT a FROM AbonoEntity a " +
           "LEFT JOIN FETCH a.cuentaCliente cc " +
           "LEFT JOIN FETCH cc.usuario " +
           "LEFT JOIN FETCH a.tienda " +
           "WHERE a.id = :id")
    AbonoEntity findByIdWithDetails(@Param("id") Long id);

    // Método para clientes - abonos por tienda
    List<AbonoEntity> findByCuentaClienteUsuarioIdAndTiendaIdOrderByFechaAbonoDesc(
            @Param("usuarioId") Long usuarioId,
            @Param("tiendaId") Long tiendaId);
}
