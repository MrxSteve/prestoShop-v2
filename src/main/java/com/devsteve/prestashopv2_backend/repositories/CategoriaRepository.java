package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
}
