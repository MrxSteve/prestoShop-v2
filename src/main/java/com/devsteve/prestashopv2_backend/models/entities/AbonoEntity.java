package com.devsteve.prestashopv2_backend.models.entities;

import com.devsteve.prestashopv2_backend.models.enums.EstadoAbono;
import com.devsteve.prestashopv2_backend.models.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonos")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class AbonoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntity tienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_cliente_id", nullable = false)
    private CuentaClienteEntity cuentaCliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_abono")
    @Builder.Default
    private LocalDateTime fechaAbono = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    @Builder.Default
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoAbono estado = EstadoAbono.APLICADO;
}
