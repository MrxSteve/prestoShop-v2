package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.SolicitudTiendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudTiendaRepository extends JpaRepository<SolicitudTiendaEntity, Long> {

    boolean existsByEmailEncargadoAndEstado(String emailEncargado, SolicitudTiendaEntity.EstadoSolicitud estado);

    List<SolicitudTiendaEntity> findByEstadoOrderByCreatedAtDesc(SolicitudTiendaEntity.EstadoSolicitud estado);

    @Query("SELECT s FROM SolicitudTiendaEntity s " +
           "LEFT JOIN FETCH s.municipio m " +
           "LEFT JOIN FETCH m.departamento " +
           "WHERE s.estado = :estado " +
           "ORDER BY s.createdAt DESC")
    List<SolicitudTiendaEntity> findByEstadoWithMunicipioAndDepartamento(@Param("estado") SolicitudTiendaEntity.EstadoSolicitud estado);
}
