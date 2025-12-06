package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);

    @Query("SELECT u FROM UsuarioEntity u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.empleadoTiendas et " +
           "LEFT JOIN FETCH et.tienda " +
           "WHERE u.email = :email AND u.activo = true")
    Optional<UsuarioEntity> findByEmailWithRolesAndTiendas(@Param("email") String email);
}
