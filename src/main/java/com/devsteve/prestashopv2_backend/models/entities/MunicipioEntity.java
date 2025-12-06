package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "municipios",
       uniqueConstraints = @UniqueConstraint(columnNames = {"departamento_id", "nombre"}))
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class MunicipioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private DepartamentoEntity departamento;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "municipio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TiendaEntity> tiendas = new HashSet<>();
}
