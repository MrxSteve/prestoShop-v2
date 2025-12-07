package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.DepartamentoEntity;
import com.devsteve.prestashopv2_backend.models.entities.MunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipioRepository extends JpaRepository<MunicipioEntity, Integer> {
    Optional<MunicipioEntity> findByDepartamentoAndNombre(DepartamentoEntity departamento, String nombre);
}
