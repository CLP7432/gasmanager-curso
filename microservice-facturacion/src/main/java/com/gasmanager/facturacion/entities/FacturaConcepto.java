package com.gasmanager.facturacion.entities;

import com.gasmanager.facturacion.enums.OrigenConceptoFactura;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "factura_conceptos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaConcepto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", length = 20, nullable = false)
    private OrigenConceptoFactura origen;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "origen_folio", length = 50)
    private String origenFolio;

    @Column(name = "descripcion", length = 300, nullable = false)
    private String descripcion;

    @Column(name = "clave_prod_serv", length = 8, nullable = false)
    private String claveProdServ;

    @Column(name = "clave_unidad", length = 3, nullable = false)
    private String claveUnidad;

    @Column(name = "unidad", length = 30)
    private String unidad;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(name = "ieps", nullable = false, precision = 12, scale = 2)
    private BigDecimal ieps;

    @Column(name = "es_combustible", nullable = false)
    @Builder.Default
    private boolean esCombustible = false;

    @Column(name = "tipo_combustible", length = 3)
    private String tipoCombustible;
}