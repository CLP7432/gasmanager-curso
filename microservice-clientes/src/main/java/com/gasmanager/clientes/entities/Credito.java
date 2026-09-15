package com.gasmanager.clientes.entities;

import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.enums.MetodoPagoCredito;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "creditos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folio_credito", unique = true, nullable = false, length = 30)
    private String folioCredito;

    //La relacion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    //El dinero
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "monto_pagado", precision = 12, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", precision = 12, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "plazo_meses")
    private Integer plazoMeses;

    @Column(name = "tasa_interes", precision = 12, scale = 2)
    private BigDecimal tasaInteres;

    @Column(name = "monto_interes", precision = 12, scale = 2)
    private BigDecimal montoInteres = BigDecimal.ZERO;

    //LAS FECHAS
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_ultimo_pago")
    private LocalDate fechaUltimoPago;

    //Estado y Acuerdos
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCredito estado = EstadoCredito.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPagoCredito metodoPago;

    @Column(name = "dia_pago")
    private Integer diaPago;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    @UpdateTimestamp
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "credito", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AbonoCredito> abonos = new ArrayList<>();

    @OneToMany(mappedBy = "credito")
    @Builder.Default
    private List<NotaCredito> notasCredito = new ArrayList<>();

    //SE EJECUTA ANTES DE GUARDAR
    @PrePersist
    protected void onCreate(){
        if(saldoPendiente == null){
            saldoPendiente = montoTotal;
        }
        if(montoPagado == null){
            montoPagado = BigDecimal.ZERO;
        }
    }

    public void addAbono(AbonoCredito abono){
        abonos.add(abono);
        abono.setCredito(this);

        if(montoPagado == null){
            montoPagado = BigDecimal.ZERO;
        }
        montoPagado = montoPagado.add(abono.getMonto());

        if(saldoPendiente == null){
            saldoPendiente = montoTotal;
        }

        saldoPendiente = saldoPendiente.subtract(abono.getMonto());
        fechaUltimoPago = abono.getFechaAbono();
    }
    public void removeAbono(AbonoCredito abono){
        abonos.remove(abono);
        abono.setCredito(null);
    }
}
