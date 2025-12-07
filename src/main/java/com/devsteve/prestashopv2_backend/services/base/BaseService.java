package com.devsteve.prestashopv2_backend.services.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interfaz generica para operaciones CRUD básicas en servicios
 * @param <T> Tipo del DTO de respuesta
 * @param <R> Tipo del DTO de request para crear
 * @param <U> Tipo del DTO de request para actualizar
 */
public interface BaseService<T, R, U> {
    // CRUD basico
    T crear(R request);
    T actualizar(Long id, U request);
    void eliminar(Long id);
    Optional<T> buscarPorId(Long id);

    // Listado paginado
    Page<T> listarTodos(Pageable pageable);
}
