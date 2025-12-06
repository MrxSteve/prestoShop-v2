package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tienda_id", "nombre"}))
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class CategoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntity tienda;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private Set<ProductoEntity> productos = new HashSet<>();
}
