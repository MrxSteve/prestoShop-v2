package com.devsteve.prestashopv2_backend.models.entities;

import com.devsteve.prestashopv2_backend.models.enums.EstadoVenta;
import com.devsteve.prestashopv2_backend.models.enums.TipoVenta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class VentaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntity tienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_cliente_id")
    private CuentaClienteEntity cuentaCliente;

    @Column(name = "cliente_ocasional")
    private String clienteOcasional;

    @Column(name = "fecha_venta")
    @Builder.Default
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta")
    @Builder.Default
    private TipoVenta tipoVenta = TipoVenta.CREDITO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVentaEntity> detalleVentas = new ArrayList<>();
}
