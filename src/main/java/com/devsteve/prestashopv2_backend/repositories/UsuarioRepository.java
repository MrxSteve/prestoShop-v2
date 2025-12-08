package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);

    @Query("SELECT u FROM UsuarioEntity u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.empleadoTiendas et " +
           "LEFT JOIN FETCH et.tienda " +
           "LEFT JOIN FETCH u.cuentasCliente cc " +
           "WHERE u.email = :email AND u.activo = true")
    Optional<UsuarioEntity> findByEmailWithRolesAndTiendas(@Param("email") String email);

    @Query("SELECT u FROM UsuarioEntity u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.empleadoTiendas et " +
           "LEFT JOIN FETCH et.tienda " +
           "LEFT JOIN FETCH u.cuentasCliente cc " +
           "LEFT JOIN FETCH cc.tienda " +
           "WHERE u.id = :id")
    Optional<UsuarioEntity> findByIdWithRolesAndTiendas(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN u.cuentasCliente cc " +
           "JOIN u.roles r " +
           "WHERE cc.tienda.id = :tiendaId AND r.nombre = 'CLIENTE' AND u.activo = true")
    List<UsuarioEntity> findClientesByTiendaId(@Param("tiendaId") Long tiendaId);

    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN u.roles r " +
           "WHERE r.nombre IN ('EMPLEADO', 'ENCARGADO') AND u.activo = true")
    List<UsuarioEntity> findEmpleadosAndEncargados();

    @Query("SELECT DISTINCT u FROM UsuarioEntity u " +
           "JOIN u.empleadoTiendas et " +
           "JOIN u.roles r " +
           "WHERE et.tienda.id = :tiendaId AND r.nombre IN ('EMPLEADO', 'ENCARGADO') AND u.activo = true")
    List<UsuarioEntity> findEmpleadosByTiendaId(@Param("tiendaId") Long tiendaId);
}
