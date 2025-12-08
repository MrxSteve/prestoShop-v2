package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.MovimientoTiendaEntity;
import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoTiendaRepository extends JpaRepository<MovimientoTiendaEntity, Long> {

    // Consultas por tienda
    Page<MovimientoTiendaEntity> findByTiendaId(@Param("tiendaId") Long tiendaId, Pageable pageable);

    List<MovimientoTiendaEntity> findByTiendaIdOrderByFechaEventoDesc(@Param("tiendaId") Long tiendaId);

    // Consultas por tipo de evento
    Page<MovimientoTiendaEntity> findByTiendaIdAndTipoEvento(
            @Param("tiendaId") Long tiendaId,
            @Param("tipoEvento") TipoEvento tipoEvento,
            Pageable pageable);

    // Consultas por usuario operador
    Page<MovimientoTiendaEntity> findByUsuarioOperadorId(@Param("usuarioOperadorId") Long usuarioOperadorId, Pageable pageable);

    // Consultas por cliente
    Page<MovimientoTiendaEntity> findByClienteUsuarioId(@Param("clienteUsuarioId") Long clienteUsuarioId, Pageable pageable);

    // Consultas por fecha
    Page<MovimientoTiendaEntity> findByTiendaIdAndFechaEventoBetween(
            @Param("tiendaId") Long tiendaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Consultas para estadísticas - ventas del día
    @Query("SELECT m FROM MovimientoTiendaEntity m WHERE m.tienda.id = :tiendaId " +
           "AND m.tipoEvento = 'VENTA_REGISTRADA' " +
           "AND m.fechaEvento BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoTiendaEntity> findVentasDelDia(
            @Param("tiendaId") Long tiendaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Consultas para estadísticas - abonos del día
    @Query("SELECT m FROM MovimientoTiendaEntity m WHERE m.tienda.id = :tiendaId " +
           "AND m.tipoEvento = 'ABONO_REGISTRADO' " +
           "AND m.fechaEvento BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoTiendaEntity> findAbonosDelDia(
            @Param("tiendaId") Long tiendaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Consultas para estadísticas - ventas del mes
    @Query("SELECT m FROM MovimientoTiendaEntity m WHERE m.tienda.id = :tiendaId " +
           "AND m.tipoEvento = 'VENTA_REGISTRADA' " +
           "AND m.fechaEvento BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoTiendaEntity> findVentasDelMes(
            @Param("tiendaId") Long tiendaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Consultas para estadísticas - abonos del mes
    @Query("SELECT m FROM MovimientoTiendaEntity m WHERE m.tienda.id = :tiendaId " +
           "AND m.tipoEvento = 'ABONO_REGISTRADO' " +
           "AND m.fechaEvento BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoTiendaEntity> findAbonosDelMes(
            @Param("tiendaId") Long tiendaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Consulta con joins para optimización
    @Query("SELECT m FROM MovimientoTiendaEntity m " +
           "LEFT JOIN FETCH m.tienda " +
           "LEFT JOIN FETCH m.usuarioOperador " +
           "LEFT JOIN FETCH m.clienteUsuario " +
           "WHERE m.id = :id")
    MovimientoTiendaEntity findByIdWithDetails(@Param("id") Long id);
}
