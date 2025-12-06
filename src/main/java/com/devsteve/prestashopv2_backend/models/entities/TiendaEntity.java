package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tiendas")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class TiendaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id", nullable = false)
    private MunicipioEntity municipio;

    @Column(name = "direccion_exacta")
    private String direccionExacta;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EmpleadoTiendaEntity> empleados = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CategoriaEntity> categorias = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductoEntity> productos = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CuentaClienteEntity> cuentasCliente = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VentaEntity> ventas = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AbonoEntity> abonos = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MovimientoTiendaEntity> movimientos = new HashSet<>();

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<NotificacionEntity> notificaciones = new HashSet<>();
}
