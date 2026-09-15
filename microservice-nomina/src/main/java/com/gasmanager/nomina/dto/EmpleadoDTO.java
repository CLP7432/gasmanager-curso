package com.gasmanager.nomina.dto;

import com.gasmanager.nomina.enums.TipoContrato;
import com.gasmanager.nomina.enums.TipoJornada;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class EmpleadoDTO {

    private Long id;


    private String codigoEmpleado;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    private String apellidoMaterno;

    private String nombreCompleto;

    private String rfc;

    private String curp;

    private String nss;

    private String email;

    private String telefono;

    private String celular;

    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    private LocalDate fechaBaja;

    private Boolean activo;

    private Long usuarioId;

    private Long puestoId;

    private String puestoNombre;

    private Long departamentoId;

    private String departamentoNombre;

    private TipoContrato tipoContrato;

    private TipoJornada tipoJornada;

    @NotNull(message = "El salario diario es obligatorio")
    @Positive(message = "El salario diario debe ser mayor a 0")
    private BigDecimal salarioDiario;

    private BigDecimal salarioMensual;

    private String numeroCuenta;

    private String banco;

    private String direccion;

    @Builder.Default
    private List<EmpleadoPuestoHistorialDTO> historial = new ArrayList<>();
}