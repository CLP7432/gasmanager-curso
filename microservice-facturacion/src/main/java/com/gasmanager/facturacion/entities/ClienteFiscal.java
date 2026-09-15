package com.gasmanager.facturacion.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes_fiscales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", unique = true, nullable = false)
    private Long clienteId;

    @Column(name = "rfc", length = 13, nullable = false)
    private String rfc;

    @Column(name = "razon_social", length = 300)
    private String razonSocial;

    @Column(name = "regimen_fiscal", length = 3, nullable = false)
    private String regimenFiscal;

    @Column(name = "codigo_postal", length = 5, nullable = false)
    private String codigoPostal;

    @Column(name = "uso_cfdi", length = 3)
    private String usoCfdi;

    @Column(name = "correo", length = 150)
    private String correo;

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