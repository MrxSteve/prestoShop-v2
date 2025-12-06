package com.devsteve.prestashopv2_backend.repositories;

import com.devsteve.prestashopv2_backend.models.entities.CuentaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaClienteRepository extends JpaRepository<CuentaClienteEntity, Long> {
}
