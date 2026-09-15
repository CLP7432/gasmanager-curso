package com.gasmanager.clientes.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items_nota_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemNotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id", nullable = false)
    private NotaCredito notaCredito;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "producto", nullable = false, length = 100)
    private String producto;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "unidad", length = 10)
    @Builder.Default
    private String unidad = "L";

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void calcularSubtotal() {
        if (subtotal == null && precioUnitario != null && cantidad != null) {
            subtotal = precioUnitario.multiply(cantidad);
        }
    }
}