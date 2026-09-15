package com.gasmanager.facturacion.entities;

import com.gasmanager.facturacion.enums.EstadoFactura;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio", unique = true, nullable = false, length = 30)
    private String folio;

    @Column(name = "uuid", unique = true, nullable = false, length = 36)
    private String uuid;

    @Column(name = "serie", length = 10)
    private String serie;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_fiscal_id", nullable = false)
    private ClienteFiscal clienteFiscal;

    @Column(name = "receptor_rfc", length = 13, nullable = false)
    private String receptorRfc;

    @Column(name = "receptor_nombre", length = 300)
    private String receptorNombre;

    @Column(name = "receptor_regimen_fiscal", length = 3)
    private String receptorRegimenFiscal;

    @Column(name = "receptor_codigo_postal", length = 5)
    private String receptorCodigoPostal;

    @Column(name = "receptor_uso_cfdi", length = 3)
    private String receptorUsoCfdi;

    @Builder.Default
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "ieps", nullable = false, precision = 12, scale = 2)
    private BigDecimal ieps = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "forma_pago", length = 2)
    private String formaPago;

    @Column(name = "metodo_pago", length = 3)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoFactura estado = EstadoFactura.SIMULADA;

    @Column(name = "timbrada", nullable = false)
    @Builder.Default
    private boolean timbrada = false;

    @Column(name = "fecha_timbrado")
    private LocalDateTime fechaTimbrado;

    @Column(name = "sello", columnDefinition = "TEXT")
    private String sello;

    @Column(name = "no_certificado", length = 20)
    private String noCertificado;

    @Column(name = "xml_path", length = 500)
    private String xmlPath;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "correo_enviado", nullable = false)
    @Builder.Default
    private boolean correoEnviado = false;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Builder.Default
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<FacturaConcepto> conceptos = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}