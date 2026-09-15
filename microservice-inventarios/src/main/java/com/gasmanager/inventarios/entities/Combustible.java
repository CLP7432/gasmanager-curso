package com.gasmanager.inventarios.entities;

import com.gasmanager.inventarios.enums.TipoCombustible;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "combustibles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Combustible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, unique = true, length = 20)
    private TipoCombustible tipo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "precio_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioActual;

    // Último costo de compra (factura PEMEX). Lo actualiza cada compra;
    // el precio de venta SOLO cambia en Cambios de precio.
    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "fecha_ultimo_cambio_precio")
    private LocalDateTime fechaUltimoCambioPrecio;

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "combustible", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrecioHistorico> historialPrecios = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addPrecioHistorico(PrecioHistorico precioHistorico) {
        historialPrecios.add(precioHistorico);
        precioHistorico.setCombustible(this);
    }
}
