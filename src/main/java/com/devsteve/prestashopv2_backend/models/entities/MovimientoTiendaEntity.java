package com.devsteve.prestashopv2_backend.models.entities;

import com.devsteve.prestashopv2_backend.models.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_tienda")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @Builder
public class MovimientoTiendaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntity tienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_operador_id", nullable = false)
    private UsuarioEntity usuarioOperador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_usuario_id")
    private UsuarioEntity clienteUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    @Column(nullable = false)
    private String descripcion;

    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "referencia_tabla")
    private String referenciaTabla;

    @Column(name = "fecha_evento")
    @Builder.Default
    private LocalDateTime fechaEvento = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
