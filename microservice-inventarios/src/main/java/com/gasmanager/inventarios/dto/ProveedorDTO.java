package com.gasmanager.inventarios.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProveedorDTO {

    private Long id;

    @NotBlank(message = "El RFC es obligatorio")
    private String rfc;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    private String nombreComercial;
    private String contacto;
    private String telefono;
    private String email;
    private String tipoProveedor;
    private Boolean activo;
}