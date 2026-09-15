package com.gasmanager.clientes.entities;

import com.gasmanager.clientes.enums.EstadoNotaCredito;
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
@Table(name = "notas_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, nullable = false, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id", nullable = false)
    private Credito credito;

    @Column(name = "saldo", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "vehiculo", length = 50)
    private String vehiculo;

    @Column(name = "conductor", length = 100)
    private String conductor;

    @Column(name = "fecha_carga")
    @Builder.Default
    private LocalDate fechaCarga = LocalDate.now();

    @Column(name = "origen_corte", length = 20)
    private String origenCorte;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoNotaCredito estado = EstadoNotaCredito.ACTIVA;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "notaCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemNotaCredito> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (saldo == null) saldo = BigDecimal.ZERO;
    }

    public void addItem(ItemNotaCredito item) {
        items.add(item);
        item.setNotaCredito(this);
        saldo = saldo.subtract(item.getSubtotal());
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            estado = EstadoNotaCredito.AGOTADA;
        }
    }
}