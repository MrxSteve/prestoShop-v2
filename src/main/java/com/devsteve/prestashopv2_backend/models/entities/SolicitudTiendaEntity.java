package com.devsteve.prestashopv2_backend.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_tienda")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SolicitudTiendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreTienda;

    @Column(nullable = false)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id", nullable = false)
    private MunicipioEntity municipio;

    @Column(name = "direccion_exacta", nullable = false)
    private String direccionExacta;

    @Column(name = "nombre_encargado", nullable = false)
    private String nombreEncargado;

    @Column(name = "email_encargado", nullable = false, unique = true)
    private String emailEncargado;

    @Column(name = "dui_encargado")
    private String duiEncargado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    private String descripcion;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @OneToOne
    @JoinColumn(name = "tienda_id")
    private TiendaEntity tienda;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "aprobado_por")
    private String aprobadoPor;

    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }
}
