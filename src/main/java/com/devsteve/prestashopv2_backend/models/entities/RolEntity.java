package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class RolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<UsuarioEntity> usuarios = new HashSet<>();
}
