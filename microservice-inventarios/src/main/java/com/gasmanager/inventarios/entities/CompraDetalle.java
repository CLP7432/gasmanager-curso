package com.gasmanager.inventarios.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @Column(name = "tipo_producto", nullable = false, length = 20)
    private String tipoProducto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aceite_id")
    private Aceite aceite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "combustible_id")
    private Combustible combustible;

    @Column(name = "presentacion", length = 20)
    private String presentacion;

    @Column(name = "unidades_por_caja")
    private Integer unidadesPorCaja;

    @Column(name = "cajas")
    private Integer cajas = 0;

    @Column(name = "piezas")
    private Integer piezas = 0;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "litros_descargados", nullable = false, precision = 12, scale = 3,
            columnDefinition = "decimal(12,3) default 0")
    private BigDecimal litrosDescargados = BigDecimal.ZERO;
}