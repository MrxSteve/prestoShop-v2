package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, Long> {
}
