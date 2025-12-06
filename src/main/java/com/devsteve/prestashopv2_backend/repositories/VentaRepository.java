package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<VentaEntity, Long> {
}
