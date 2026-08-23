package com.gasmanager.clientes.dto;

import com.gasmanager.clientes.enums.TipoPersona;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "El código de cliente es obligatorio")
    private String codigoCliente;

    @NotNull(message = "El tipo de persona es obligatorio")
    private TipoPersona tipoPersona;

    private String razonSocial;
    private String nombreComercial;

    private String rfc;
    private String curp;

    @Email(message = "Email inválido")
    private String email;

    private String telefono;
    private String celular;

    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String ciudad;
    private String estado;
    private String codigoPostal;

    private Boolean activo;
}
