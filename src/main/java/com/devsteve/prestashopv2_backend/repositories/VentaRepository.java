package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.VentaEntity;
import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.models.enums.TipoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<VentaEntity, Long> {

    Page<VentaEntity> findByCuentaClienteId(@Param("cuentaClienteId") Long cuentaClienteId, Pageable pageable);

    Page<VentaEntity> findByTipoVenta(@Param("tipoVenta") TipoVenta tipoVenta, Pageable pageable);

    Page<VentaEntity> findByEstado(@Param("estado") EstadoVenta estado, Pageable pageable);

    Page<VentaEntity> findByFechaVentaBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable);

    Page<VentaEntity> findByClienteOcasionalContainingIgnoreCase(
        @Param("clienteOcasional") String clienteOcasional,
        Pageable pageable);

    Page<VentaEntity> findByCuentaClienteIdAndEstado(
        @Param("cuentaClienteId") Long cuentaClienteId,
        @Param("estado") EstadoVenta estado,
        Pageable pageable);

    @Query("SELECT v FROM VentaEntity v WHERE v.cuentaCliente.id = :cuentaClienteId AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Page<VentaEntity> findByClienteAndFechaRange(
        @Param("cuentaClienteId") Long cuentaClienteId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable);

    List<VentaEntity> findByTiendaIdOrderByFechaVentaDesc(@Param("tiendaId") Long tiendaId);

    List<VentaEntity> findByTiendaIdAndEstadoOrderByFechaVentaDesc(
        @Param("tiendaId") Long tiendaId,
        @Param("estado") EstadoVenta estado);

    @Query("SELECT v FROM VentaEntity v " +
           "LEFT JOIN FETCH v.detalleVentas d " +
           "LEFT JOIN FETCH d.producto " +
           "LEFT JOIN FETCH v.tienda " +
           "LEFT JOIN FETCH v.cuentaCliente cc " +
           "LEFT JOIN FETCH cc.usuario " +
           "WHERE v.id = :id")
    Optional<VentaEntity> findByIdWithDetalles(@Param("id") Long id);

    // Métodos para consultar ventas de clientes
    List<VentaEntity> findByCuentaClienteUsuarioIdOrderByFechaVentaDesc(@Param("usuarioId") Long usuarioId);

    List<VentaEntity> findByCuentaClienteUsuarioIdAndEstadoOrderByFechaVentaDesc(
        @Param("usuarioId") Long usuarioId,
        @Param("estado") EstadoVenta estado);

    List<VentaEntity> findByCuentaClienteUsuarioIdAndTiendaIdOrderByFechaVentaDesc(
        @Param("usuarioId") Long usuarioId,
        @Param("tiendaId") Long tiendaId);

    List<VentaEntity> findByCuentaClienteUsuarioIdAndEstadoInOrderByFechaVentaDesc(
        @Param("usuarioId") Long usuarioId,
        @Param("estados") List<EstadoVenta> estados);
}
