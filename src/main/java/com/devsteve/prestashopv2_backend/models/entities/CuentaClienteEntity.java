package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cuentas_cliente",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "tienda_id"}))
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class CuentaClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntity tienda;

    @Column(name = "limite_credito", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "saldo_actual", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    // saldo_disponible es un campo calculado en la DB, no se mapea como atributo
    // pero se puede agregar un método para calcularlo
    @Column(name = "saldo_disponible", precision = 10, scale = 2, insertable = false, updatable = false)
    @org.hibernate.annotations.Generated
    private BigDecimal saldoDisponible;

    @Column(name = "fecha_apertura")
    @Builder.Default
    private LocalDate fechaApertura = LocalDate.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cuentaCliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VentaEntity> ventas = new HashSet<>();

    @OneToMany(mappedBy = "cuentaCliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AbonoEntity> abonos = new HashSet<>();
}
