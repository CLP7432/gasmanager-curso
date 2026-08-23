package com.gasmanager.clientes.entities;

import com.gasmanager.clientes.enums.TipoPersona;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_cliente", unique = true, nullable = false, length = 50)
    private String codigoCliente;

    @Column(name = "tipo_persona", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoPersona tipoPersona;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(name = "rfc", length = 18)
    private String rfc;

    @Column(name = "curp", length = 18)
    private String curp;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "celular", length = 15)
    private String celular;

    @Column(name = "calle", length = 100)
    private String calle;

    @Column(name = "numero_exterior", length = 20)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @Column(name = "colonia", length = 100)
    private String colonia;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "estado", length = 100)
    private String estado;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;




}
