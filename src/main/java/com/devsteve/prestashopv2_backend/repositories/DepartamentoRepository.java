package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, Integer> {
    Optional<DepartamentoEntity> findByNombre(String nombre);
}
