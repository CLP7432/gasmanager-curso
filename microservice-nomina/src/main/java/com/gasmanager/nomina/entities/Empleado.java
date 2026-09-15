package com.gasmanager.nomina.entities;

import com.gasmanager.nomina.enums.TipoContrato;
import com.gasmanager.nomina.enums.TipoJornada;
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
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_empleado", unique = true, nullable = false, length = 50)
    private String codigoEmpleado;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 50)
    private String apellidoMaterno;

    @Column(name = "rfc", unique = true, length = 13)
    private String rfc;

    @Column(name = "curp", length = 18)
    private String curp;

    @Column(name = "nss", length = 20)
    private String nss;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "celular", length = 15)
    private String celular;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", length = 30)
    private TipoContrato tipoContrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_jornada", length = 30)
    private TipoJornada tipoJornada;

    @Column(name = "salario_diario", precision = 12, scale = 2)
    private BigDecimal salarioDiario;

    @Column(name = "salario_mensual", precision = 12, scale = 2)
    private BigDecimal salarioMensual;

    @Column(name = "numero_cuenta", length = 20)
    private String numeroCuenta;

    @Column(name = "banco", length = 50)
    private String banco;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmpleadoPuestoHistorial> historialPuestos = new ArrayList<>();

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Incidencia> incidencias = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Version
    private Long version;

    public String getNombreCompleto() {
        return nombre + " " + apellidoPaterno + (apellidoMaterno != null ? " " + apellidoMaterno : "");
    }

    public void addHistorialPuesto(EmpleadoPuestoHistorial historial) {
        historialPuestos.add(historial);
        historial.setEmpleado(this);
    }

    public void addIncidencia(Incidencia incidencia) {
        incidencias.add(incidencia);
        incidencia.setEmpleado(this);
    }
}